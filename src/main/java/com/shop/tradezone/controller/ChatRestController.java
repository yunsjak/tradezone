package com.shop.tradezone.controller;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.shop.tradezone.service.ChatMessageService;
import com.shop.tradezone.service.MemberPrincipal;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/chat")
@RequiredArgsConstructor
public class ChatRestController {

	private final ChatMessageService chatMessageService;

//	@GetMapping("/rooms/{roomId}/messages")
//	public Page<ChatMessageDto> history(@PathVariable Long roomId, @RequestParam(defaultValue = "0") int page,
//			@RequestParam(defaultValue = "30") int size) {
//		return chatMessageService.getMessages(roomId, page, size);
//	}

	@PostMapping("/rooms/{roomId}/read")
	public void markRead(@PathVariable Long roomId, @AuthenticationPrincipal MemberPrincipal principal) {
		chatMessageService.clearUnread(roomId, principal.getMemberId());
	}
}
