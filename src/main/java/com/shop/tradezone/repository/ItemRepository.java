package com.shop.tradezone.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.shop.tradezone.constant.ItemSellStatus;
import com.shop.tradezone.entity.Item;
import com.shop.tradezone.entity.Member;

@Repository
public interface ItemRepository extends JpaRepository<Item, Long> {

	int countBySeller(Member seller);

	boolean existsByItemCode(String itemCode);

	// 페이징 & 정렬 포함 목록 조회
	Page<Item> findAll(Pageable pageable);

	Page<Item> findByStatus(ItemSellStatus status, Pageable pageable);

	// 부모 카테고리 ID 및 자식 카테고리 ID로 상품 조회
	Page<Item> findByCategoryId_IdAndCategoryId_Parent_Id(Long childId, Long parentId, Pageable pageable);

	Page<Item> findBySellerId(Long sellerId, Pageable pageable);

}
