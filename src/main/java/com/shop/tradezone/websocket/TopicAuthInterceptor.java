package com.shop.tradezone.websocket;

import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.SimpMessageType;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Component;

import com.shop.tradezone.entity.Member;
import com.shop.tradezone.repository.TradeRepository;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class TopicAuthInterceptor implements ChannelInterceptor {

    private final TradeRepository tradeRepository;

    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {
        StompHeaderAccessor acc = StompHeaderAccessor.wrap(message);

        if (SimpMessageType.SUBSCRIBE.equals(acc.getMessageType())) {
            String dest = acc.getDestination(); // /topic/chat.{roomId} or /topic/trade.{tradeId}

            Object principal = acc.getUser();
            if (principal == null) throw new AccessDeniedException("Unauthenticated");
            // 프로젝트 보안 컨벤션에 따라 Member 캐스팅 부분 조정 필요
            Long uid = ((Member) principal).getId();

            if (dest != null) {
                if (dest.startsWith("/topic/chat.")) {
                    Long roomId = parseSuffixId(dest, "/topic/chat.");
                    if (!tradeRepository.canAccessRoom(roomId, uid)) {
                        throw new AccessDeniedException("not allowed");
                    }
                } else if (dest.startsWith("/topic/trade.")) {
                    Long tradeId = parseSuffixId(dest, "/topic/trade.");
                    if (!tradeRepository.canAccessTrade(tradeId, uid)) {
                        throw new AccessDeniedException("not allowed");
                    }
                }
            }
        }
        return message;
    }

    private Long parseSuffixId(String dest, String prefix) {
        String s = dest.substring(prefix.length());
        return Long.valueOf(s);
    }
}
