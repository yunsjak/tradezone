package com.shop.tradezone.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import com.shop.tradezone.entity.Notice;

public interface NoticeRepository extends JpaRepository<Notice, Long> {

	Page<Notice> findAll(Pageable pageable);

	Notice findByTitle(String title);

	Notice findByTitleAndContent(String title, String content);

}