package com.shop.tradezone.service;

import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.UUID;

import javax.imageio.ImageIO;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.shop.tradezone.entity.Item;
import com.shop.tradezone.entity.ItemImg;
import com.shop.tradezone.repository.ItemImgRepository;

import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class ItemImgService {

	private final ItemImgRepository itemImgRepository;
	private final S3Service s3Service;

	private final List<String> allowedContentTypes = List.of("image/jpeg", "image/png", "image/gif", "image/webp",
			"image/bmp");

	// 이미지 업로드 + 리사이징 + DB 저장 (상위 트랜잭션과 분리)
	@Transactional(propagation = Propagation.REQUIRES_NEW)
	public ItemImg uploadAndSaveItemImg(MultipartFile file, Item item) throws IOException {
		log.info("서비스: uploadAndSaveItemImg 호출됨 - 파일명: {}", file.getOriginalFilename());

		validateFile(file);

		BufferedImage originalImage = ImageIO.read(file.getInputStream());
		BufferedImage thumbnailImage = resizeImage(originalImage, 200);

		String originalFileName = "images/original_" + UUID.randomUUID() + ".jpg";
		String thumbnailFileName = "images/thumbnail_" + UUID.randomUUID() + ".jpg";

		byte[] originalBytes = toByteArray(originalImage);
		byte[] thumbnailBytes = toByteArray(thumbnailImage);

		String originalUrl;
		String thumbnailUrl;

		try {
			originalUrl = s3Service.upload(originalBytes, originalFileName, "image/jpeg");
			thumbnailUrl = s3Service.upload(thumbnailBytes, thumbnailFileName, "image/jpeg");
			log.info("✅ S3 업로드 성공 - 원본: {}, 썸네일: {}", originalUrl, thumbnailUrl);
		} catch (Exception e) {
			log.error("❌ S3 업로드 실패", e);
			throw e; // 예외 재던져서 호출자에게 알림 (트랜잭션 롤백 유도)
		}

		ItemImg itemImg = ItemImg.builder().imgName(file.getOriginalFilename()).imgUrl(originalUrl)
				.thumbnailUrl(thumbnailUrl).item(item).build();

		ItemImg savedImg = itemImgRepository.save(itemImg);
		log.info("✅ 이미지 DB 저장 완료 - 이미지ID: {}", savedImg.getId());

		return savedImg;
	}

	@Transactional(propagation = Propagation.REQUIRES_NEW)
	public void deleteItemImg(Long itemImgId) {
		ItemImg img = itemImgRepository.findById(itemImgId).orElseThrow(() -> new RuntimeException("이미지를 찾을 수 없습니다."));

		s3Service.deleteFile(img.getImgUrl());
		s3Service.deleteFile(img.getThumbnailUrl());
		itemImgRepository.delete(img);
	}

	@Transactional(propagation = Propagation.REQUIRES_NEW)
	public ItemImg updateItemImg(Long itemImgId, MultipartFile newFile) throws IOException {
		validateFile(newFile);

		ItemImg existingImg = itemImgRepository.findById(itemImgId)
				.orElseThrow(() -> new RuntimeException("이미지를 찾을 수 없습니다."));

		// 기존 이미지 삭제
		s3Service.deleteFile(existingImg.getImgUrl());
		s3Service.deleteFile(existingImg.getThumbnailUrl());

		// 새로운 이미지 처리
		BufferedImage originalImage = ImageIO.read(newFile.getInputStream());
		BufferedImage thumbnailImage = resizeImage(originalImage, 200);

		String originalFileName = "images/original_" + UUID.randomUUID() + ".jpg";
		String thumbnailFileName = "images/thumbnail_" + UUID.randomUUID() + ".jpg";

		byte[] originalBytes = toByteArray(originalImage);
		byte[] thumbnailBytes = toByteArray(thumbnailImage);

		String originalUrl = s3Service.upload(originalBytes, originalFileName, "image/jpeg");
		String thumbnailUrl = s3Service.upload(thumbnailBytes, thumbnailFileName, "image/jpeg");

		existingImg.setImgName(newFile.getOriginalFilename());
		existingImg.setImgUrl(originalUrl);
		existingImg.setThumbnailUrl(thumbnailUrl);

		return itemImgRepository.save(existingImg);
	}

	private void validateFile(MultipartFile file) {
		if (file.isEmpty()) {
			throw new IllegalArgumentException("파일이 비어 있습니다.");
		}
		String contentType = file.getContentType();
		if (contentType == null || !allowedContentTypes.contains(contentType)) {
			throw new IllegalArgumentException("지원하지 않는 이미지 형식입니다: " + contentType);
		}
	}

	private byte[] toByteArray(BufferedImage image) throws IOException {
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		ImageIO.write(image, "jpg", baos);
		return baos.toByteArray();
	}

	private BufferedImage resizeImage(BufferedImage originalImage, int targetWidth) {
		int targetHeight = (int) (originalImage.getHeight() * (targetWidth / (double) originalImage.getWidth()));
		BufferedImage resizedImage = new BufferedImage(targetWidth, targetHeight, BufferedImage.TYPE_INT_RGB);
		Graphics2D g2d = resizedImage.createGraphics();
		g2d.drawImage(originalImage, 0, 0, targetWidth, targetHeight, null);
		g2d.dispose();
		return resizedImage;
	}
}
