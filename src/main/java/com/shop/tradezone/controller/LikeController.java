package com.shop.tradezone.controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.shop.tradezone.service.LikeService;
import com.shop.tradezone.service.MemberService;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/item")
public class LikeController {

	private final LikeService likeService;
//	private final ItemService itemService; // 아이템 조회용
	private final MemberService memberService; // 회원 조회용

	/** 좋아요 토글 (ON/OFF) */
//	@PreAuthorize("isAuthenticated()")
//	@PostMapping("/{itemId}/like")
//	public ResponseEntity<?> toggle(@PathVariable("itemId") Long itemId, @AuthenticationPrincipal User userPrincipal) {
//
//		// 1) 엔티티 조회
//		Item item = itemService.getItem(itemId);
//		Member member = memberService.getMemberByEmail(userPrincipal.getUsername());
//
//		// 2) 토글 실행
//		boolean liked = likeService.toggle(item, member);
//
//		// 3) 최신 카운트 조회
//		long count = likeService.countLikes(item);
//
//		// 4) 응답(JSON) — 프론트가 바로 UI 반영 가능
//		return ResponseEntity.ok(Map.of("liked", liked, // true면 채워진 하트, false면 빈 하트
//				"count", count // 최신 찜 개수
//		));
//	}

	/** 현재 상태/카운트 조회 (비로그인도 가능) */
//	@GetMapping("/{itemId}/like")
//	public ResponseEntity<?> getState(@PathVariable("itemId") Long itemId,
//			@AuthenticationPrincipal User userPrincipal) {
//
//		Item item = itemService.getItem(itemId);
//		long count = likeService.countLikes(item);
//
//		boolean liked = false;
//		if (userPrincipal != null) {
//			Member member = memberService.getMemberByEmail(userPrincipal.getUsername());
//			liked = likeService.isLiked(item, member);
//		}
//
//		return ResponseEntity.ok(Map.of("liked", liked, "count", count));
//	}

}