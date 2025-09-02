package com.shop.tradezone.controller;

import org.springframework.messaging.handler.annotation.MessageExceptionHandler;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.messaging.simp.annotation.SendToUser;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;

import com.shop.tradezone.dto.ChatSystemMessage;
import com.shop.tradezone.dto.TradeActionDto;
import com.shop.tradezone.dto.TradeSignalDto;
import com.shop.tradezone.entity.Member;
import com.shop.tradezone.entity.Trade;
import com.shop.tradezone.service.TradeApprovalService;

import lombok.RequiredArgsConstructor;

@Controller
@RequiredArgsConstructor
public class TradeApprovalStompController {

	private final TradeApprovalService svc;
	private final SimpMessagingTemplate msg;

	// ---------- 완료(성사) ----------
	@MessageMapping("/trade.complete.request")
	public void requestComplete(TradeActionDto p, @AuthenticationPrincipal Member actor) {
		Trade t = svc.requestComplete(p.getTradeId(), actor);
		Long roomId = svc.getRoomIdOfTrade(p.getTradeId()); // ★ roomId 서버 재확인

		msg.convertAndSend("/topic/chat." + roomId,
				ChatSystemMessage.system(roomId, "[거래완료 요청] " + actor.getUsername() + "님이 거래 완료를 요청했습니다."));

		msg.convertAndSend("/topic/trade." + t.getId(),
				TradeSignalDto.builder().type("REQUEST_COMPLETE").tradeId(t.getId()).roomId(roomId) // ★ 서버 확정값
						.status(t.getStatus()).pendingType(t.getPendingType()).requesterId(actor.getId())
						.requesterName(actor.getUsername()).actorId(actor.getId()).actorName(actor.getUsername())
						.at(t.getRequestedAt()).build());
	}

	@MessageMapping("/trade.complete.approve")
	public void approveComplete(TradeActionDto p, @AuthenticationPrincipal Member actor) {
		Trade t = svc.approveComplete(p.getTradeId(), actor);
		Long roomId = svc.getRoomIdOfTrade(p.getTradeId());

		msg.convertAndSend("/topic/chat." + roomId,
				ChatSystemMessage.system(roomId, "[거래완료 승인] " + actor.getUsername() + "님이 거래 완료를 승인했습니다."));

		msg.convertAndSend("/topic/trade." + t.getId(),
				TradeSignalDto.builder().type("APPROVE_COMPLETE").tradeId(t.getId()).roomId(roomId)
						.status(t.getStatus()).pendingType(t.getPendingType()).actorId(actor.getId())
						.actorName(actor.getUsername()).at(t.getCompletedAt()).build());
	}

	@MessageMapping("/trade.complete.reject")
	public void rejectComplete(TradeActionDto p, @AuthenticationPrincipal Member actor) {
		Trade t = svc.rejectComplete(p.getTradeId(), actor);
		Long roomId = svc.getRoomIdOfTrade(p.getTradeId());

		msg.convertAndSend("/topic/chat." + roomId,
				ChatSystemMessage.system(roomId, "[거래완료 거절] " + actor.getUsername() + "님이 거래 완료 요청을 거절했습니다."));

		msg.convertAndSend("/topic/trade." + t.getId(),
				TradeSignalDto.builder().type("REJECT_COMPLETE").tradeId(t.getId()).roomId(roomId).status(t.getStatus())
						.pendingType(t.getPendingType()).actorId(actor.getId()).actorName(actor.getUsername()).build());
	}

	// ---------- 취소(무산) ----------
	@MessageMapping("/trade.cancel.request")
	public void requestCancel(TradeActionDto p, @AuthenticationPrincipal Member actor) {
		Trade t = svc.requestCancel(p.getTradeId(), actor, p.getReason());
		Long roomId = svc.getRoomIdOfTrade(p.getTradeId());

		msg.convertAndSend("/topic/chat." + roomId, ChatSystemMessage.system(roomId, "[거래종료 요청] " + actor.getUsername()
				+ "님이 거래 종료(무산)를 요청했습니다." + (p.getReason() == null ? "" : " 사유: " + p.getReason())));

		msg.convertAndSend("/topic/trade." + t.getId(),
				TradeSignalDto.builder().type("REQUEST_CANCEL").tradeId(t.getId()).roomId(roomId).status(t.getStatus())
						.pendingType(t.getPendingType()).requesterId(actor.getId()).requesterName(actor.getUsername())
						.actorId(actor.getId()).actorName(actor.getUsername()).reason(t.getCanceledReason())
						.at(t.getRequestedAt()).build());
	}

	@MessageMapping("/trade.cancel.approve")
	public void approveCancel(TradeActionDto p, @AuthenticationPrincipal Member actor) {
		Trade t = svc.approveCancel(p.getTradeId(), actor);
		Long roomId = svc.getRoomIdOfTrade(p.getTradeId());

		msg.convertAndSend("/topic/chat." + roomId,
				ChatSystemMessage.system(roomId, "[거래종료 승인] " + actor.getUsername() + "님이 거래 종료(무산)를 승인했습니다."));

		msg.convertAndSend("/topic/trade." + t.getId(),
				TradeSignalDto.builder().type("APPROVE_CANCEL").tradeId(t.getId()).roomId(roomId).status(t.getStatus())
						.pendingType(t.getPendingType()).actorId(actor.getId()).actorName(actor.getUsername())
						.reason(t.getCanceledReason()).at(t.getEndedAt()).build());
	}

	@MessageMapping("/trade.cancel.reject")
	public void rejectCancel(TradeActionDto p, @AuthenticationPrincipal Member actor) {
		Trade t = svc.rejectCancel(p.getTradeId(), actor);
		Long roomId = svc.getRoomIdOfTrade(p.getTradeId());

		msg.convertAndSend("/topic/chat." + roomId,
				ChatSystemMessage.system(roomId, "[거래종료 거절] " + actor.getUsername() + "님이 거래 종료 요청을 거절했습니다."));

		msg.convertAndSend("/topic/trade." + t.getId(),
				TradeSignalDto.builder().type("REJECT_CANCEL").tradeId(t.getId()).roomId(roomId).status(t.getStatus())
						.pendingType(t.getPendingType()).actorId(actor.getId()).actorName(actor.getUsername()).build());
	}

	// ---------- STOMP 에러를 개인 큐로 전달 (UX 향상) ----------
	@MessageExceptionHandler({ IllegalStateException.class,
			org.springframework.security.access.AccessDeniedException.class })
	@SendToUser("/queue/errors")
	public String handleWsErrors(Exception ex) {
		return ex.getMessage();
	}
}
