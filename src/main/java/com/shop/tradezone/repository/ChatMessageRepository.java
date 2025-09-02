package com.shop.tradezone.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import com.shop.tradezone.entity.ChatMessage;

public interface ChatMessageRepository extends JpaRepository<ChatMessage, Long> {

	// 최근 N개(뒤→앞) 슬라이스 조회
	// ❌ before: m.room.id → ✅ after: m.chatRoom.id
	@Query("""
			select m
			  from ChatMessage m
			  join fetch m.sender
			 where m.chatRoom.id = :roomId
			   and (:beforeId is null or m.id < :beforeId)
			 order by m.id desc
			""")
	List<ChatMessage> findSlice(@Param("roomId") Long roomId, @Param("beforeId") Long beforeId, Pageable pageable);

	// 읽음 처리 (A 시점)
	@Modifying
	@Query("""
			update ChatMessage m
			   set m.readByA = true
			 where m.chatRoom.id = :roomId
			   and m.readByA = false
			   and m.sender.id <> :readerId
			""")
	int markReadByA(@Param("roomId") Long roomId, @Param("readerId") Long readerId);

	// 읽음 처리 (B 시점)
	@Modifying
	@Query("""
			update ChatMessage m
			   set m.readByB = true
			 where m.chatRoom.id = :roomId
			   and m.readByB = false
			   and m.sender.id <> :readerId
			""")
	int markReadByB(@Param("roomId") Long roomId, @Param("readerId") Long readerId);

	Page<ChatMessage> findByChatRoom_IdOrderByIdDesc(Long roomId, Pageable pageable);

	// 오래된 → 최신 (createdAt 정렬)
	Page<ChatMessage> findByChatRoom_IdOrderByCreatedAtAsc(Long roomId, Pageable pageable);

	// 최신 1건
	Optional<ChatMessage> findTopByChatRoom_IdOrderByCreatedAtDesc(Long roomId);

	// ===== 대량 일괄 읽음 처리 (명시적 트랜잭션 및 플러시) =====
	@Modifying(clearAutomatically = true, flushAutomatically = true)
	@Transactional
	@Query("""
			update ChatMessage m
			   set m.readByA = true
			 where m.chatRoom.id = :roomId
			   and m.readByA = false
			""")
	int markAllReadByA(@Param("roomId") Long roomId);

	@Modifying(clearAutomatically = true, flushAutomatically = true)
	@Transactional
	@Query("""
			update ChatMessage m
			   set m.readByB = true
			 where m.chatRoom.id = :roomId
			   and m.readByB = false
			""")
	int markAllReadByB(@Param("roomId") Long roomId);
}
