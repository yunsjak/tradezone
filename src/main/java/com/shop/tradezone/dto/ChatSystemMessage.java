package com.shop.tradezone.dto;

import java.time.LocalDateTime;

import lombok.*;

/** 채팅창에 뿌리는 간단한 시스템 메시지 DTO */
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class ChatSystemMessage {
    private Long roomId;             // 채팅방 ID
    private String type;             // "SYSTEM"
    private String content;          // 메시지 내용
    private LocalDateTime createdAt; // 생성 시각

    /** 편의 팩토리 */
    public static ChatSystemMessage system(Long roomId, String content){
        return ChatSystemMessage.builder()
                .roomId(roomId)
                .type("SYSTEM")
                .content(content)
                .createdAt(LocalDateTime.now())
                .build();
    }
}