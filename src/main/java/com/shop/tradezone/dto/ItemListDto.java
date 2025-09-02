package com.shop.tradezone.dto;

import java.time.LocalDateTime;

import com.shop.tradezone.constant.ItemSellStatus;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ItemListDto {

	private String id;
	private String itemCode;
	private String name;
	private String price;
	private ItemSellStatus status;
	private LocalDateTime created;
}
