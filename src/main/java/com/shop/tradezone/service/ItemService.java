package com.shop.tradezone.service;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.shop.tradezone.constant.ItemSellStatus;
import com.shop.tradezone.dto.ItemCardDto;
import com.shop.tradezone.dto.ItemDetailDto;
import com.shop.tradezone.dto.ItemFormDto;
import com.shop.tradezone.dto.ItemListDto;
import com.shop.tradezone.dto.ItemUpdateDto;
import com.shop.tradezone.dto.ReviewFormDto;
import com.shop.tradezone.entity.Category;
import com.shop.tradezone.entity.Item;
import com.shop.tradezone.entity.ItemImg;
import com.shop.tradezone.entity.Member;
import com.shop.tradezone.repository.CategoryRepository;
import com.shop.tradezone.repository.ItemRepository;
import com.shop.tradezone.repository.LikeRepository;
import com.shop.tradezone.repository.MemberRepository;
import com.shop.tradezone.repository.ReviewRepository;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ItemService {

	private final ItemRepository itemRepository;
	private final MemberRepository memberRepository;
	private final CategoryRepository categoryRepository;
	private final LikeRepository likeRepository;
	private final ReviewRepository reviewRepository;
	private final ItemImgService itemImgService;

	// 메인 최근 상품
	public Page<ItemCardDto> getMainItems(int page, int size) {
		Pageable pageable = PageRequest.of(page, size, Sort.by("created").descending());
		Page<Item> itemsPage = itemRepository.findAll(pageable);
		return itemsPage.map(this::toItemCardDto);
	}

	// 상품 ID로 조회
	public Item getItemById(Long itemId) {
		return itemRepository.findById(itemId)
				.orElseThrow(() -> new EntityNotFoundException("상품을 찾을 수 없습니다."));
	}

	// 마이페이지 상품
	public Page<ItemCardDto> getItemsByMember(Long memberId, int page, int size) {
		Pageable pageable = PageRequest.of(page, size, Sort.by("created").descending());
		return itemRepository.findBySellerId(memberId, pageable).map(this::toItemCardDto);
	}

	// 카테고리별 상품
	public Page<ItemCardDto> getItemsByCategory(Long parentCategoryId, Long childCategoryId, Pageable pageable) {
		return itemRepository.findByCategoryId_IdAndCategoryId_Parent_Id(childCategoryId, parentCategoryId, pageable)
				.map(this::toItemCardDto);
	}

	// 관리자 상품 리스트
	@Transactional
	@PreAuthorize("hasRole('ADMIN')")
	public Page<Item> getAllItems(int page) {

		List<Sort.Order> sorts = new ArrayList<>();
		sorts.add(Sort.Order.desc("id")); // 내림차순 정렬
		Pageable pageable = PageRequest.of(page, 10, Sort.by(sorts)); // 한 페이지에 10개

		return itemRepository.findAll(pageable);
	}

	// 상품 등록
	@Transactional
	public Long createItem(ItemFormDto dto, Long sellerId) throws IOException {
		log.info("▶▶▶ createItem() 진입");

		Member seller = memberRepository.findById(sellerId)
				.orElseThrow(() -> new EntityNotFoundException("회원이 존재하지 않습니다."));

		Category parent = categoryRepository.findById(dto.getParentCategoryId())
				.orElseThrow(() -> new EntityNotFoundException("부모 카테고리가 존재하지 않습니다."));

		Category child = categoryRepository.findById(dto.getChildCategoryId())
				.orElseThrow(() -> new EntityNotFoundException("자식 카테고리가 존재하지 않습니다."));

		if (child.getParent() == null || !child.getParent().getId().equals(parent.getId())) {
			throw new IllegalArgumentException("부모-자식 카테고리 관계가 일치하지 않습니다.");
		}

		Item item = Item.builder().name(dto.getName()).description(dto.getDescription()).price(dto.getPrice())
				.region(dto.getRegion()).seller(seller).categoryId(child).status(ItemSellStatus.SELL).viewCount(0)
				.created(LocalDateTime.now()).build();

		itemRepository.save(item);
		log.info("✅ Item 저장 완료: id={}", item.getId());

		if (dto.getImages() != null) {
			for (var file : dto.getImages()) {
				try {
					ItemImg img = itemImgService.uploadAndSaveItemImg(file, item);
					item.getImages().add(img);
					log.info("✅ 이미지 저장 완료: url={}", img.getImgUrl());
				} catch (Exception e) {
					log.error("❌ 이미지 저장 중 에러 발생", e);
				}
			}
		}

		return item.getId();
	}

	// 상품 상세
	@Transactional
	public ItemDetailDto getItemDetail(Long itemId, Long memberId) {
		Item item = itemRepository.findById(itemId)
				.orElseThrow(() -> new EntityNotFoundException("상품을 찾을 수 없습니다."));

		// 조회수 증가
		item.increaseViewCount();

		// 좋아요 여부 확인
		boolean isLiked = false;
		if (memberId != null) {
			Member member = memberRepository.findById(memberId).orElse(null);
			if (member != null) {
				isLiked = likeRepository.existsByItemAndMember(item, member);
			}
		}

		// 찜 수는 likeRepository로 정확히 조회
		int likeCount = likeRepository.countByItem(item);

		// 판매자 상품 수 계산
		int sellerItemCount = itemRepository.countBySeller(item.getSeller());

		// 리뷰 목록 조회
		List<ReviewFormDto> reviews = reviewRepository.findByItemIdOrderByCreatedDesc(itemId).stream()
				.map(r -> new ReviewFormDto(r.getId(), r.getItem().getName(), r.getItem().getId(),
						r.getMember().getUsername(), r.getContent(), r.getCreated()))
				.toList();

		List<String> imageUrls = item.getImages().stream().map(ItemImg::getImgUrl).toList();

		Category childCategory = item.getCategoryId();
		Category parentCategory = childCategory.getParent();

		Long childCategoryId = item.getCategoryId().getId();
		Long parentCategoryId = parentCategory != null ? parentCategory.getId() : null;

		// 현재 사용자가 구매자인지 확인
		boolean isBuyer = false;
		if (item.getStatus() == ItemSellStatus.TRADING && item.getBuyer() != null) {
			isBuyer = item.getBuyer().getId().equals(memberId);
		}

		return ItemDetailDto.builder()
				.id(item.getId())
				.name(item.getName())
				.description(item.getDescription())
				.price(item.getPrice())
				.region(item.getRegion())
				.sellerName(item.getSeller() != null ? item.getSeller().getUsername() : "Unknown")
				.sellerId(item.getSeller() != null ? item.getSeller().getId() : null)
				.buyerName(item.getBuyer() != null ? item.getBuyer().getUsername() : null)
				.buyerId(item.getBuyer() != null ? item.getBuyer().getId() : null)
				.isBuyer(isBuyer)
				.parentName(parentCategory != null ? parentCategory.getName() : "없음")
				.childName(childCategory.getName())
				.childCategoryId(childCategoryId)
				.status(item.getStatus())
				.parentCategoryId(parentCategoryId)
				.created(item.getCreated())
				.viewCount(item.getViewCount())
				.imgUrls(imageUrls)
				.reviews(reviews)
				.likeCount(likeCount)
				.sellerItemCount(sellerItemCount)
				.isLiked(isLiked)
				.build();
	}

	// 구매하기
	@Transactional
	public void buyItem(Long itemId, Long buyerId) {
		Item item = itemRepository.findById(itemId)
				.orElseThrow(() -> new EntityNotFoundException("상품을 찾을 수 없습니다."));

		if (item.getStatus() != ItemSellStatus.SELL) {
			throw new IllegalStateException("구매할 수 없는 상품입니다.");
		}

		Member buyer = memberRepository.findById(buyerId)
				.orElseThrow(() -> new EntityNotFoundException("구매자 정보를 찾을 수 없습니다."));

		// 본인 상품은 구매할 수 없음
		if (item.getSeller().getId().equals(buyerId)) {
			throw new IllegalStateException("본인 상품은 구매할 수 없습니다.");
		}

		item.setStatus(ItemSellStatus.TRADING);
		item.setBuyer(buyer);
		itemRepository.save(item);
	}

	// 거래중지 (판매자만)
	@Transactional
	public void stopTrade(Long itemId, Long sellerId) {
		Item item = itemRepository.findById(itemId)
				.orElseThrow(() -> new EntityNotFoundException("상품을 찾을 수 없습니다."));

		if (!item.getSeller().getId().equals(sellerId)) {
			throw new AccessDeniedException("판매자만 거래를 중지할 수 있습니다.");
		}

		if (item.getStatus() != ItemSellStatus.TRADING) {
			throw new IllegalStateException("거래중인 상품만 중지할 수 있습니다.");
		}

		item.setStatus(ItemSellStatus.STOPPED);
		itemRepository.save(item);
	}

	// 거래재개 (판매자만)
	@Transactional
	public void resumeTrade(Long itemId, Long sellerId) {
		Item item = itemRepository.findById(itemId)
				.orElseThrow(() -> new EntityNotFoundException("상품을 찾을 수 없습니다."));

		if (!item.getSeller().getId().equals(sellerId)) {
			throw new AccessDeniedException("판매자만 거래를 재개할 수 있습니다.");
		}

		if (item.getStatus() != ItemSellStatus.STOPPED) {
			throw new IllegalStateException("거래중지된 상품만 재개할 수 있습니다.");
		}

		item.setStatus(ItemSellStatus.SELL);
		item.setBuyer(null); // 구매자 정보 초기화
		itemRepository.save(item);
	}

	// 결제완료 처리 (구매자 전용)
	@Transactional
	public void completePayment(Long itemId, Long buyerId) {
		log.info("결제완료 서비스 시작 - 상품ID: {}, 구매자ID: {}", itemId, buyerId);
		
		Item item = itemRepository.findById(itemId)
				.orElseThrow(() -> new EntityNotFoundException("상품을 찾을 수 없습니다."));

		log.info("상품 상태: {}, 구매자: {}", item.getStatus(), item.getBuyer() != null ? item.getBuyer().getId() : "null");

		if (item.getBuyer() == null || !item.getBuyer().getId().equals(buyerId)) {
			throw new AccessDeniedException("구매자만 결제를 완료할 수 있습니다.");
		}

		if (item.getStatus() != ItemSellStatus.TRADING) {
			throw new IllegalStateException("거래중인 상품만 결제를 완료할 수 있습니다.");
		}

		item.setStatus(ItemSellStatus.COMPLETED);
		item.setPurchasedAt(LocalDateTime.now());
		itemRepository.save(item);
		
		log.info("상품 상태 변경 완료 - 상품ID: {}, 새 상태: {}", itemId, item.getStatus());
	}

	// 관리자 상품 삭제 (권한 전용 간편 메서드)
	@Transactional
	@PreAuthorize("hasRole('ADMIN')")
	public void deleteItem(Long itemId) {
		Item item = itemRepository.findById(itemId)
				.orElseThrow(() -> new EntityNotFoundException("상품이 없습니다."));
		itemRepository.delete(item);
	}

	// 상품 수정 폼 조회
	@Transactional
	@PreAuthorize("hasRole('ADMIN') or hasRole('USER')") // 관리자 또는 일반 사용자만 접근 가능
	public ItemUpdateDto getItemUpdateForm(Long itemId, Long memberId,
			Collection<? extends GrantedAuthority> authorities) {
		// 상품 조회
		Item item = itemRepository.findById(itemId).orElseThrow(() -> new EntityNotFoundException("상품이 존재하지 않습니다."));

		// 현재 사용자가 관리자 권한이 있는지 확인
		boolean isAdmin = authorities.stream().anyMatch(auth -> auth.getAuthority().equals("ROLE_ADMIN"));

		if (isAdmin) {
			// 관리자면 권한 검사 없이 진행
		} else {
			// 관리자가 아니면, memberId와 상품 작성자 ID를 비교하여 본인 상품인지 확인
			if (memberId == null || !item.getSeller().getId().equals(memberId)) {
				throw new AccessDeniedException("해당 상품에 대한 수정 권한이 없습니다.");
			}
		}

		Category child = item.getCategoryId();
		Category parent = child.getParent();

//		// 상품 정보를 DTO로 변환해 반환

		ItemUpdateDto dto = new ItemUpdateDto();

		dto.setId(item.getId());
		dto.setName(item.getName());
		dto.setDescription(item.getDescription());
		dto.setPrice(item.getPrice());
		dto.setRegion(item.getRegion());

		// 카테고리 ID 분리
		if (item.getCategoryId() != null) {
			dto.setChildCategoryId(item.getCategoryId().getId());
			if (item.getCategoryId().getParent() != null) {
				dto.setParentCategoryId(item.getCategoryId().getParent().getId());
			}
		}

		// 이미지 URL과 이미지 ID 매핑
		List<ItemImg> itemImgs = item.getImages();
		if (itemImgs != null && !itemImgs.isEmpty()) {
			StringBuilder urls = new StringBuilder();
			List<Long> imgIds = new ArrayList<>();

			for (ItemImg img : itemImgs) {
				if (img.getImgUrl() != null) {
					urls.append(img.getImgUrl()).append(",");
				}
				imgIds.add(img.getId());
			}

			// 마지막 쉼표 제거
			if (urls.length() > 0) {
				urls.setLength(urls.length() - 1);
			}

			dto.setImageUrls(urls.toString());
			dto.setImgIds(imgIds);
		}

		return dto;

//		return ItemUpdateDto.builder().id(item.getId()).name(item.getName()).description(item.getDescription())
//				.price(item.getPrice()).region(item.getRegion())
//				.parentCategoryId(parent != null ? parent.getId() : null).childCategoryId(child.getId())
//				.imgIds(item.getImages().stream().map(ItemImg::getId).toList()).build();
	}

	// 상품 수정 처리
	@Transactional
	public void updateItem(ItemUpdateDto dto, Long memberId, Collection<? extends GrantedAuthority> authorities)
			throws IOException {
		// 상품 조회
		Item item = itemRepository.findById(dto.getId()).orElseThrow(() -> new EntityNotFoundException("상품이 없습니다."));

		// 관리자 권한 체크
		boolean isAdmin = authorities.stream().anyMatch(auth -> auth.getAuthority().equals("ROLE_ADMIN"));

		if (!isAdmin) {
			// 관리자 아니면 본인 상품인지 확인
			if (memberId == null || !item.getSeller().getId().equals(memberId)) {
				throw new AccessDeniedException("해당 상품에 대한 수정 권한이 없습니다.");
			}
		}

		// 카테고리 조회 및 검증
		Category child = categoryRepository.findById(dto.getChildCategoryId())
				.orElseThrow(() -> new EntityNotFoundException("자식 카테고리가 없습니다."));

		Category parent = categoryRepository.findById(dto.getParentCategoryId())
				.orElseThrow(() -> new EntityNotFoundException("부모 카테고리가 없습니다."));

		if (child.getParent() == null || !child.getParent().getId().equals(parent.getId())) {
			throw new IllegalArgumentException("부모 카테고리와 자식 카테고리가 일치하지 않습니다.");
		}

		// 상품 정보 업데이트
		item.setName(dto.getName());
		item.setDescription(dto.getDescription());
		item.setPrice(dto.getPrice());
		item.setRegion(dto.getRegion());
		item.setCategoryId(child);

		// 이미지 수정 처리
		List<Long> imgIds = dto.getImgIds() != null ? dto.getImgIds() : List.of();
		List<ItemImg> currentImgs = item.getImages();

		// 삭제된 이미지 처리
		currentImgs.stream().filter(img -> !imgIds.contains(img.getId())).toList().forEach(img -> {
			itemImgService.deleteItemImg(img.getId());
			item.getImages().remove(img);
		});

		List<MultipartFile> newFiles = dto.getImages() != null ? dto.getImages() : List.of();

		// 기존 이미지 업데이트
		int i = 0;
		for (; i < imgIds.size() && i < newFiles.size(); i++) {
			MultipartFile file = newFiles.get(i);
			if (file != null && !file.isEmpty()) {
				itemImgService.updateItemImg(imgIds.get(i), file);
			}
		}

		// 새로운 이미지 추가
		for (; i < newFiles.size(); i++) {
			MultipartFile file = newFiles.get(i);
			if (file != null && !file.isEmpty()) {
				ItemImg newImg = itemImgService.uploadAndSaveItemImg(file, item);
				item.getImages().add(newImg);
			}
		}
	}

	// 상품 삭제
	@Transactional
	@PreAuthorize("hasRole('ADMIN') or hasRole('USER')")
	public void deleteItem(Long itemId, Long memberId, Collection<? extends GrantedAuthority> authorities) {

		// 상품 조회
		Item item = itemRepository.findById(itemId).orElseThrow(() -> new EntityNotFoundException("상품이 없습니다."));

		// 현재 사용자가 관리자 권한을 가지고 있는지 확인
		boolean isAdmin = authorities.stream().anyMatch(auth -> auth.getAuthority().equals("ROLE_ADMIN"));

		// 관리자가 아니면 본인 상품인지 검사
		if (!isAdmin) {
			if (memberId == null || !item.getSeller().getId().equals(memberId)) {
				throw new AccessDeniedException("해당 상품에 대한 삭제 권한이 없습니다.");
			}
		}

		// 상품 삭제
		itemRepository.delete(item);
	}

	// DTO 변환
	private ItemCardDto toItemCardDto(Item item) {
		String thumbnail = item.getImages().isEmpty() ? null : item.getImages().get(0).getThumbnailUrl();
		return ItemCardDto.builder().itemId(item.getId()).name(item.getName()).thumbnailUrl(thumbnail)
				.price(item.getPrice()).status(item.getStatus()).created(item.getCreated()).build();
	}

	private ItemListDto toItemListDto(Item item) {
		return ItemListDto.builder().id(String.valueOf(item.getId())).name(item.getName()).price(item.getPrice())
				.status(item.getStatus()).created(item.getCreated()).build();
	}
}
