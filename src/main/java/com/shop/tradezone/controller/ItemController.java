package com.shop.tradezone.controller;

import java.io.IOException;
import java.util.Collection;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.shop.tradezone.dto.ItemCardDto;
import com.shop.tradezone.dto.ItemDetailDto;
import com.shop.tradezone.dto.ItemFormDto;
import com.shop.tradezone.dto.ItemUpdateDto;
import com.shop.tradezone.dto.ReviewFormDto;
import com.shop.tradezone.entity.Category;
import com.shop.tradezone.entity.Item;
import com.shop.tradezone.entity.Review;
import com.shop.tradezone.repository.ItemRepository;
import com.shop.tradezone.service.CategoryService;
import com.shop.tradezone.service.ItemService;
import com.shop.tradezone.service.MemberPrincipal;
import com.shop.tradezone.service.ReviewService;

import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Controller
@RequiredArgsConstructor
@RequestMapping("/items")
public class ItemController {

	private final ItemService itemService;
	private final ItemRepository itemRepository;
	private final CategoryService categoryService;
	private final ReviewService reviewService;

	// 메인 최근 상품
	@GetMapping("/main")
	public String mainPage(Model model) {
		// 0페이지, 12개 상품 중 판매중(SELL) 상태인 상품만 가져옴
		Page<ItemCardDto> recentItemsPage = itemService.getMainItems(0, 12);
		model.addAttribute("recentItems", recentItemsPage.getContent()); // 상품 리스트
		model.addAttribute("paging", recentItemsPage); // 페이징 정보

		return "main"; // main.html 뷰 렌더링
	}

//	@GetMapping("/my")
//	public String getMyItems(@RequestParam Long memberId, @RequestParam(defaultValue = "0") int page,
//			@RequestParam(defaultValue = "12") int size, Model model) {
//
//		Page<?> items = itemService.getItemsByMember(memberId, page, size);
//		model.addAttribute("items", items.getContent());
//		model.addAttribute("paging", items);
//		return "mypage";
//	}

	// 카테고리별 상품 상세 조회
	@GetMapping("/category/{parentId}/{childId}")
	public String getItemsByCategory(@PathVariable("parentId") Long parentId, @PathVariable("childId") Long childId,
			@RequestParam(name = "page", defaultValue = "0") int page, Model model) {

		Pageable pageable = PageRequest.of(page, 10);
		Page<ItemCardDto> items = itemService.getItemsByCategory(parentId, childId, pageable);

		model.addAttribute("items", items);
		model.addAttribute("paging", items);

		model.addAttribute("parentName", categoryService.getCategoryNameById(parentId));
		model.addAttribute("childName", categoryService.getCategoryNameById(childId));
		model.addAttribute("childId", childId);

		return "category"; // 카테고리별 상품 리스트 뷰
	}

	// 상품 상세 조회
	@GetMapping("/detail/{id}")
	public String detailPage(@PathVariable("id") Long id, Model model,
			@AuthenticationPrincipal MemberPrincipal memberPrincipal) {
		// 1) 아이템 조회
		ItemDetailDto itemDetail = itemService.getItemDetail(id, null);
		model.addAttribute("item", itemDetail);

		// 2) 로그인한 사용자가 판매자인지 체크
		boolean isSeller = false;
		if (memberPrincipal != null) {
			String loginUser = memberPrincipal.getUsername();
			isSeller = loginUser.equalsIgnoreCase(itemDetail.getSellerName());
		}
		model.addAttribute("isSeller", isSeller);

		// 3) Item 엔티티 직접 조회 (필요하다면 itemRepository 주입 필요)
		Item item = itemRepository.findById(id).orElseThrow(() -> new EntityNotFoundException("상품이 존재하지 않습니다."));

		// 4) 기존 서비스 메서드로 리뷰 리스트 조회
		List<Review> reviews = reviewService.getByItem(item);

		// 5) Review -> ReviewFormDto 변환
		List<ReviewFormDto> reviewDtos = reviews.stream().map(ReviewFormDto::new).toList();

		// 6) 모델에 리뷰 목록 추가
		model.addAttribute("reviews", reviewDtos);

		// 7) 빈 리뷰 작성 폼 객체 바인딩
		model.addAttribute("reviewForm", new ReviewFormDto());

		return "item";
	}

	// 상품 등록 폼 조회
	@PreAuthorize("isAuthenticated()")
	@GetMapping("/new")
	public String showCreateForm(Model model) {
		model.addAttribute("itemFormDto", new ItemFormDto());

		// 부모 카테고리 리스트 세팅
		List<Category> parents = categoryService.findParentCategoriesWithChildren();
		model.addAttribute("parents", parents);

		// 기본 자식 카테고리 (첫 부모의 자식들)
		Long defaultParentId = parents.isEmpty() ? null : parents.get(0).getId();
		List<Category> children = (defaultParentId == null) ? List.of()
				: categoryService.findChildrenByParentId(defaultParentId);
		model.addAttribute("children", children);

		return "register"; // 등록 폼 뷰 이름
	}

	// 상품 등록 처리
	@PreAuthorize("isAuthenticated()")
	@PostMapping("/new")
	public String createItem(@Valid @ModelAttribute ItemFormDto itemFormDto, BindingResult bindingResult, Model model,
			@AuthenticationPrincipal MemberPrincipal memberPrincipal) throws IOException {

		log.info("📥 POST /new 요청 진입");
		log.info("상품명: {}", itemFormDto.getName());

		if (bindingResult.hasErrors()) {
			model.addAttribute("itemFormDto", itemFormDto);
			// 일관성 위해 부모/자식 리스트 다시 넣기
			List<Category> parents = categoryService.findParentCategoriesWithChildren();
			Long defaultParentId = parents.isEmpty() ? null : parents.get(0).getId();
			List<Category> children = defaultParentId == null ? List.of()
					: categoryService.findChildrenByParentId(defaultParentId);
			model.addAttribute("parents", parents);
			model.addAttribute("children", children);
			return "register";
		}

		Long realParentId = categoryService.getParentIdByChildId(itemFormDto.getChildCategoryId());
		if (!realParentId.equals(itemFormDto.getParentCategoryId())) {
			bindingResult.rejectValue("childCategoryId", "error.itemFormDto", "선택한 항목이 부모 카테고리와 일치하지 않습니다.");
			model.addAttribute("itemFormDto", itemFormDto);

			// 다시 부모/자식 리스트 넣기
			List<Category> parents = categoryService.findParentCategoriesWithChildren();
			Long defaultParentId = parents.isEmpty() ? null : parents.get(0).getId();
			List<Category> children = defaultParentId == null ? List.of()
					: categoryService.findChildrenByParentId(defaultParentId);
			model.addAttribute("parents", parents);
			model.addAttribute("children", children);
			return "register";
		}

		log.info("parentCategoryId: {}", itemFormDto.getParentCategoryId());
		log.info("childCategoryId: {}", itemFormDto.getChildCategoryId());

		// TODO: 로그인한 회원 ID 받아와서 넣기 (현재 하드코딩)
		Long userId = memberPrincipal.getMemberId();
		itemService.createItem(itemFormDto, userId);

		return "redirect:/items/main";
	}

	// 상품 수정 폼 조회
	@PreAuthorize("isAuthenticated()")
	@GetMapping("/edit/{id}")
	public String editItemForm(@AuthenticationPrincipal MemberPrincipal memberPrincipal, @PathVariable("id") Long id,
			Model model) {

		Long memberId = memberPrincipal.getMemberId();
		Collection<? extends GrantedAuthority> authorities = memberPrincipal.getAuthorities();

		ItemUpdateDto updateDto = itemService.getItemUpdateForm(id, memberId, authorities);

		Long childId = updateDto.getChildCategoryId();
		Long parentId = categoryService.getParentIdByChildId(childId);

		model.addAttribute("itemUpdateDto", updateDto);
		model.addAttribute("categoryTree", categoryService.getCategoryTree());
		model.addAttribute("selectedParentId", parentId);
		model.addAttribute("selectedChildId", childId);

		return "admin/item_form"; // 동일한 공통 폼 뷰 사용
	}

	// 상품 수정 처리
	@PreAuthorize("isAuthenticated()")
	@PostMapping("/edit/{id}")
	public String updateItem(@AuthenticationPrincipal MemberPrincipal memberPrincipal, @PathVariable("id") Long id,
			@Valid @ModelAttribute ItemUpdateDto updateDto, BindingResult bindingResult, Model model)
			throws IOException {

		Long memberId = memberPrincipal.getMemberId();
		Collection<? extends GrantedAuthority> authorities = memberPrincipal.getAuthorities();

		if (bindingResult.hasErrors()) {
			model.addAttribute("categoryTree", categoryService.getCategoryTree());
			return "admin/item_form";
		}

		itemService.updateItem(updateDto, memberId, authorities);

		return "redirect:/items/detail/" + id; // 일반 사용자 상세 페이지
	}

	// 상품 삭제
	@PreAuthorize("isAuthenticated()")
	@PostMapping("/delete/{id}")
	public String deleteItem(@AuthenticationPrincipal MemberPrincipal memberPrincipal, @PathVariable("id") Long id) {

		Long memberId = memberPrincipal.getMemberId();
		Collection<? extends GrantedAuthority> authorities = memberPrincipal.getAuthorities();

		itemService.deleteItem(id, memberId, authorities);

		return "redirect:/items/main"; // 일반 사용자 메인 페이지
	}
}
