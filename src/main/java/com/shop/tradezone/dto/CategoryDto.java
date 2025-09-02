package com.shop.tradezone.dto;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CategoryDto {
	private Long id;
	private String name;
	private String imgUrl;
	private Long parentCategoryId;
	private List<CategoryDto> children;
}
