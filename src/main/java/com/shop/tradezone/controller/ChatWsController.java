package com.shop.tradezone.controller;

import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;

import com.shop.tradezone.service.ChatMessageService;
import com.shop.tradezone.service.MemberPrincipal;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;

/**
 * 클라이언트: /app/chat.send 로 전송 서버 브로드캐스트: /topic/chat.room.{roomId}
 */
@Controller
@RequiredArgsConstructor
public class ChatWsController {

	private final ChatMessageService chatMessageService;

	public record SendPayload(Long roomId, Long senderId, String content) {
	}

	public record SystemPayload(Long roomId, String content) {
	}

	// 클라이언드에서 보내는 페이로드?
	public record SendMessageReq(@NotNull Long roomId, @NotBlank String content) {
	}

	// 메세지 전송
	@MessageMapping("/chat.send")
	public void send(@Payload SendPayload payload, @AuthenticationPrincipal MemberPrincipal principal) {
		chatMessageService.sendText(payload.roomId(), principal.getMemberId(), payload.content());
	}
}
