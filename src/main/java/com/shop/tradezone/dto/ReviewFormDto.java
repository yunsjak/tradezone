package com.shop.tradezone.dto;

import java.time.LocalDateTime;

import com.shop.tradezone.entity.Item;
import com.shop.tradezone.entity.Member;
import com.shop.tradezone.entity.Review;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ReviewFormDto {

	private Long id;

	private String username;

	private Long itemId;

	private String itemName;

	@NotBlank(message = "내용을 입력해주세요.")
	@Size(min = 2, max = 300, message = "내용은 2자 이상 300자 이하로 입력해주세요.")
	private String content;

	private LocalDateTime created;

	public ReviewFormDto(Review review) {
		Item item = review.getItem();
		Member member = review.getMember();

		this.itemId = item.getId();
		this.itemName = item.getName();
		this.content = review.getContent();
		this.created = review.getCreated();
		this.username = member.getUsername();
	}

}
