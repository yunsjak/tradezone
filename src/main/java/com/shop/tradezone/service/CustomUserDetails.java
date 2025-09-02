package com.shop.tradezone.service;

import java.util.Collection;
import java.util.List;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import com.shop.tradezone.entity.Member;

public class CustomUserDetails implements UserDetails {

	private final Member member;

	public CustomUserDetails(Member member) {
		this.member = member;
	}

	@Override
	public Collection<? extends GrantedAuthority> getAuthorities() {
		// Member의 Role을 권한으로 변환해서 리턴
		return List.of(new SimpleGrantedAuthority(member.getRole().name()));
	}

	@Override
	public String getPassword() {
		return member.getPassword(); // 로컬 로그인 시만 필요
	}

	@Override
	public String getUsername() {
		return member.getUsername(); // 혹은 member.getEmail()
	}

	@Override
	public boolean isAccountNonExpired() {
		return true; // 필요에 따라 수정
	}

	@Override
	public boolean isAccountNonLocked() {
		return true; // 필요에 따라 수정
	}

	@Override
	public boolean isCredentialsNonExpired() {
		return true; // 필요에 따라 수정
	}

	@Override
	public boolean isEnabled() {
		return true; // 필요에 따라 수정
	}

	public Member getMember() {
		return this.member;
	}
}
