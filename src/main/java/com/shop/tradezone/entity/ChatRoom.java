package com.shop.tradezone.entity;

import java.time.LocalDateTime;

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
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "chat_room",
		// UNIQUE 제약조건 설정
		uniqueConstraints = { @UniqueConstraint(
				// 제약조건 이름(DB에서 확인 가능)
				name = "uk_room_item_usera_userb",
				// (item_id, user_a_id, user_b_id) 세 컬럼 조합은 유일해야 한다
				// 즉, 같은 아이템에 대해 같은 두 사용자는 방을 하나만 가질 수 있음
				columnNames = { "item_id", "user_a_id", "user_b_id" }) },
		// 인덱스 설정 (조회 성능 최적화용)
		indexes = {
				// item_id 컬럼에 인덱스 부여 → 아이템별 채팅방 찾을 때 빠름
				@Index(name = "idx_room_item", columnList = "item_id"),
				// user_a_id 컬럼에 인덱스 부여 → 내가 A로 들어간 방들 조회 최적화
				@Index(name = "idx_room_usera", columnList = "user_a_id"),
				// user_b_id 컬럼에 인덱스 부여 → 내가 B로 들어간 방들 조회 최적화
				@Index(name = "idx_room_userb", columnList = "user_b_id"),
				// lastMessageAt 컬럼에 인덱스 부여 → 최근 메시지 기준으로 정렬/목록 조회 성능 향상
				@Index(name = "idx_room_lastmsg", columnList = "lastMessageAt") })
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ChatRoom {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "item_id", nullable = false)
	private Item item;

	// 참여자 A/B — Member 엔티티는 변경 금지라 그대로 참조만 사용
	// 서비스단에서 "항상 작은 ID를 A로" 같은 정규화 규칙을 적용하면
	// (A,B)와 (B,A) 중복 생성 문제를 원천 차단 가능
	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "user_a_id", nullable = false)
	private Member userA;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "user_b_id", nullable = false)
	private Member userB;

	// 상태값 필드 추가 (onCreate에서 사용하므로 누락되면 NPE/컴파일 오류)
	public enum Status {
		OPEN, CLOSED
	}

	@Enumerated(EnumType.STRING)
	@Column(length = 20, nullable = false)
	private Status status;

	// Builder 기본값 보존: @Builder.Default 필수
	@Column(nullable = false, updatable = false)
	@Builder.Default
	private LocalDateTime createdAt = LocalDateTime.now();

	// 목록 정렬/최근 대화 갱신에 사용
	@Column(nullable = false)
	@Builder.Default
	private LocalDateTime lastMessageAt = LocalDateTime.now();

	// 미읽음 카운트는 항상 값이 있어야 하므로 NOT NULL + 기본값
	@Column(nullable = false)
	@Builder.Default
	private int unreadA = 0;

	@Column(nullable = false)
	@Builder.Default
	private int unreadB = 0;

	@PrePersist
	void onCreate() {
		// 방 생성 시 기본 상태/시각 보정
		if (status == null) {
			status = Status.OPEN;
		}
		if (lastMessageAt == null) {
			lastMessageAt = LocalDateTime.now();
		}
	}

	// 서비스에서 읽음/미읽음 처리, 최신메시지 갱신 시 활용
	public void touchLastMessageAt() {
		this.lastMessageAt = LocalDateTime.now();
	}

	public void incrementUnreadFor(Member target) {
		if (target == null)
			return;
		if (userA != null && userA.getId().equals(target.getId())) {
			this.unreadA++;
		} else if (userB != null && userB.getId().equals(target.getId())) {
			this.unreadB++;
		}
	}

	public void clearUnreadFor(Member target) {
		if (target == null)
			return;
		if (userA != null && userA.getId().equals(target.getId())) {
			this.unreadA = 0;
		} else if (userB != null && userB.getId().equals(target.getId())) {
			this.unreadB = 0;
		}
	}

}