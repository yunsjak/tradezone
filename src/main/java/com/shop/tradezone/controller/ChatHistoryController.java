package com.shop.tradezone.controller;

import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.shop.tradezone.dto.ChatMessageDto;
import com.shop.tradezone.service.ChatMessageService;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/chat")
public class ChatHistoryController {

	private final ChatMessageService chatMessageService;

	@GetMapping("/rooms/{roomId}/messages")
	public Page<ChatMessageDto> history(@PathVariable Long roomId, @RequestParam(defaultValue = "0") int page,
			@RequestParam(defaultValue = "30") int size) {
		return chatMessageService.getMessages(roomId, page, size);
	}
}
