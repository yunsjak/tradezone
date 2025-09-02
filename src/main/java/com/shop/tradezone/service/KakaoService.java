package com.shop.tradezone.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import com.shop.tradezone.dto.KakaoTokenResponseDto;
import com.shop.tradezone.dto.KakaoUserInfoResponseDto;

import io.netty.handler.codec.http.HttpHeaderValues;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;

@Slf4j
@RequiredArgsConstructor
@Service
public class KakaoService {

	private String clientId;
	private final String KAUTH_TOKEN_URL_HOST;
	private final String KAUTH_USER_URL_HOST;

	@Autowired
	public KakaoService(@Value("${kakao.client_id}") String clientId) {
		this.clientId = clientId;
		KAUTH_TOKEN_URL_HOST = "https://kauth.kakao.com";
		KAUTH_USER_URL_HOST = "https://kapi.kakao.com";
	}

	public String getAccessTokenFromKakao(String code) {

		KakaoTokenResponseDto kakaoTokenResponseDto = WebClient.create(KAUTH_TOKEN_URL_HOST).post()
				.uri(uriBuilder -> uriBuilder.scheme("https").path("/oauth/token")
						.queryParam("grant_type", "authorization_code").queryParam("client_id", clientId)
						.queryParam("code", code).build(true))
				.header(HttpHeaders.CONTENT_TYPE, HttpHeaderValues.APPLICATION_X_WWW_FORM_URLENCODED.toString())
				.retrieve()
				// TODO : Custom Exception
				.onStatus(HttpStatusCode::is4xxClientError,
						clientResponse -> Mono.error(new RuntimeException("Invalid Parameter")))
				.onStatus(HttpStatusCode::is5xxServerError,
						clientResponse -> Mono.error(new RuntimeException("Internal Server Error")))
				.bodyToMono(KakaoTokenResponseDto.class).block();

		log.info(" [Kakao Service] Access Token ------> {}", kakaoTokenResponseDto.getAccessToken());
		log.info(" [Kakao Service] Refresh Token ------> {}", kakaoTokenResponseDto.getRefreshToken());
		// 제공 조건: OpenID Connect가 활성화 된 앱의 토큰 발급 요청인 경우 또는 scope에 openid를 포함한 추가 항목 동의
		// 받기 요청을 거친 토큰 발급 요청인 경우
		log.info(" [Kakao Service] Id Token ------> {}", kakaoTokenResponseDto.getIdToken());
		log.info(" [Kakao Service] Scope ------> {}", kakaoTokenResponseDto.getScope());

		return kakaoTokenResponseDto.getAccessToken();
	}

	public KakaoUserInfoResponseDto getUserInfo(String accessToken) {

		// 카카오 API 호출
		KakaoUserInfoResponseDto userInfo = WebClient.create(KAUTH_USER_URL_HOST).get()
				.uri(uriBuilder -> uriBuilder.scheme("https").path("/v2/user/me").build(true))
				.header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken)
				.header(HttpHeaders.CONTENT_TYPE, HttpHeaderValues.APPLICATION_X_WWW_FORM_URLENCODED.toString())
				.retrieve()
				.onStatus(HttpStatusCode::is4xxClientError,
						clientResponse -> Mono.error(new RuntimeException("Invalid Parameter")))
				.onStatus(HttpStatusCode::is5xxServerError,
						clientResponse -> Mono.error(new RuntimeException("Internal Server Error")))
				.bodyToMono(KakaoUserInfoResponseDto.class).block();

		// userInfo가 null일 경우, 로그를 찍고 early return
		if (userInfo == null) {
			log.error("[ Kakao Service ] userInfo가 null입니다.");
			return null; // 추가적인 처리가 필요할 경우 예외를 던지거나 사용자에게 알림을 할 수 있음.
		}

		log.info("[ Kakao Service ] Auth ID ---> {}", userInfo.getId());

		// KakaoAccount가 null인지 확인
		if (userInfo.getKakaoAccount() != null) {
			// KakaoAccount가 null이 아닐 때만 Profile을 체크
			if (userInfo.getKakaoAccount().getProfile() != null) {
				String nickName = userInfo.getKakaoAccount().getProfile().getNickName();
				String profileImageUrl = userInfo.getKakaoAccount().getProfile().getProfileImageUrl();

				log.info("[ Kakao Service ] NickName ---> {}", nickName);
				log.info("[ Kakao Service ] ProfileImageUrl ---> {}", profileImageUrl);
			} else {
				log.warn("[ Kakao Service ] KakaoAccount에 Profile 정보가 없습니다.");
				// Profile이 없을 경우 기본값 설정 또는 다른 처리 방법 추가 가능
			}
		} else {
			log.warn("[ Kakao Service ] KakaoAccount 정보가 없습니다.");
			// KakaoAccount가 없을 경우 fallback 처리 추가
		}

		return userInfo;
	}

}