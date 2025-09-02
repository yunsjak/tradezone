package com.shop.tradezone.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.shop.tradezone.entity.Item;
import com.shop.tradezone.entity.Member;
import com.shop.tradezone.entity.Review;

@Repository
public interface ReviewRepository extends JpaRepository<Review, Long> {
	List<Review> findByItemIdOrderByCreatedDesc(Long itemId);

	// 상품 상세 페이지 리뷰목록
	List<Review> findByItemOrderByCreatedDesc(Item item);

	// 마이페이지 > 내가 작성한 리뷰 목록
	List<Review> findByMemberOrderByCreatedDesc(Member member);

	// 특정 아이템에 연결된 모든 리뷰 삭제 상품 삭제 시 연쇄 삭제가 안 되는 경우 수동 삭제
	void deleteByItem(Item item);
}