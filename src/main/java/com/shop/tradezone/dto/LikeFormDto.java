package com.shop.tradezone.dto;

import java.time.LocalDateTime;

import com.shop.tradezone.entity.Item;
import com.shop.tradezone.entity.Like;

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
	}
}