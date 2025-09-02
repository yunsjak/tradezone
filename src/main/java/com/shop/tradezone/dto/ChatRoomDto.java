package com.shop.tradezone.dto;

import java.time.LocalDateTime;

import com.shop.tradezone.entity.ChatRoom;

import lombok.Builder;
import lombok.Getter;

// 채팅방 응답 DTO

@Getter
@Builder
public class ChatRoomDto {

	private Long id; // 방 ID
	private Long itemId; // 연결된 아이템 ID
	private String itemName; // Item.name
	private Long userAId; // 참여자 A ID
	private Long userBId; // 참여자 B ID
	private LocalDateTime lastMessageAt; // 최근 메시지 시각
	private String type;

	public static ChatRoomDto from(ChatRoom r) {
		return ChatRoomDto.builder().id(r.getId()).itemId(r.getItem().getId()).itemName(r.getItem().getName()) // Item
																												// 엔티티에
																												// name
																												// 필드 사용
				.userAId(r.getUserA().getId()).userBId(r.getUserB().getId()).lastMessageAt(r.getLastMessageAt())
				.type("TEXT") // or 최근 메시지 타입에 맞게 채워주기
				.build();
	}
}
