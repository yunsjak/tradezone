package com.shop.tradezone.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.shop.tradezone.entity.ChatRoom;
import com.shop.tradezone.entity.Item;
import com.shop.tradezone.entity.Member;

public interface ChatRoomRepository extends JpaRepository<ChatRoom, Long> {

	Optional<ChatRoom> findByUserAIdAndUserBId(Long aId, Long bId);

	Optional<ChatRoom> findByUserAAndUserB(Member a, Member b);

	Optional<ChatRoom> findByUserBAndUserA(Member b, Member a);

	List<ChatRoom> findByUserAOrUserB(Member userA, Member userB);

	// 실제로는 userA/userB를 참조하도록 JPQL로 매핑 교정
	@Query("""
				select r
				from ChatRoom r
				where r.item = :item
				  and r.userA = :seller
				  and r.userB = :buyer
			""")
	Optional<ChatRoom> findByItemAndSellerAndBuyer(Item item, Member seller, Member buyer);

	// 실제 필드는 userA/userB를 사용하여 최신 메시지 순으로 페이지 조회
	@Query("""
				select r
				from ChatRoom r
				where r.userA = :seller or r.userB = :buyer
				order by r.lastMessageAt desc
			""")
	Page<ChatRoom> findBySellerOrBuyerOrderByLastMessageAtDesc(Member seller, Member buyer, Pageable pageable);

	// (옵션) 목록에서 item 제목/이미지 등을 자주 쓰면 N+1 방지용으로 즉시 로딩 지정
	@EntityGraph(attributePaths = { "item" })
	@Query("""
				select r
				from ChatRoom r
				where r.userA = :me or r.userB = :me
				order by r.lastMessageAt desc
			""")
	Page<ChatRoom> findMyRoomsWithItemOrdered(Member me, Pageable pageable);
}
