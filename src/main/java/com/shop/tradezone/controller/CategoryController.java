package com.shop.tradezone.controller;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.shop.tradezone.dto.CategoryDto;
import com.shop.tradezone.entity.Category;
import com.shop.tradezone.service.CategoryService;

@RestController // JSON 반환을 위해 @Controller가 아닌 @RestController 사용
@RequestMapping("/categories")
public class CategoryController {

	@Autowired
	private CategoryService categoryService;

	@GetMapping("/children")
	public List<CategoryDto> getChildCategories(@RequestParam("parentId") Long parentId) {
		// 자식 카테고리 가져오기
		List<Category> children = categoryService.findChildrenByParentId(parentId);

		// Entity -> DTO 매핑
		return children.stream()
				.map(child -> new CategoryDto(child.getId(), child.getName(), child.getImgUrl(), parentId, null))
				.collect(Collectors.toList());
	}
}