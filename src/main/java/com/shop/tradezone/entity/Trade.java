package com.shop.tradezone.entity;

import java.time.LocalDateTime;

import org.hibernate.annotations.Comment; // (선택) 컬럼 주석을 DDL에 남기고 싶을 때

import com.shop.tradezone.constant.TradePendingType;
import com.shop.tradezone.constant.TradeStatus;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import jakarta.persistence.Version;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "trade", indexes = { @Index(name = "idx_trade_chatroom", columnList = "chat_room_id"),
		@Index(name = "idx_trade_buyer", columnList = "buyer_id"),
		@Index(name = "idx_trade_seller", columnList = "seller_id"),
		@Index(name = "idx_trade_status", columnList = "status"),
		@Index(name = "idx_trade_pending_type", columnList = "pending_type") }, uniqueConstraints = {
				@UniqueConstraint(name = "uk_trade_chatroom", columnNames = { "chat_room_id" }) })
public class Trade {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "buyer_id")
	private Member buyer;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "seller_id")
	private Member seller;

	@OneToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "chat_room_id")
	private ChatRoom chatRoom; // ★ 방과 1:1 연결

	// ===== 상태(메인) =====
	@Enumerated(EnumType.STRING)
	@Column(name = "status", nullable = false, length = 24)
	private TradeStatus status = TradeStatus.PENDING; // 기본 진행

	// ===== 보조 상태(무슨 대기인지) =====
	@Enumerated(EnumType.STRING)
	@Column(name = "pending_type", nullable = false, length = 16)
	private TradePendingType pendingType = TradePendingType.NONE; // NONE/COMPLETE/CANCEL

	// ===== 타임스탬프 =====
	@Column(name = "completed_at")
	private LocalDateTime completedAt; // 최종 완료 시각

	@Column(name = "ended_at")
	private LocalDateTime endedAt; // 최종 무산 시각

	// 취소 사유 (선택 입력)
	@Comment("취소 사유(선택)") // (선택) DDL 주석
	@Column(name = "canceled_reason", length = 200)
	private String canceledReason;

	// ===== 요청 정보(대기 중임을 표현) =====
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "requested_by_id")
	private Member requestedBy; // 현재 대기 요청자(완료/취소 공용)

	@Column(name = "requested_at")
	private LocalDateTime requestedAt;

	// ===== 마지막 행위자 =====
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "last_action_by_id")
	private Member lastActionBy;

	// ===== 동시성 제어 =====
	@Version
	@Column(name = "version")
	private Long version;

	// ================== 유틸/검증 ==================
	public boolean isTerminal() {
		return status == TradeStatus.COMPLETED || status == TradeStatus.ENDED;
	}

	public boolean isPending() {
		return pendingType != TradePendingType.NONE;
	}

	public boolean isParticipant(Long memberId) {
		return (buyer != null && buyer.getId().equals(memberId)) || (seller != null && seller.getId().equals(memberId));
	}

	// ================== 전이 메서드 ==================
	// 완료 요청 → COMPLETE_REQUESTED
	public void requestComplete(Member actor, LocalDateTime now) {
		if (isTerminal() || isPending())
			throw new IllegalStateException("요청 불가 상태입니다.");
		this.status = TradeStatus.COMPLETE_REQUESTED;
		this.pendingType = TradePendingType.COMPLETE;
		this.requestedBy = actor;
		this.requestedAt = now;
		this.lastActionBy = actor;
	}

	// 완료 승인 → COMPLETED
	public void approveComplete(Member actor, LocalDateTime now) {
		if (this.status != TradeStatus.COMPLETE_REQUESTED || this.pendingType != TradePendingType.COMPLETE)
			throw new IllegalStateException("완료 대기 상태가 아닙니다.");
		if (requestedBy != null && requestedBy.getId().equals(actor.getId()))
			throw new IllegalStateException("요청 당사자는 승인할 수 없습니다.");
		this.status = TradeStatus.COMPLETED;
		this.completedAt = now;

		// 대기 흔적 정리
		this.pendingType = TradePendingType.NONE;
		this.requestedBy = null;
		this.requestedAt = null;
		this.lastActionBy = actor;
	}

	// 완료 거절 → 원상(여기서는 PENDING) 복귀
	public void rejectComplete(Member actor) {
		if (this.status != TradeStatus.COMPLETE_REQUESTED || this.pendingType != TradePendingType.COMPLETE)
			throw new IllegalStateException("완료 대기 상태가 아닙니다.");
		if (requestedBy != null && requestedBy.getId().equals(actor.getId()))
			throw new IllegalStateException("요청 당사자는 거절할 수 없습니다.");
		this.status = TradeStatus.PENDING;
		this.pendingType = TradePendingType.NONE;
		this.requestedBy = null;
		this.requestedAt = null;
		this.lastActionBy = actor;
	}

	// 취소 요청 → 상태는 PENDING 유지, pendingType=CANCEL 로 표현
	public void requestCancel(Member actor, String reason, LocalDateTime now) {
		if (isTerminal() || isPending())
			throw new IllegalStateException("요청 불가 상태입니다.");
		this.status = TradeStatus.PENDING; // 상태는 유지
		this.pendingType = TradePendingType.CANCEL; // "취소 대기"만 보조필드로 표현
		this.canceledReason = reason; // 대기 중 사유 미리 보관
		this.requestedBy = actor;
		this.requestedAt = now;
		this.lastActionBy = actor;
	}

	// 취소 승인 → ENDED
	public void approveCancel(Member actor, LocalDateTime now) {
		if (this.status != TradeStatus.PENDING || this.pendingType != TradePendingType.CANCEL)
			throw new IllegalStateException("취소 대기 상태가 아닙니다.");
		if (requestedBy != null && requestedBy.getId().equals(actor.getId()))
			throw new IllegalStateException("요청 당사자는 승인할 수 없습니다.");
		this.status = TradeStatus.ENDED;
		this.endedAt = now;

		// 대기 흔적 정리
		this.pendingType = TradePendingType.NONE;
		this.requestedBy = null;
		this.requestedAt = null;
		this.lastActionBy = actor;
	}

	// 취소 거절 → 원상(PENDING) 복귀 + 사유 초기화
	public void rejectCancel(Member actor) {
		if (this.status != TradeStatus.PENDING || this.pendingType != TradePendingType.CANCEL)
			throw new IllegalStateException("취소 대기 상태가 아닙니다.");
		if (requestedBy != null && requestedBy.getId().equals(actor.getId()))
			throw new IllegalStateException("요청 당사자는 거절할 수 없습니다.");
		this.pendingType = TradePendingType.NONE;
		this.canceledReason = null; // 거절 시 사유 비움
		this.requestedBy = null;
		this.requestedAt = null;
		this.lastActionBy = actor;
	}

	@PrePersist
	protected void onCreate() {
		if (this.status == null)
			this.status = TradeStatus.PENDING;
		if (this.pendingType == null)
			this.pendingType = TradePendingType.NONE;
	}
}
