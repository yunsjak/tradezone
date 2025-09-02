package com.shop.tradezone.service;

import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import com.shop.tradezone.entity.Member;
import com.shop.tradezone.repository.MemberRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;

@RequiredArgsConstructor
@Log4j2
@Service("customAdminDetailsService")
public class CustomUserDetailsService implements UserDetailsService {

	private final MemberRepository memberRepository;

	@Override
	public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {

		Member member = memberRepository.findByEmailSe(email);

		if (member == null) {
			log.error("사용자를 찾을 수 없습니다: {}", email);
			throw new UsernameNotFoundException("사용자를 찾을 수 없습니다: " + email);
		}

		return new CustomUserDetails(member);
	}
}