package com.shop.tradezone.service;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.shop.tradezone.constant.TradeStatus;
import com.shop.tradezone.entity.ChatMessage;
import com.shop.tradezone.entity.ChatRoom;
import com.shop.tradezone.entity.Member;
import com.shop.tradezone.entity.Trade;
import com.shop.tradezone.repository.ChatMessageRepository;
import com.shop.tradezone.repository.ChatRoomRepository;
import com.shop.tradezone.repository.MemberRepository;
import com.shop.tradezone.repository.TradeRepository;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional
public class ChatService {

	private final ChatRoomRepository roomRepo;
	private final ChatMessageRepository msgRepo;
	private final MemberRepository memberRepo;
	private final TradeRepository tradeRepo;

	@Transactional
	public ChatRoom getOrCreateRoom(Member buyer, Member seller, Long itemId) {
		// 방 유니크 규칙: (userA, userB) 정렬해서 저장(작은 id가 A)
		Member a = buyer.getId() < seller.getId() ? buyer : seller;
		Member b = buyer.getId() < seller.getId() ? seller : buyer;

		return roomRepo.findByUserAAndUserB(a, b).or(() -> roomRepo.findByUserBAndUserA(a, b)) // 안전 보강
				.orElseGet(() -> {
					ChatRoom r = ChatRoom.builder().userA(a).userB(b).createdAt(LocalDateTime.now())
							.lastMessageAt(LocalDateTime.now()).build();
					return roomRepo.save(r);
				});
	}

	@Transactional
	public ChatMessage saveMessage(Long roomId, Member sender, String content, String type) {
		ChatRoom room = roomRepo.findById(roomId)
				.orElseThrow(() -> new EntityNotFoundException("채팅방이 없습니다. id=" + roomId));

		boolean isA = sender.getId().equals(room.getUserA().getId());
		boolean isB = sender.getId().equals(room.getUserB().getId());
		if (!isA && !isB)
			throw new AccessDeniedException("방 참가자만 보낼 수 있습니다.");

		ChatMessage m = ChatMessage.builder().chatRoom(room).sender(sender).content(content)
				.type(type != null ? type : "TEXT").createdAt(LocalDateTime.now()).build();
		msgRepo.save(m);

		// 방 메타 업데이트: 최근메시지 시각 + 상대방 미읽음++
		room.setLastMessageAt(LocalDateTime.now());
		if (isA) {
			room.setUnreadB(room.getUnreadB() + 1);
		} else {
			room.setUnreadA(room.getUnreadA() + 1);
		}
		// JPA dirty checking으로 flush

		return m;
	}

	// 방 만들기(유저A-유저B 쌍을 기준으로 단일 방 유지)
	public ChatRoom openRoom(Member a, Member b) {
		return roomRepo.findByUserAAndUserB(a, b).or(() -> roomRepo.findByUserBAndUserA(a, b)).orElseGet(() -> {
			ChatRoom r = ChatRoom.builder().userA(a).userB(b).build();
			return roomRepo.save(r);
		});
	}

	// 로그인 사용자(meId)와 상대(otherId)로 방 생성/조회
	public ChatRoom createOrGetRoomByIds(Long meId, Long otherId) { // ★ 추가
		Member me = memberRepo.findById(meId).orElseThrow(() -> new IllegalArgumentException("회원이 없습니다: " + meId));
		Member other = memberRepo.findById(otherId)
				.orElseThrow(() -> new IllegalArgumentException("회원이 없습니다: " + otherId));
		return openRoom(me, other);
	}

	/** (기존 시그니처 유지) senderId + content 버전 */
	public ChatMessage saveMessage(Long roomId, Long senderId, String content) {
		// type 미지정 시 기본값 TEXT
		return saveMessageInternal(roomId, senderId, content, "TEXT");
	}

	// 공통 저장 로직
	private ChatMessage saveMessageInternal(Long roomId, Long senderId, String content, String type) { // ★ 추가(리팩터)
		ChatRoom room = roomRepo.findById(roomId)
				.orElseThrow(() -> new IllegalArgumentException("채팅방을 찾을 수 없습니다: " + roomId));

		// sender가 A인지 B인지 판정
		boolean senderIsA = room.getUserA().getId().equals(senderId);

		ChatMessage saved = msgRepo
				.save(ChatMessage.builder().chatRoom(room).sender(senderIsA ? room.getUserA() : room.getUserB())
						.content(content).type(type != null ? type : "TEXT") // ★ type 적용
						.delivered(true).readByA(senderIsA) // 보낸 사람은 즉시 읽음 처리
						.readByB(!senderIsA).build());

		// 방 메타 업데이트
		room.setLastMessageAt(saved.getCreatedAt());
		if (senderIsA) {
			room.setUnreadB(room.getUnreadB() + 1);
		} else {
			room.setUnreadA(room.getUnreadA() + 1);
		}
		return saved;
	}

	/** 페이징 히스토리(id DESC) */
	@Transactional(readOnly = true)
	public Page<ChatMessage> getHistory(Long roomId, Pageable pageable) {
		return msgRepo.findByChatRoom_IdOrderByIdDesc(roomId, pageable);
	}

	/** 최근 100개를 시간 오름차순으로 반환(프론트 출력 편의) */
	@Transactional(readOnly = true)
	public List<ChatMessage> recentMessages(Long roomId) {
		Page<ChatMessage> page = msgRepo.findByChatRoom_IdOrderByIdDesc(roomId, PageRequest.of(0, 100));
		List<ChatMessage> list = page.getContent();
		list.sort((a, b) -> a.getId().compareTo(b.getId()));
		return list;
	}

	// 읽음 처리: reader가 A면 A 읽음, B면 B 읽음
	@Transactional
	public void markRead(Long roomId, Member who) {
		ChatRoom room = roomRepo.findById(roomId)
				.orElseThrow(() -> new EntityNotFoundException("채팅방이 없습니다. id=" + roomId));

		boolean isA = who.getId().equals(room.getUserA().getId());
		boolean isB = who.getId().equals(room.getUserB().getId());
		if (!isA && !isB)
			throw new AccessDeniedException("방 참가자만 읽음 처리가 가능합니다.");

		if (isA) {
			// 메시지 readByA = true
			msgRepo.markAllReadByA(roomId);
			// 뱃지 0
			room.setUnreadA(0);
		} else {
			msgRepo.markAllReadByB(roomId);
			room.setUnreadB(0);
		}
	}

	// 메시지 저장 전, 연결된 Trade 상태가 COMPLETED/ENDED라면 예외 처리
	private void assertRoomWritable(ChatRoom room) {
		Trade t = tradeRepo.findByChatRoom(room).orElse(null);
		if (t != null && (t.getStatus() == TradeStatus.COMPLETED || t.getStatus() == TradeStatus.ENDED)) {
			throw new IllegalStateException("거래가 종료된 방입니다.");
		}
	}
}
