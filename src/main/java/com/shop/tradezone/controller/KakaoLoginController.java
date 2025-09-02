package com.shop.tradezone.controller;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.shop.tradezone.entity.Member;
import com.shop.tradezone.service.CustomUserDetails;
import com.shop.tradezone.service.KakaoService;
import com.shop.tradezone.service.UserKakaoService;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;

@Controller
@RequiredArgsConstructor
public class KakaoLoginController {

	private final KakaoService kakaoService;
	private final UserKakaoService userKakaoService;

	@GetMapping("/callback")
	public String callback(@RequestParam("code") String code, HttpServletRequest request) {

		String accessToken = kakaoService.getAccessTokenFromKakao(code);
		var kakaoUserInfo = kakaoService.getUserInfo(accessToken);

		if (kakaoUserInfo == null) {
			return "redirect:/login?error";
		}

		Member member = userKakaoService.processKakaoUser(kakaoUserInfo);

		CustomUserDetails userDetails = new CustomUserDetails(member);
		Authentication authentication = new UsernamePasswordAuthenticationToken(userDetails, null,
				userDetails.getAuthorities());

		SecurityContext context = SecurityContextHolder.createEmptyContext();
		context.setAuthentication(authentication);
		SecurityContextHolder.setContext(context);

		HttpSession session = request.getSession(true);
		session.setAttribute("SPRING_SECURITY_CONTEXT", context);
		session.setAttribute("loginType", "KAKAO");

		return "redirect:/";
	}
}
