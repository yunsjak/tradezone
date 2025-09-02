package com.shop.tradezone.controller;

import java.util.List;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.shop.tradezone.dto.ChatRoomListDto;
import com.shop.tradezone.service.ChatRoomService;
import com.shop.tradezone.service.MemberPrincipal;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/chat/rooms")
@RequiredArgsConstructor
public class ChatRoomRestController {

	private final ChatRoomService chatRoomService;

	@GetMapping("/my")
	public List<ChatRoomListDto> getMyRooms(@AuthenticationPrincipal MemberPrincipal principal) {
		return chatRoomService.getMyRooms(principal.getMemberId());
	}
}
