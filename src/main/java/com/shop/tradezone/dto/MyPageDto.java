package com.shop.tradezone.dto;

import java.util.List;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class MyPageDto {

	private String email;
	private List<ItemCardDto> items;
	private List<LikeFormDto> likes;
	private List<ReviewFormDto> review;

}
