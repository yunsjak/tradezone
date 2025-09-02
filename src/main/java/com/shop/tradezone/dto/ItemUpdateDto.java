package com.shop.tradezone.dto;

import java.util.List;

import org.springframework.web.multipart.MultipartFile;

import com.shop.tradezone.entity.Item;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@AllArgsConstructor
public class ItemUpdateDto {

	private Long id; // 여기서는 null 체크는 하지 않음 (서비스에서 처리)

	@NotBlank(message = "상품명을 입력해주세요.")
	private String name;

	@NotBlank(message = "상품 설명을 입력해주세요.")
	private String description;

	@NotNull(message = "가격을 입력해주세요.")
	@Pattern(regexp = "^[0-9,]+$", message = "숫자만 입력해주세요.")
	private String price;

	private String region;

	@NotNull(message = "팀을 선택해주세요.")
	private Long parentCategoryId;

	@NotNull(message = "항목을 선택해주세요.")
	private Long childCategoryId;

	private List<Long> imgIds;

	private List<MultipartFile> images; // 이미지 수정은 선택 사항

	public ItemUpdateDto(Item item) {

		this.id = item.getId();
		this.name = item.getName();
		this.description = item.getDescription();
		this.price = item.getPrice();
		this.region = item.getRegion();
	}

}
