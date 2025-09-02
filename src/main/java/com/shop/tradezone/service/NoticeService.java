package com.shop.tradezone.service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import com.shop.tradezone.entity.Member;
import com.shop.tradezone.entity.Notice;
import com.shop.tradezone.exception.DataNotFoundException;
import com.shop.tradezone.repository.NoticeRepository;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Service
public class NoticeService {

	private final NoticeRepository noticeRepository;

	public Page<Notice> getList(int page) {
		List<Sort.Order> sorts = new ArrayList<>();
		sorts.add(Sort.Order.desc("id"));
		Pageable pageable = PageRequest.of(page, 10, Sort.by(sorts));
		return this.noticeRepository.findAll(pageable);
	}

	public Notice getNotice(Long id) {

		// 브라우져에서 없는 id값을 입력 할 경우 예외 처리함
		// optional은 NUll 처리를 안전하게 할수 있도록 도와주는 class 입니다
		Optional<Notice> notice = this.noticeRepository.findById(id);
		if (notice.isPresent()) {
			return notice.get();
		} else {
			throw new DataNotFoundException("notice not found");
		}

	}

	public void create(String title, String content, Member member) {
		Notice n = new Notice();
		n.setTitle(title);
		n.setContent(content);
		n.setMember(member);
		n.setCreated(LocalDateTime.now());
		this.noticeRepository.save(n);
	}

	public void modify(Notice notice, String title, String content) {
		notice.setTitle(title);
		notice.setContent(content);
		notice.setCreated(LocalDateTime.now());
		noticeRepository.save(notice);
	}

	public void delete(Notice notice) {
		this.noticeRepository.delete(notice);
	}

}