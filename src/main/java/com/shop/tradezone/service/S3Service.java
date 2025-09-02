package com.shop.tradezone.service;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import software.amazon.awssdk.core.exception.SdkException;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.ObjectCannedACL;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

@Service
@RequiredArgsConstructor
@Slf4j
public class S3Service {

	private final S3Client s3Client;

	@Value("${aws.s3.bucket-name}")
	private String bucketName;

	// [1] MultipartFile 리스트 업로드 (원본 그대로)
	public List<String> upload(List<MultipartFile> files) {
		return files.stream().map(this::uploadImage).toList();
	}

	// [2] MultipartFile 하나 업로드
	private String uploadImage(MultipartFile file) {
		validateFile(file.getOriginalFilename());
		return uploadImageToS3(file);
	}

	// [3] MultipartFile → InputStream 업로드 (직접 S3)
	private String uploadImageToS3(MultipartFile file) {
		String originalFilename = file.getOriginalFilename();
		String extension = Objects.requireNonNull(originalFilename).substring(originalFilename.lastIndexOf('.') + 1);
		String s3FileName = UUID.randomUUID().toString().substring(0, 10) + "_" + originalFilename;

		try (InputStream inputStream = file.getInputStream()) {
			PutObjectRequest putRequest = PutObjectRequest.builder().bucket(bucketName).key(s3FileName)
					.contentType("image/" + extension).acl(ObjectCannedACL.PUBLIC_READ).contentLength(file.getSize())
					.build();

			s3Client.putObject(putRequest, RequestBody.fromInputStream(inputStream, file.getSize()));

		} catch (IOException | SdkException e) { // IOException 외에 AWS SDK 예외도 잡기
			log.error("S3 업로드 실패", e); // 예외 로그 확실히 남기기
			throw new RuntimeException("S3 업로드 실패", e); // 예외 다시 던져서 트랜잭션 롤백 유도
		}

		return s3Client.utilities().getUrl(builder -> builder.bucket(bucketName).key(s3FileName)).toString();
	}

	// [4] byte[] 기반 이미지 업로드 (리사이징 후 호출)
	public String upload(byte[] bytes, String fileName, String contentType) {
		PutObjectRequest request = PutObjectRequest.builder().bucket(bucketName).key(fileName).contentType(contentType)
				.acl(ObjectCannedACL.PUBLIC_READ).build();

		s3Client.putObject(request, RequestBody.fromBytes(bytes));

		return s3Client.utilities().getUrl(builder -> builder.bucket(bucketName).key(fileName)).toString();
	}

	// [5] S3 URL로 파일 삭제
	public void deleteFile(String fileUrl) {
		String key = extractKeyFromUrl(fileUrl);

		DeleteObjectRequest deleteRequest = DeleteObjectRequest.builder().bucket(bucketName).key(key).build();

		s3Client.deleteObject(deleteRequest);
	}

	// [6] URL → S3 key 추출
	private String extractKeyFromUrl(String fileUrl) {
		try {
			URI uri = new URI(fileUrl);
			String path = uri.getPath();
			return path.startsWith("/") ? path.substring(1) : path;
		} catch (URISyntaxException e) {
			throw new IllegalArgumentException("잘못된 S3 URL입니다.");
		}
	}

	private void validateFile(String filename) {
		if (filename == null || filename.isBlank()) {
			throw new IllegalArgumentException("파일명이 비어 있습니다.");
		}
	}
}
