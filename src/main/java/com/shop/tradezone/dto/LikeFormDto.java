package com.shop.tradezone.dto;

import java.time.LocalDateTime;

import com.shop.tradezone.entity.Item;
import com.shop.tradezone.entity.Like;
import com.shop.tradezone.entity.ItemImg;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class LikeFormDto {

	private Long itemId;

	private String itemName;

	private String imgUrl;

	private String price;

	private LocalDateTime created;

	// ======================================

	public LikeFormDto(Like like) {

		Item item = like.getItem();

		this.itemId = item.getId();
		this.itemName = item.getName();
		this.price = item.getPrice();
		this.created = item.getCreated();
		
		// 이미지 URL 설정 (첫 번째 이미지 사용) - ItemCardDto와 동일한 방식
		if (item.getImages() != null && !item.getImages().isEmpty()) {
			this.imgUrl = item.getImages().get(0).getImgUrl();
		}
	}
}