package com.shop.tradezone.websocket;

import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

@Component
public class AuthChannelInterceptor implements ChannelInterceptor {

	@Override
	public Message<?> preSend(Message<?> message, MessageChannel channel) {
		StompHeaderAccessor acc = MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);
		if (acc == null)
			return message;

		if (StompCommand.CONNECT.equals(acc.getCommand())) {
			// 세션 기반 인증: SecurityContext에 이미 로그인 정보가 있음
			Authentication auth = SecurityContextHolder.getContext().getAuthentication();

			// 로그인 안 된 상태에서 소켓 연결을 막고 싶다면 여기서 검사
			if (auth == null || !auth.isAuthenticated())
				throw new IllegalStateException("Unauthenticated");

			if (auth != null) {
				acc.setUser(auth); // 이후 @MessageMapping 메서드의 Principal/Authentication 로 들어감
			}
		}
		return message;
	}
}
