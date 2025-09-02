package com.shop.tradezone.service;

import java.time.LocalDateTime;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.shop.tradezone.entity.Member;
import com.shop.tradezone.entity.Trade;
import com.shop.tradezone.repository.TradeRepository;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class TradeApprovalService {

	private final TradeRepository tradeRepository;

	private Trade get(Long id) {
		return tradeRepository.findById(id).orElseThrow(() -> new EntityNotFoundException("Trade not found: " + id));
	}

	private void ensureParticipant(Trade t, Member actor) {
		if (actor == null || !t.isParticipant(actor.getId())) {
			throw new AccessDeniedException("거래 참여자만 상태 변경이 가능합니다.");
		}
	}

	// -------- 완료(성사) -----------
	@Transactional
	public Trade requestComplete(Long tradeId, Member actor) {
		Trade t = get(tradeId);
		ensureParticipant(t, actor);
		t.requestComplete(actor, LocalDateTime.now());
		return t;
	}

	@Transactional
	public Trade approveComplete(Long tradeId, Member actor) {
		Trade t = get(tradeId);
		ensureParticipant(t, actor);
		t.approveComplete(actor, LocalDateTime.now());
		return t;
	}

	@Transactional
	public Trade rejectComplete(Long tradeId, Member actor) {
		Trade t = get(tradeId);
		ensureParticipant(t, actor);
		t.rejectComplete(actor);
		return t;
	}

	// -------- 취소(무산) -----------
	@Transactional
	public Trade requestCancel(Long tradeId, Member actor, String reason) {
		Trade t = get(tradeId);
		ensureParticipant(t, actor);
		t.requestCancel(actor, reason, LocalDateTime.now());
		return t;
	}

	@Transactional
	public Trade approveCancel(Long tradeId, Member actor) {
		Trade t = get(tradeId);
		ensureParticipant(t, actor);
		t.approveCancel(actor, LocalDateTime.now());
		return t;
	}

	@Transactional
	public Trade rejectCancel(Long tradeId, Member actor) {
		Trade t = get(tradeId);
		ensureParticipant(t, actor);
		t.rejectCancel(actor);
		return t;
	}

	// -------- roomId 스푸핑 방지용 헬퍼 -----------
	@Transactional(readOnly = true)
	public Long getRoomIdOfTrade(Long tradeId) {
		return tradeRepository.findRoomIdByTradeId(tradeId)
				.orElseThrow(() -> new EntityNotFoundException("Trade not found or chat room not linked: " + tradeId));
	}
}
