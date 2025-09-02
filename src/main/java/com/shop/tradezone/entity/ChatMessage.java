package com.shop.tradezone.entity;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "chat_message", indexes = { @Index(name = "idx_msg_room_id", columnList = "room_id, id") // 히스토리 페이징용
})
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ChatMessage {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "room_id", nullable = false)
	private ChatRoom chatRoom;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "sender_id", nullable = true)
	private Member sender;

	@Column(nullable = false, columnDefinition = "TEXT")
	private String content;

	// 확장 대비: TEXT / IMAGE / NOTICE 등
	@Column(nullable = false, length = 20)
	@Builder.Default
	private String type = "TEXT";

	@Column(nullable = false)
	@Builder.Default
	private LocalDateTime createdAt = LocalDateTime.now();

	// 전달/읽음 상태(단순화)
	@Column(nullable = false)
	@Builder.Default
	private boolean delivered = true;

	@Column(nullable = false)
	@Builder.Default
	private boolean readByA = false;

	@Column(nullable = false)
	@Builder.Default
	private boolean readByB = false;

}
