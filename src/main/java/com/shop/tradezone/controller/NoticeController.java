package com.shop.tradezone.controller;

import java.security.Principal;

import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.server.ResponseStatusException;

import com.shop.tradezone.dto.NoticeFormDto;
import com.shop.tradezone.entity.Member;
import com.shop.tradezone.entity.Notice;
import com.shop.tradezone.service.MemberService;
import com.shop.tradezone.service.NoticeService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RequestMapping("/notice")
@Controller
@RequiredArgsConstructor
public class NoticeController {

	private final NoticeService noticeService;
	private final MemberService memberService;

	@GetMapping("")
	public String list(@RequestParam(value = "page", defaultValue = "0") int page, Model model) {
		Page<Notice> paging = noticeService.getList(page);
		model.addAttribute("paging", paging);
		return "notice";
	}

	@GetMapping(value = "/detail/{id}")
	public String detail(Model model, @PathVariable("id") Long id) {
		Notice notice = this.noticeService.getNotice(id);
		model.addAttribute("notice", notice);
		return "notice_detail";

	}

	@PreAuthorize("hasRole('ADMIN')")
	@GetMapping("/create")
	public String noticeCreate(Model model) {
		model.addAttribute("noticeFormDto", new NoticeFormDto()); // 소문자 시작 이름 사용 권장
		return "notice_form";
	}

	@PreAuthorize("hasRole('ADMIN')")
	@PostMapping("/create")
	public String noticeCreate(@Valid NoticeFormDto noticeFormDto, BindingResult bindingResult, Principal principal) {
		if (bindingResult.hasErrors()) {
			return "notice_form";
		}
		Member member = this.memberService.findByUsername(principal.getName());
		this.noticeService.create(noticeFormDto.getTitle(), noticeFormDto.getContent(), member);
		return "redirect:/notice";
	}

	@PreAuthorize("hasRole('ADMIN')")
	@GetMapping("/modify/{id}")
	public String modifyForm(@PathVariable("id") Long id, NoticeFormDto noticeFormDto, Principal principal) {
		Notice notice = noticeService.getNotice(id);
		noticeFormDto.setTitle(notice.getTitle());
		noticeFormDto.setContent(notice.getContent());
		return "notice_form";
	}

	@PreAuthorize("hasRole('ADMIN')")
	@PostMapping("/modify/{id}")
	public String modify(@PathVariable("id") Long id, @Valid NoticeFormDto noticeFormDto, BindingResult bindingResult,
			Member member, Principal principal) {
		if (bindingResult.hasErrors()) {
			return "notice_form";
		}

		Notice notice = noticeService.getNotice(id);
		noticeService.modify(notice, noticeFormDto.getTitle(), noticeFormDto.getContent());

		return String.format("redirect:/notice/detail/%s", id);
	}

	@PreAuthorize("hasRole('ADMIN')")
	@GetMapping("/delete/{id}")
	public String noticeDelete(Principal principal, @PathVariable("id") Long id) {
		Notice notice = this.noticeService.getNotice(id);
		if (!notice.getMember().getUsername().equals(principal.getName())) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "삭제 권한이 없습니다.");
		}
		this.noticeService.delete(notice);
		return "redirect:/notice";
	}
}