
package com.shop.tradezone.service;

import java.util.Optional;

import org.springframework.stereotype.Service;

import com.shop.tradezone.constant.LoginType;
import com.shop.tradezone.constant.Role;
import com.shop.tradezone.dto.KakaoUserInfoResponseDto;
import com.shop.tradezone.entity.Member;
import com.shop.tradezone.repository.MemberRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserKakaoService {

	private final MemberRepository memberRepository;

	public Member processKakaoUser(KakaoUserInfoResponseDto kakaoUserInfo) {
		String kakaoId = String.valueOf(kakaoUserInfo.getId());
		String email = kakaoUserInfo.getKakaoAccount().getEmail();
		String phone = kakaoUserInfo.getKakaoAccount().getPhoneNumber();

		if (phone != null && phone.startsWith("+82")) {
			String number = phone.substring(3).replaceAll("[^0-9]", "");
			number = "0" + number;
			phone = number.replaceFirst("(\\d{3})(\\d{4})(\\d+)", "$1-$2-$3");
		}

		String name = kakaoUserInfo.getKakaoAccount().getName();

		Optional<Member> userByKakaoId = memberRepository.findByKakaoId(kakaoId);
		if (userByKakaoId.isPresent()) {
			return userByKakaoId.get();
		}

		Optional<Member> userByEmail = memberRepository.findByEmail(email);
		if (userByEmail.isPresent()) {
			Member existingUser = userByEmail.get();
			existingUser.setKakaoId(kakaoId);
			existingUser.setLoginType(LoginType.KAKAO);
//			existingUser.setCreated(LocalDateTime.now());
			existingUser.setRole(Role.USER);
			existingUser.setPhone(phone);
			existingUser.setUsername(name);
			return memberRepository.save(existingUser);
		}
		Member newMember = Member.builder().email(email).kakaoId(kakaoId).loginType(LoginType.KAKAO).role(Role.USER)
				.phone(phone).username(name).build();

		log.info("여기를 보라 " + newMember);

//		.created(LocalDateTime.now())
		return memberRepository.save(newMember);
	}
}