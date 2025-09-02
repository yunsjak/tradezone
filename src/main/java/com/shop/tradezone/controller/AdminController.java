package com.shop.tradezone.controller;

import java.io.IOException;
import java.util.Collection;

import org.springframework.data.domain.Page;
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
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.shop.tradezone.constant.Role;
import com.shop.tradezone.dto.ItemUpdateDto;
import com.shop.tradezone.dto.MemberUpdateDto;
import com.shop.tradezone.entity.Item;
import com.shop.tradezone.entity.Member;
import com.shop.tradezone.entity.Review;
import com.shop.tradezone.service.CategoryService;
import com.shop.tradezone.service.ItemService;
import com.shop.tradezone.service.MemberPrincipal;
import com.shop.tradezone.service.MemberService;
import com.shop.tradezone.service.ReviewService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@Controller
@RequiredArgsConstructor
@RequestMapping("/admin")
@PreAuthorize("hasRole('ADMIN')")
public class AdminController {

	private final MemberService memberService;

	private final ItemService itemService;

	private final ReviewService reviewService;

	private final CategoryService categoryService;

	// 관리자페이지
	@GetMapping("/adminPage")
	public String adminPage() {
		return "admin/common";
	}

	// =================== 회원관리 ===================

	// 회원목록 조회
	@GetMapping("/users")
	public String memberList(Model model, @RequestParam(value = "page", defaultValue = "0") int page) {

		Page<Member> members = memberService.getAllMembers(page);
		model.addAttribute("members", members);

		return "admin/admin_user";
	}

	// 회원정보 수정
	@GetMapping("/users/edit/{id}")
	public String editMember(@PathVariable("id") Long id, Model model) {

		MemberUpdateDto dto = memberService.getMemberById(id);

		model.addAttribute("memberUpdateDto", dto);

		return "admin/user_form";
	}

	@PostMapping("/users/edit/{id}")
	public String editMember(@Valid MemberUpdateDto dto, BindingResult bindingResult, @PathVariable("id") Long id,
			RedirectAttributes redirectAttributes) {

		if (bindingResult.hasErrors()) {
			return "admin/user_form";
		}

		memberService.updateMember(dto, id);

		return "redirect:/admin/users";
	}

	// 회원 삭제
	@PostMapping("/users/delete/{id}")
	public String deleteMember(@PathVariable("id") Long id) {

		memberService.deleteMember(id);

		return "redirect:/admin/users";
	}

	// =================== 상품관리 ===================

	// 상품목록 조회
	@GetMapping("/items")
	public String itemList(Model model, @RequestParam(value = "page", defaultValue = "0") int page) {

		Page<Item> items = itemService.getAllItems(page);
		model.addAttribute("items", items);

		return "admin/admin_item";
	}

	// 상품 수정 폼 조회 (관리자 전용)
	@GetMapping("/items/edit/{id}")
	public String editItemForm(@AuthenticationPrincipal MemberPrincipal memberPrincipal, @PathVariable Long id,
			Model model) {

		Long memberId = memberPrincipal.getMemberId();
		Collection<? extends GrantedAuthority> authorities = memberPrincipal.getAuthorities();

		// 관리자 권한 체크 (필요하면 서비스에서 한번 더 체크)
		boolean isAdmin = authorities.stream().anyMatch(auth -> auth.getAuthority().equals(Role.ADMIN.getValue()));

		if (!isAdmin) {
			return "redirect:/access-denied"; // 권한 없으면 접근 차단 페이지로 이동
		}

		ItemUpdateDto updateDto = itemService.getItemUpdateForm(id, memberId, authorities);

		Long childId = updateDto.getChildCategoryId();
		Long parentId = categoryService.getParentIdByChildId(childId);

		model.addAttribute("itemUpdateDto", updateDto);
		model.addAttribute("categoryTree", categoryService.getCategoryTree());
		model.addAttribute("selectedParentId", parentId);
		model.addAttribute("selectedChildId", childId);

		return "admin/item_form"; // 공통 폼 뷰 사용
	}

	// 상품 수정 처리 (관리자 전용)
	@PostMapping("/items/edit/{id}")
	public String updateItem(@AuthenticationPrincipal MemberPrincipal memberPrincipal, @PathVariable Long id,
			@Valid @ModelAttribute ItemUpdateDto updateDto, BindingResult bindingResult, Model model)
			throws IOException {

		Long memberId = memberPrincipal.getMemberId();
		Collection<? extends GrantedAuthority> authorities = memberPrincipal.getAuthorities();

		boolean isAdmin = authorities.stream().anyMatch(auth -> auth.getAuthority().equals(Role.ADMIN.getValue()));

		if (!isAdmin) {
			return "redirect:/access-denied";
		}

		if (bindingResult.hasErrors()) {
			model.addAttribute("categoryTree", categoryService.getCategoryTree());
			return "admin/item_form";
		}

		itemService.updateItem(updateDto, memberId, authorities);

		return "redirect:/admin/items"; // 관리자 상품 목록 페이지
	}

	// 상품 삭제
	@PostMapping("/items/delete/{id}")
	public String deleteItem(@AuthenticationPrincipal MemberPrincipal memberPrincipal, @PathVariable("id") Long id) {

		Long memberId = memberPrincipal.getMemberId();
		Collection<? extends GrantedAuthority> authorities = memberPrincipal.getAuthorities();

		boolean isAdmin = authorities.stream().anyMatch(auth -> auth.getAuthority().equals(Role.ADMIN.getValue()));

		if (!isAdmin) {
			return "redirect:/access-denied";
		}

		itemService.deleteItem(id, memberId, authorities);

		return "redirect:/admin/items";
	}

	// =================== 댓글관리 ===================

	// 댓글목록 조회
	@GetMapping("/reviews")
	public String reviewList(Model model, @RequestParam(value = "page", defaultValue = "0") int page) {

		Page<Review> reviews = reviewService.getAllReviews(page);
		model.addAttribute("reviews", reviews);

		return "admin/admin_review";
	}

	// 댓글 삭제
	@PostMapping("reviews")
	public String deleteReview(@PathVariable("id") Long id) {

		reviewService.deletReview(id);

		return "admin/admin_review";
	}
}