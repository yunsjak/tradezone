package com.shop.tradezone.config;

import java.util.List;

import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

import com.shop.tradezone.entity.Category;
import com.shop.tradezone.service.CategoryService;

import lombok.RequiredArgsConstructor;

@ControllerAdvice
@RequiredArgsConstructor
public class CategoryAdvice {

	private final CategoryService categoryService;

	@ModelAttribute("categories")
	public List<Category> categories() {
		return categoryService.findParentCategoriesWithChildren();
	}

}
