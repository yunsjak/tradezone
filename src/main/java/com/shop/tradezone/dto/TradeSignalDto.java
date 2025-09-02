package com.shop.tradezone.dto;

import java.time.LocalDateTime;

import com.shop.tradezone.constant.TradePendingType;
import com.shop.tradezone.constant.TradeStatus;
import com.shop.tradezone.entity.Member;
import com.shop.tradezone.entity.Trade;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/** 서버 → 클라이언트 방송 페이로드 (상태/버튼 제어) */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TradeSignalDto {
	private String type; // REQUEST_*/APPROVE_*/REJECT_*
	private Long tradeId;
	private Long roomId;
	private TradeStatus status; // PENDING / COMPLETE_REQUESTED / ... / ENDED
	private TradePendingType pendingType; // NONE / COMPLETE / CANCEL
	private Long requesterId;
	private String requesterName;
	private Long actorId;
	private String actorName;
	private String reason;
	private LocalDateTime at;

	/** REST 편의 생성자 */
	public static TradeSignalDto of(String type, Trade t, Member actor, TradeActionDto body) {
		return TradeSignalDto.builder().type(type).tradeId(t.getId()).status(t.getStatus())
				.pendingType(t.getPendingType()).actorId(actor != null ? actor.getId() : null)
				.actorName(actor != null ? actor.getUsername() : null).reason(t.getCanceledReason())
				.at(LocalDateTime.now()).build();
	}
}