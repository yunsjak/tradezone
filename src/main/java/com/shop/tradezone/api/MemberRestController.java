package com.shop.tradezone.api;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.shop.tradezone.service.MemberService;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/member")
public class MemberRestController {

	private final MemberService memberService;

	@GetMapping("/check-username")
	public ResponseEntity<Boolean> checkUsername(@RequestParam(name = "username") String username) {
		// @RequestParam → 파라미터 이름으로 바인딩하는 데 사용되는 어노테이션

		boolean isDuplicate = memberService.isUsernameDuplicate(username);
		// isDuplicate → 배열 원소의 중복을 체크하는 함수(true, false 반환)

		return ResponseEntity.ok(isDuplicate);
	}
}