package com.shop.tradezone.controller;

import java.io.IOException;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
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
import org.springframework.web.bind.annotation.ResponseBody;

import com.shop.tradezone.dto.ItemCardDto;
import com.shop.tradezone.dto.ItemDetailDto;
import com.shop.tradezone.dto.ItemFormDto;
import com.shop.tradezone.dto.ItemUpdateDto;
import com.shop.tradezone.dto.ReviewFormDto;
import com.shop.tradezone.entity.Category;
import com.shop.tradezone.entity.Item;
import com.shop.tradezone.repository.ItemRepository;
import com.shop.tradezone.service.CategoryService;
import com.shop.tradezone.service.ItemService;
import com.shop.tradezone.service.MemberPrincipal;
import com.shop.tradezone.service.ReviewService;

import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Log4j2
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

		model.addAttribute("parentId", parentId);
		model.addAttribute("parentName", categoryService.getCategoryNameById(parentId));
		model.addAttribute("childId", childId);
		model.addAttribute("childName", categoryService.getCategoryNameById(childId));

		return "category"; // 카테고리별 상품 리스트 뷰
	}

	// 상품 상세 조회
	@GetMapping("/detail/{id}")
	public String detailPage(@PathVariable("id") Long id, Model model,
			@AuthenticationPrincipal MemberPrincipal memberPrincipal) {

		Long memberId = memberPrincipal != null ? memberPrincipal.getMemberId() : null;
		ItemDetailDto itemDetail = itemService.getItemDetail(id, memberId);

		// 현재 사용자가 판매자인지 확인
		boolean isSeller = memberPrincipal != null && memberPrincipal.getMemberId().equals(itemDetail.getSellerId());

		model.addAttribute("item", itemDetail);
		model.addAttribute("isSeller", isSeller);
		model.addAttribute("isBuyer", itemDetail.isBuyer());
		model.addAttribute("reviews", itemDetail.getReviews());

		return "item";
	}

	// 구매하기
	@PostMapping("/buy/{id}")
	@PreAuthorize("isAuthenticated()")
	@ResponseBody
	public ResponseEntity<Map<String, String>> buyItem(@PathVariable("id") Long id,
			@AuthenticationPrincipal MemberPrincipal memberPrincipal) {
		try {
			itemService.buyItem(id, memberPrincipal.getMemberId());
			return ResponseEntity.ok(Map.of("message", "구매가 완료되었습니다."));
		} catch (Exception e) {
			return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
		}
	}

	// 거래중지
	@PostMapping("/stop-trade/{id}")
	@PreAuthorize("isAuthenticated()")
	@ResponseBody
	public ResponseEntity<Map<String, String>> stopTrade(@PathVariable("id") Long id,
			@AuthenticationPrincipal MemberPrincipal memberPrincipal) {
		try {
			itemService.stopTrade(id, memberPrincipal.getMemberId());
			return ResponseEntity.ok(Map.of("message", "거래가 중지되었습니다."));
		} catch (Exception e) {
			return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
		}
	}

	// 거래재개
	@PostMapping("/resume-trade/{id}")
	@PreAuthorize("isAuthenticated()")
	@ResponseBody
	public ResponseEntity<Map<String, String>> resumeTrade(@PathVariable("id") Long id,
			@AuthenticationPrincipal MemberPrincipal memberPrincipal) {
		try {
			itemService.resumeTrade(id, memberPrincipal.getMemberId());
			return ResponseEntity.ok(Map.of("message", "거래가 재개되었습니다."));
		} catch (Exception e) {
			return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
		}
	}

	// 결제 페이지 조회
	@GetMapping("/payment/{id}")
	@PreAuthorize("isAuthenticated()")
	public String showPaymentPage(@PathVariable("id") Long id, Model model,
			@AuthenticationPrincipal MemberPrincipal memberPrincipal) {
		try {
			ItemDetailDto itemDetail = itemService.getItemDetail(id, memberPrincipal.getMemberId());
			model.addAttribute("item", itemDetail);
			model.addAttribute("tossprice", itemDetail.getPrice());
			return "toss/payment";
		} catch (Exception e) {
			return "redirect:/items/" + id + "?error=payment";
		}
	}

	// 결제완료 처리
	@PostMapping("/complete-payment/{id}")
	@PreAuthorize("isAuthenticated()")
	@ResponseBody
	public ResponseEntity<Map<String, String>> completePayment(@PathVariable("id") Long id,
			@AuthenticationPrincipal MemberPrincipal memberPrincipal) {
		try {
					itemService.completePayment(id, memberPrincipal.getMemberId());
			return ResponseEntity.ok(Map.of("message", "결제가 완료되었습니다."));
		} catch (Exception e) {
			log.error("결제완료 처리 실패 - 상품ID: {}, 에러: {}", id, e.getMessage(), e);
			return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
		}
	}

	// 상품 등록 폼 조회
	@PreAuthorize("isAuthenticated()")
	@GetMapping("/new")
	public String showCreateForm(@RequestParam(value = "parentId", required = false) Long parentId,
			@RequestParam(value = "childId", required = false) Long childId, Model model) {
		
		ItemFormDto form = ItemFormDto.builder()
			.parentCategoryId(parentId)
			.childCategoryId(childId)
			.build();
		
		model.addAttribute("itemFormDto", form);

		// 부모 카테고리 리스트 세팅
		List<Category> parents = categoryService.findParentCategoriesWithChildren();
		model.addAttribute("parents", parents);

		// 자식 카테고리: parentId가 있으면 해당 부모의 자식들, 없으면 빈 리스트
		List<Category> children = List.of();
		if (parentId != null) {
			children = categoryService.findChildrenByParentId(parentId);
		}
		model.addAttribute("children", children);

		return "register"; // 등록 폼 뷰 이름
	}

	// 자식 카테고리 조회 API
	@GetMapping("/categories/children")
	@ResponseBody
	public List<Category> getChildrenByParent(@RequestParam("parentId") Long parentId) {
		return categoryService.findChildrenByParentId(parentId);
	}

	// 상품 등록 처리
	@PreAuthorize("isAuthenticated()")
	@PostMapping("/new")
	public String createItem(@Valid @ModelAttribute ItemFormDto itemFormDto, BindingResult bindingResult, Model model,
			@AuthenticationPrincipal MemberPrincipal memberPrincipal) throws IOException {

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

		// 부모 카테고리(teams)와 자식 카테고리(품목)를 분리하여 모델에 전달
		List<Category> parents = categoryService.findParentCategoriesWithChildren();
		List<Category> children = categoryService.findChildrenByParentId(parentId); // 해당 부모의 자식들만 로드
		
		model.addAttribute("parents", parents); // 부모 카테고리 목록
		model.addAttribute("children", children); // 자식 카테고리 목록

		model.addAttribute("id", id);
		model.addAttribute("itemUpdateDto", updateDto);
		model.addAttribute("categoryTree", categoryService.getCategoryTree());
		model.addAttribute("selectedParentName", parentId);
		model.addAttribute("selectedChildName", childId);

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
