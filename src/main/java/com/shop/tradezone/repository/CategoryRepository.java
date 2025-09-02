package com.shop.tradezone.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import com.shop.tradezone.entity.Category;

public interface CategoryRepository extends JpaRepository<Category, Long> {

	// 1차 카테고리(부모) 조회 (자식 함께 로딩)
	@EntityGraph(attributePaths = { "children" })
	List<Category> findByParentIsNull();

	// 이름 기반 조합 조회 (1차 + 2차)
	Optional<Category> findByParent_NameAndName(String parentName, String name);

	// 1차 카테고리만 이름으로 조회 (자식 조회 시 사용 가능)
	Optional<Category> findByNameAndParentIsNull(String name);
}
