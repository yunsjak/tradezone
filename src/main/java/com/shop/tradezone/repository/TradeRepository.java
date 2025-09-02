package com.shop.tradezone.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.shop.tradezone.entity.ChatRoom;
import com.shop.tradezone.entity.Trade;

import jakarta.persistence.LockModeType;

public interface TradeRepository extends JpaRepository<Trade, Long> {

	// 기본 조회
	Optional<Trade> findByChatRoom(ChatRoom chatRoom);

	Optional<Trade> findByChatRoom_Id(Long roomId);

	@EntityGraph(attributePaths = { "buyer", "seller" })
	Optional<Trade> findWithPartiesByChatRoom_Id(Long roomId);

	@Lock(LockModeType.OPTIMISTIC)
	Optional<Trade> findWithLockById(Long id);

	boolean existsByChatRoom_Id(Long roomId);

	// roomId 스푸핑 방지용: tradeId -> roomId
	@Query("select t.chatRoom.id from Trade t where t.id = :tradeId")
	Optional<Long> findRoomIdByTradeId(@Param("tradeId") Long tradeId);

	// 구독 권한 체크용 (room 기준)
	@Query("""
			select (count(t) > 0) from Trade t
			where t.chatRoom.id = :roomId
			  and (t.buyer.id = :uid or t.seller.id = :uid)
			""")
	boolean canAccessRoom(@Param("roomId") Long roomId, @Param("uid") Long userId);

	// 구독 권한 체크용 (trade 기준)
	@Query("""
			select (count(t) > 0) from Trade t
			where t.id = :tradeId
			  and (t.buyer.id = :uid or t.seller.id = :uid)
			""")
	boolean canAccessTrade(@Param("tradeId") Long tradeId, @Param("uid") Long userId);
}