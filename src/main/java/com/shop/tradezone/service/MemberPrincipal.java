package com.shop.tradezone.service;

import java.util.Collection;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import com.shop.tradezone.entity.Member;

public class MemberPrincipal implements UserDetails {
	private final Member member;
	private final Collection<? extends GrantedAuthority> auth;

	public MemberPrincipal(Member member, Collection<? extends GrantedAuthority> auth) {
		this.member = member;
		this.auth = auth;
//		this.enabled = enabled;
	}

	public Long getMemberId() {
		return member.getId();
	}

	@Override
	public Collection<? extends GrantedAuthority> getAuthorities() {
		return auth;
	}

	@Override
	public String getPassword() {
		return member.getPassword();
	}

	@Override
	public String getUsername() {
		return member.getUsername();
	}

	@Override
	public boolean isAccountNonExpired() {
		return true;
	}

	@Override
	public boolean isAccountNonLocked() {
		return true;
	}

	@Override
	public boolean isCredentialsNonExpired() {
		return true;
	}

//	@Override
//	public boolean isEnabled() {
//		return enabled;
//	}
}
