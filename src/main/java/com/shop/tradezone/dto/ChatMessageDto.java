package com.shop.tradezone.dto;

import java.time.LocalDateTime;

import com.shop.tradezone.entity.ChatMessage;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

// 채팅 메시지 페이로드 DTO 
// 역할: - 서버 ↔ 클라이언트 간 전송되는 "데이터" 만을 담는다.

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ChatMessageDto {

	private Long id; // 메시지 PK
	private Long roomId; // 채팅방 ID
	private Long senderId; // 보낸 사람 ID
	private String content; // 메시지 내용
	private String type; // TEXT / IMAGE / NOTICE 등
	private LocalDateTime createdAt; // 작성 시각

	// ✅ 엔티티 -> DTO 변환 생성자
	public ChatMessageDto(ChatMessage e) {
		this.id = e.getId();
		this.roomId = e.getChatRoom().getId();
		this.senderId = (e.getSender() != null ? e.getSender().getId() : null);
		this.content = e.getContent();
		this.type = e.getType();
		this.createdAt = e.getCreatedAt();
	}

}