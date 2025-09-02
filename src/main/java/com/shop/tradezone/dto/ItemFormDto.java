package com.shop.tradezone.dto;

import java.util.List;

import org.springframework.web.multipart.MultipartFile;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ItemFormDto {

	@NotBlank(message = "상품명을 입력해주세요.")
	private String name;

	@NotBlank(message = "상품 설명을 입력해주세요.")
	private String description;

	@NotNull(message = "가격을 입력해주세요")
	@Pattern(regexp = "^[0-9,]+$", message = "숫자만 입력해주세요.")
	private String price;

	@NotNull(message = "팀을 선택해주세요.")
	private Long parentCategoryId;

	@NotNull(message = "항목을 선택해주세요.")
	private Long childCategoryId;

	private String region;

	@NotEmpty(message = "상품 이미지를 1개 이상 업로드해주세요.")
	private List<MultipartFile> images;
}
