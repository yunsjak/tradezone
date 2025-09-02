package com.shop.tradezone.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.shop.tradezone.entity.Item;
import com.shop.tradezone.entity.Like;
import com.shop.tradezone.entity.Member;

@Repository
public interface LikeRepository extends JpaRepository<Like, Long> {

	Optional<Like> findByItemAndMember(Item item, Member member);

	// 특정 유저가 특정 아이템을 찜했는지(존재 여부)
	boolean existsByItemAndMember(Item item, Member member);

	// 토글 OFF 시 사용 (단건 삭제)
	void deleteByItemAndMember(Item item, Member member);

	// 상세 페이지 숫자 표시에 사용
	int countByItem(Item item);

	// (마이페이지 등에서 전체 목록용)
	java.util.List<Like> findByMember(Member member);

	// 아이템 삭제 시 해당 아이템의 찜 전체 삭제
	void deleteByItem(Item item);

}