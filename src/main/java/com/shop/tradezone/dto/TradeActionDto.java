package com.shop.tradezone.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TradeActionDto {
	private Long tradeId; // 거래 ID (STOMP에서 사용)
	private Long roomId; // 채팅방 ID (브로드캐스트 경로 계산용)
	private String reason; // 취소 요청 시 사유(선택)
}