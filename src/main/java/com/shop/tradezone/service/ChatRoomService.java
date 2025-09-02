package com.shop.tradezone.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.shop.tradezone.dto.ChatRoomListDto;
import com.shop.tradezone.entity.ChatMessage;
import com.shop.tradezone.entity.ChatRoom;
import com.shop.tradezone.entity.Item;
import com.shop.tradezone.entity.Member;
import com.shop.tradezone.repository.ChatMessageRepository;
import com.shop.tradezone.repository.ChatRoomRepository;
import com.shop.tradezone.repository.ItemRepository;
import com.shop.tradezone.repository.MemberRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ChatRoomService {

	private final ChatRoomRepository chatRoomRepository;
	private final ChatMessageRepository chatMessageRepository;
	private final ItemRepository itemRepository;
	private final MemberRepository memberRepository;

	// 공용 로더 (예외 메시지 한글화)
	// itemId로 Item 로드 (없으면 400 성격의 예외 던짐)
	private Item loadItem(Long itemId) {
		return itemRepository.findById(itemId)
				.orElseThrow(() -> new IllegalArgumentException("존재하지 않는 아이템입니다. id=" + itemId));
	}

	// memberId로 Member 로드 (없으면 400 성격의 예외 던짐)
	private Member loadMember(Long memberId) {
		return memberRepository.findById(memberId)
				.orElseThrow(() -> new IllegalArgumentException("존재하지 않는 사용자입니다. id=" + memberId));
	}

	// 1) getOrCreate : 같은 아이템 + 같은 두 사용자 → 방 1개만 보장
	/**
	 * 채팅방 생성 혹은 조회. - 두 사용자 ID를 정규화(작은 ID → A, 큰 ID → B)하여 항상 같은 조합으로 조회/생성 - 유니크
	 * 제약 충돌 발생 시 재조회로 복구 (동시 클릭 대비)
	 */
	@Transactional
	public ChatRoom getOrCreate(Long itemId, Long userId1, Long userId2) {
		if (userId1.equals(userId2)) {
			throw new IllegalArgumentException("동일한 사용자끼리는 채팅방을 만들 수 없습니다.");
		}

		Item item = loadItem(itemId);
		Member u1 = loadMember(userId1);
		Member u2 = loadMember(userId2);

		// AB/BA 정규화: 항상 작은 ID가 A, 큰 ID가 B
		Member userA = (u1.getId() < u2.getId()) ? u1 : u2;
		Member userB = (u1.getId() < u2.getId()) ? u2 : u1;

		// 레포지토리 메서드명(과거 seller/buyer 표기)을 그대로 쓰되,
		// 실제 JPQL은 userA/userB를 바라보도록 @Query로 매핑돼 있어야 함
		Optional<ChatRoom> existing = chatRoomRepository.findByItemAndSellerAndBuyer(item, userA, userB);
		if (existing.isPresent())
			return existing.get();

		ChatRoom room = ChatRoom.builder().item(item).userA(userA).userB(userB).build();

		try {
			return chatRoomRepository.save(room);
		} catch (DataIntegrityViolationException dup) {
			// 동시성으로 유니크 충돌 시 재조회로 안전 복구
			return chatRoomRepository.findByItemAndSellerAndBuyer(item, userA, userB).orElseThrow(() -> dup);
		}
	}

	// 2) DTO 변환: 목록에 필요한 최소 필드 + 최신 메시지 1건

	/**
	 * ChatRoom -> ChatRoomListDto 변환 - 상대 표시명, 미읽음 카운트, 최신 메시지/시각을 채워서 리스트에 쓰기 좋게
	 * 가공
	 */
	@Transactional(readOnly = true)
	private ChatRoomListDto toDto(ChatRoom r, Member me) {
		// 1) 상대방/미읽음 카운트 계산
		Member partner = r.getUserA().equals(me) ? r.getUserB() : r.getUserA();
		int unread = r.getUserA().equals(me) ? r.getUnreadA() : r.getUnreadB();

		// 2) 최신 메시지 1건 조회
		// ⚠️ 레포지토리에 아래 시그니처가 필요합니다.
		// Optional<ChatMessage> findTopByChatRoomIdOrderByCreatedAtDesc(Long roomId);
		ChatMessage latest = chatMessageRepository.findTopByChatRoom_IdOrderByCreatedAtDesc(r.getId()).orElse(null);

		String lastMessage = (latest != null) ? latest.getContent() : null;

		// 3) lastMessageAt 보정: 엔티티의 lastMessageAt 없으면 최신 메시지 기준
		LocalDateTime lastAt = (r.getLastMessageAt() != null) ? r.getLastMessageAt()
				: (latest != null ? latest.getCreatedAt() : null);

		return ChatRoomListDto.builder().roomId(r.getId()).partnerName(partner.getUsername()) // Member 표시명 필드에 맞게 사용
				.lastMessage(lastMessage).lastMessageAt(lastAt).unreadCount(unread).build();
	}

	// 3) 내가 속한 채팅방 리스트 조회 (Member로 받는 버전) — 기존 메서드 유지
	@Transactional(readOnly = true)
	public List<ChatRoomListDto> getMyRooms(Member me) {
		return chatRoomRepository.findByUserAOrUserB(me, me).stream().map(r -> toDto(r, me))
				.collect(Collectors.toList());
	}

	// 4) 내가 속한 채팅방 리스트 조회 (meId로 받는 오버로드) — 컨트롤러에서 쓰기 편함
	@Transactional(readOnly = true)
	public List<ChatRoomListDto> getMyRooms(Long meId) {
		Member me = loadMember(meId);
		return getMyRooms(me); // 위 메서드 재사용
	}
}
