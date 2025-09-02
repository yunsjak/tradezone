package com.shop.tradezone.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.shop.tradezone.entity.ItemImg;

@Repository
public interface ItemImgRepository extends JpaRepository<ItemImg, Long> {
	List<ItemImg> findByItemId(Long itemId);
}
