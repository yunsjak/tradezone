package com.shop.tradezone.entity;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import com.shop.tradezone.constant.ItemSellStatus;
import com.shop.tradezone.repository.ItemRepository;
import com.shop.tradezone.util.BeanUtil;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.PrePersist;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Item {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "item_id")
	private Long id;

	@Column(unique = true)
	private String itemCode;

	@Column(nullable = false, length = 50)
	private String name;

	@Column(columnDefinition = "TEXT", length = 1000)
	private String description;

	private String price;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "category_id")
	private Category categoryId;

	@Enumerated(EnumType.STRING)
	private ItemSellStatus status;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "seller")
	private Member seller;
	
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "buyer")
	private Member buyer;
	
	@Column(name = "purchased_at")
	private LocalDateTime purchasedAt;

	private String region;

	@OneToMany(mappedBy = "item", cascade = CascadeType.ALL)
	private List<ItemImg> images = new ArrayList<>();

	@OneToMany(mappedBy = "item", cascade = CascadeType.ALL)
	private List<Like> likes = new ArrayList<>();

	@OneToMany(mappedBy = "item", cascade = CascadeType.ALL)
	private List<Review> reviews = new ArrayList<>();

	private LocalDateTime created;

	@Column(nullable = false)
	private int viewCount = 0;

	public void increaseViewCount() {
		this.viewCount++;
	}

	@PrePersist
	public void prePersist() {
		if (itemCode == null || itemCode.isBlank()) {
			itemCode = generateUniqueItemCode();
		}
		if (created == null) {
			created = LocalDateTime.now();
		}
	}

	private String generateUniqueItemCode() {
		String code;
		ItemRepository itemRepository = BeanUtil.getBean(ItemRepository.class);
		do {
			code = generateRandomCode(7);
		} while (itemRepository.existsByItemCode(code));
		return code;
	}

	private String generateRandomCode(int length) {
		Random random = new Random();
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < length; i++) {
			sb.append(random.nextInt(10));
		}
		return sb.toString();
	}
}
