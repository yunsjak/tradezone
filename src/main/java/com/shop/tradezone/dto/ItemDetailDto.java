package com.shop.tradezone.dto;

import java.time.LocalDateTime;
import java.util.List;

import com.shop.tradezone.constant.ItemSellStatus;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ItemDetailDto {
	// 상품 정보
	private Long id;
	private String name;
	private String description;
	private String price;
	private String region;
	private int sellerItemCount;

	// 판매자 정보
	private String sellerName;
	private Long sellerId;

	// 카테고리, 상태
	private String parentName;
	private String childName;
	private Long childCategoryId;
	private Long parentCategoryId;
	private ItemSellStatus status;

	// 이미지, 리뷰
	private List<String> imgUrls;
	private List<ReviewFormDto> reviews;

	// 메타 정보
	private LocalDateTime created;
	private int viewCount;
	private int likeCount;
	private boolean isLiked;

	// 생성자
	public ItemDetailDto(boolean isLiked, int likeCount) {
		this.isLiked = isLiked;
		this.likeCount = likeCount;
	}
}
