package com.shop.tradezone.controller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;

@Controller
public class LogoutController {

	@Value("${kakao.client_id}")
	private String kakaoClientId;

	@Value("${kakao.logout_redirect_uri}")
	private String kakaoLogoutRedirectUri;

	@GetMapping("/logout")
	public String logout(HttpServletRequest request) {
		HttpSession session = request.getSession(false);
		if (session == null)
			return "redirect:/";

		String loginType = (String) session.getAttribute("loginType");
		session.invalidate(); // 세션 종료

		if ("KAKAO".equals(loginType)) {
			String kakaoLogoutUrl = "https://kauth.kakao.com/oauth/logout?client_id=" + kakaoClientId
					+ "&logout_redirect_uri=" + kakaoLogoutRedirectUri;
			return "redirect:" + kakaoLogoutUrl;
		}

		return "redirect:/"; // 로컬 로그아웃 시 기본 리다이렉트
	}
}
