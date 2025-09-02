package com.shop.tradezone.entity;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Entity
@Setter
@Getter
public class Review {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY) // ID
	private Long id;

	@ManyToOne(fetch = FetchType.LAZY) // 아이템과 다대일 관계 (한 아이템에 여러 리뷰 가능)
	@JoinColumn(name = "item_id", nullable = false) // 외래키 지정
	private Item item;

	@ManyToOne(fetch = FetchType.LAZY) // 유저와 다대일 관계 (한 유저가 여러 리뷰 가능)
	@JoinColumn(name = "member_id", nullable = false)
	private Member member;

	@NotBlank(message = "내용을 입력해주세요.") // 내용이 비어있으면 안됨
	@Size(min = 2, max = 300, message = "내용은 2자 이상 300자 이하로 입력해주세요.") // 길이 제한
	@Column(columnDefinition = "TEXT", nullable = false)
	private String content;

	private LocalDateTime created = LocalDateTime.now(); // 날짜시간

	private LocalDateTime modified; // 수정날짜

}