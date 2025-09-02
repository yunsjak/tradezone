package com.shop.tradezone.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.shop.tradezone.dto.CategoryDto;
import com.shop.tradezone.entity.Category;
import com.shop.tradezone.repository.CategoryRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class CategoryService {

	private final CategoryRepository categoryRepository;

	/** 부모 카테고리 전체 조회 (1차) */
	public List<Category> findParentCategoriesWithChildren() {
		return categoryRepository.findByParentIsNull();
	}

	/** 특정 부모 카테고리의 자식(2차) 카테고리 조회 */
	public List<Category> findChildrenByParentId(Long parentId) {
		Category parent = categoryRepository.findById(parentId)
				.orElseThrow(() -> new IllegalArgumentException("해당 부모 카테고리가 존재하지 않습니다."));
		return parent.getChildren();
	}

	/** 자식 ID로 부모 ID 반환 */
	public Long getParentIdByChildId(Long childId) {
		Category child = categoryRepository.findById(childId)
				.orElseThrow(() -> new IllegalArgumentException("해당 카테고리가 존재하지 않습니다."));
		return (child.getParent() != null) ? child.getParent().getId() : null;
	}

	/** 카테고리 이름 반환 (ID 기준) */
	public String getCategoryNameById(Long categoryId) {
		Category category = categoryRepository.findById(categoryId).orElseThrow(() -> new RuntimeException("카테고리 없음"));
		return category.getName();
	}

	/** 부모 ID, 자식 ID가 연결되어 있는지 검증 및 조회 */
	public Category getCategoryByParentIdAndChildId(Long parentId, Long childId) {
		Category child = categoryRepository.findById(childId)
				.orElseThrow(() -> new IllegalArgumentException("자식 카테고리가 존재하지 않습니다."));

		if (child.getParent() == null || !child.getParent().getId().equals(parentId)) {
			throw new IllegalArgumentException("자식 카테고리가 해당 부모 카테고리에 속하지 않습니다.");
		}

		return child;
	}

	/** 트리 구조 (부모 + 자식 DTO) */
	public List<CategoryDto> getCategoryTree() {
		List<Category> mainCategories = categoryRepository.findByParentIsNull();
		return mainCategories.stream().map(this::toDto).toList();
	}

	/** [내부용] 엔티티 → DTO 변환 */
	private CategoryDto toDto(Category category) {
		List<CategoryDto> childDtos = category.getChildren().stream().map(this::toDto).toList();

		Long parentId = category.getParent() != null ? category.getParent().getId() : null;

		return new CategoryDto(category.getId(), category.getName(), category.getImgUrl(), parentId, childDtos);
	}
}
