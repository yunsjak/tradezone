package com.shop.tradezone.dto;

import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.shop.tradezone.constant.ItemSellStatus;
import com.shop.tradezone.entity.Item;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ItemCardDto {

	private Long itemId;

	private String name;

	@JsonProperty("imgUrl")
	private String thumbnailUrl;

	private String price;

	private ItemSellStatus status;

	private LocalDateTime created;

	public ItemCardDto(Item item) {
		this.itemId = item.getId();
		this.name = item.getName();
		this.price = item.getPrice();
		this.created = item.getCreated();
		if (item.getImages() != null && !item.getImages().isEmpty()) {
			this.thumbnailUrl = item.getImages().get(0).getImgUrl();
		}
	}

}
