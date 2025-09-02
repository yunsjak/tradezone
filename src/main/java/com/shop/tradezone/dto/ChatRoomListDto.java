package com.shop.tradezone.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 채팅방 리스트에 표시할 데이터 DTO 구매자/판매자 모두 공용
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChatRoomListDto {
	private Long roomId;
	private String partnerName;
	private String lastMessage;
	private java.time.LocalDateTime lastMessageAt;
	private int unreadCount;
}