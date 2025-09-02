package com.shop.tradezone.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@Setter
@Table(name = "likes", uniqueConstraints = {
		@UniqueConstraint(name = "uk_like_member_item", columnNames = { "member_id", "item_id" }) })
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Like {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY) // ID
	private Long id;

	@ManyToOne(fetch = FetchType.LAZY) // 한 아이템을 여러 유저가 찜 가능
	@JoinColumn(name = "item_id", nullable = false)
	private Item item;

	@ManyToOne(fetch = FetchType.LAZY) // 한 유저가 여러 아이템 찜 가능
	@JoinColumn(name = "member_id", nullable = false)
	private Member member;

}