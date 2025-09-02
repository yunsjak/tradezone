package com.shop.tradezone.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ItemImgDto {

	private Long id;

	private String imgName;

	private String imgUrl;

	private String thumbnailUrl;

}
