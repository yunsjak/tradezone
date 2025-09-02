package com.shop.tradezone.controller;

import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.shop.tradezone.entity.Item;
import com.shop.tradezone.entity.Member;
import com.shop.tradezone.service.MemberPrincipal;
import com.shop.tradezone.service.ItemService;
import com.shop.tradezone.service.LikeService;
import com.shop.tradezone.service.MemberService;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/item")
public class LikeController {

	private final LikeService likeService;
	private final ItemService itemService; // 아이템 조회용
	private final MemberService memberService; // 회원 조회용

	/** 좋아요 토글 (ON/OFF) */
	@PreAuthorize("isAuthenticated()")
	@PostMapping("/{itemId}/like")
	public ResponseEntity<?> toggle(@PathVariable("itemId") Long itemId, @AuthenticationPrincipal MemberPrincipal memberPrincipal) {

		// 1) 엔티티 조회
		Item item = itemService.getItemById(itemId);
		Member member = memberService.findByEmail(memberPrincipal.getUsername());

		// 2) 토글 실행
		boolean liked = likeService.toggle(item, member);

		// 3) 최신 카운트 조회
		long count = likeService.countLikes(item);

		// 4) 응답(JSON) — 프론트가 바로 UI 반영 가능
		return ResponseEntity.ok(Map.of("liked", liked, // true면 채워진 하트, false면 빈 하트
				"count", count // 최신 찜 개수
		));
	}
}