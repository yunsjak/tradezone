package com.shop.tradezone.service;

import java.time.LocalDateTime;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import com.shop.tradezone.dto.ChatMessageDto; // ← 프로젝트의 실제 패키지에 맞춤
import com.shop.tradezone.entity.ChatMessage;
import com.shop.tradezone.entity.ChatRoom;
import com.shop.tradezone.entity.Member;
import com.shop.tradezone.repository.ChatMessageRepository;
import com.shop.tradezone.repository.ChatRoomRepository;
import com.shop.tradezone.repository.MemberRepository;

import lombok.RequiredArgsConstructor;

/**
 * 채팅 메시지 비즈니스 로직 - 메시지 저장 - 방 메타(lastMessageAt / unread) 갱신 - 메시지 페이지 조회 -
 * (트랜잭션 커밋 후) 소켓 브로드캐스트
 */
@Service
@RequiredArgsConstructor
public class ChatMessageService {

	private final ChatMessageRepository chatMessageRepository;
	private final ChatRoomRepository chatRoomRepository;
	private final MemberRepository memberRepository;
	private final SimpMessagingTemplate messagingTemplate; // /topic 브로드캐스트용

	// 공용 로더

	private ChatRoom loadRoom(Long roomId) {
		return chatRoomRepository.findById(roomId)
				.orElseThrow(() -> new IllegalArgumentException("존재하지 않는 채팅방입니다. id=" + roomId));
	}

	private Member loadMember(Long memberId) {
		return memberRepository.findById(memberId)
				.orElseThrow(() -> new IllegalArgumentException("존재하지 않는 사용자입니다. id=" + memberId));
	}

	/// 1) 텍스트 메시지 전송

	// 1) content 정제/검증 2) 방/보낸이 검증 3) 저장 + 방 메타 갱신 4) 트랜잭션 커밋 후 브로드캐스트

	@Transactional
	public ChatMessageDto sendText(Long roomId, Long senderId, String content) {
		// 1) content 정제/검증
		content = (content == null) ? "" : content.strip();
		if (content.isEmpty())
			throw new IllegalArgumentException("메시지 내용이 비어 있습니다.");

		// 2) 방/보낸이 검증
		ChatRoom room = loadRoom(roomId);
		Member sender = loadMember(senderId);

		boolean senderIsA = sender.getId().equals(room.getUserA().getId());
		boolean senderIsB = sender.getId().equals(room.getUserB().getId());
		if (!senderIsA && !senderIsB) {
			throw new IllegalStateException("해당 채팅방의 참가자가 아닙니다.");
		}

		// 3) 저장 (+ 기본 type=TEXT)
		ChatMessage msg = ChatMessage.builder().chatRoom(room) // 엔티티 필드명이 chatRoom 이어야 합니다.
				.sender(sender).content(content)
				// .type("TEXT") // @Builder.Default 로 기본값이면 생략 가능
				.build();
		chatMessageRepository.save(msg);

		// 방 메타 갱신
		room.setLastMessageAt(LocalDateTime.now());
		if (senderIsA)
			room.setUnreadB(room.getUnreadB() + 1);
		else
			room.setUnreadA(room.getUnreadA() + 1);

		// 4) 커밋 후 브로드캐스트
		ChatMessageDto dto = new ChatMessageDto(msg);
		TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
			@Override
			public void afterCommit() {
				messagingTemplate.convertAndSend("/topic/chat." + roomId, dto);
			}
		});
		return dto;
	}

	// 2) 시스템 메시지 전송

	// 시스템 메시지 전송 (ex. 거래 상태 변경 안내 등) - sender 를 null 로 저장하려면 ChatMessage.sender 의
	// JoinColumn(nullable=true) 필요 또는 "시스템 사용자" 계정을 하나 두고 그 ID로 저장하세요.
	@Transactional
	public ChatMessageDto sendSystem(Long roomId, String content) {
		if (content == null || content.isBlank())
			return null;

		ChatRoom room = loadRoom(roomId);

		ChatMessage msg = ChatMessage.builder().chatRoom(room).sender(null) // nullable=true 가 아니면 시스템 사용자로 대체
				.content(content).type("SYSTEM").build();
		chatMessageRepository.save(msg);

		room.setLastMessageAt(LocalDateTime.now());
		// 시스템 메시지는 미읽음 증가 X (정책에 따라 변경 가능)

		ChatMessageDto dto = new ChatMessageDto(msg);
		TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
			@Override
			public void afterCommit() {
				messagingTemplate.convertAndSend("/topic/chat." + roomId, dto);
			}
		});
		return dto;
	}

	// 3) 메시지 페이지 조회 (오래된 → 최신)

	@Transactional(readOnly = true)
	public Page<ChatMessageDto> getMessages(Long roomId, int page, int size) {
		ChatRoom room = loadRoom(roomId);
		Pageable pageable = PageRequest.of(page, size);
		return chatMessageRepository.findByChatRoom_IdOrderByCreatedAtAsc(room.getId(), pageable)
				.map(ChatMessageDto::new);
	}

	// 4) 읽음 처리
	@Transactional
	public void clearUnread(Long roomId, Long viewerId) {
		ChatRoom room = loadRoom(roomId);
		Member viewer = loadMember(viewerId);

		if (viewer.getId().equals(room.getUserA().getId())) {
			room.setUnreadA(0);
		} else if (viewer.getId().equals(room.getUserB().getId())) {
			room.setUnreadB(0);
		} else {
			throw new IllegalStateException("해당 채팅방의 참가자가 아닙니다.");
		}
	}
}
