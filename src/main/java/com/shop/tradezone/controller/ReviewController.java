package com.shop.tradezone.controller;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.User;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.shop.tradezone.dto.ReviewFormDto;
import com.shop.tradezone.entity.Item;
import com.shop.tradezone.entity.Member;
import com.shop.tradezone.entity.Review;
import com.shop.tradezone.repository.ItemRepository;
import com.shop.tradezone.service.MemberPrincipal;
import com.shop.tradezone.service.MemberService;
import com.shop.tradezone.service.ReviewService;

import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;

@Log4j2
@Controller
@RequiredArgsConstructor
@RequestMapping("/reviews")
public class ReviewController {

	private final ReviewService reviewService;
	private final MemberService memberService;
	private final ItemRepository itemRepository;

	// 상품 상세페이지에서 리뷰 출력
//	@GetMapping("/item/{itemId}")
//	public String getItemDetail(@PathVariable("itemId") Long itemId, Model model) {
//		Item item = itemRepository.findById(itemId).orElseThrow(() -> new EntityNotFoundException("상품이 존재하지 않습니다."));
//
//		List<Review> reviews = reviewService.getByItem(item);
//		model.addAttribute("reviews", reviews);
//
//		return "item"; // 상품 상세 페이지 뷰
//	}

	// 후기 작성
	@PostMapping("/item/create")
	public String create(@RequestParam("itemId") Long itemId, @RequestParam("content") String content,
			@AuthenticationPrincipal MemberPrincipal memberPrincipal) {
		Item item = itemRepository.findById(itemId).orElseThrow(() -> new EntityNotFoundException("상품이 존재하지 않습니다."));
		Member member = memberService.findByEmail(memberPrincipal.getEmail());

		reviewService.create(item, member, content);

		return "redirect:/items/detail/" + itemId;
	}

	// 후기 수정
	@PreAuthorize("isAuthenticated()")
	@PostMapping("/{reviewId}/edit")
	public String edit(@PathVariable("reviewId") Long reviewId, @Valid @ModelAttribute("reviewForm") ReviewFormDto form,
			BindingResult bindingResult, @AuthenticationPrincipal User userPrincipal, Model model) {

		Review review = reviewService.getById(reviewId);
		Item item = review.getItem();

		if (bindingResult.hasErrors()) {
			model.addAttribute("review", review);
			return "review/detail"; // 수정 폼 뷰 (필요에 따라 구현)
		}

		Member editor = memberService.findByEmail(userPrincipal.getUsername());
		reviewService.update(reviewId, editor, form.getContent());

		return "redirect:/items/detail" + item.getId();
	}

	// 후기 삭제
	@PreAuthorize("isAuthenticated()")
	@PostMapping("/{reviewId}/delete")
	public String delete(@PathVariable("reviewId") Long reviewId, @AuthenticationPrincipal User userPrincipal) {
		Member requester = memberService.findByEmail(userPrincipal.getUsername());
		Review review = reviewService.getById(reviewId);
		Long itemId = review.getItem().getId();

		reviewService.delete(reviewId, requester);
		return "redirect:/items/detail/" + itemId;
	}

	// 마이페이지 - 나의 후기 목록
	@PreAuthorize("isAuthenticated()")
	@GetMapping("/mypage/reviews")
	public String myReviewList(@AuthenticationPrincipal User userPrincipal, Model model) {
		Member member = memberService.findByEmail(userPrincipal.getUsername());
		List<Review> reviewList = reviewService.getReviewListByUser(member);

		List<ReviewFormDto> reviewDtos = reviewList.stream().map(ReviewFormDto::new).collect(Collectors.toList());

		model.addAttribute("reviewList", reviewDtos);
		return "mypage"; // 마이페이지 후기 목록 뷰
	}

}