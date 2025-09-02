package com.shop.tradezone.entity;

import java.time.LocalDateTime;
import java.util.List;

import com.shop.tradezone.constant.LoginType;
import com.shop.tradezone.constant.Role;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Builder
@Getter
@Setter
@Entity
@NoArgsConstructor
@AllArgsConstructor
public class Member {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(unique = true)
	private String kakaoId; // 카카오 로그인용 아이디

	@Enumerated(EnumType.STRING)
	private LoginType loginType; // "local", "kakao" 등 구분

	@Column(unique = true)
	private String email;

	@Column(unique = true)
	private String username;

	private String password;

	private String phone;

	@Column(columnDefinition = "TIMESTAMP DEFAULT CURRENT_TIMESTAMP")
	private LocalDateTime created;

	@Enumerated(EnumType.STRING)
	private Role role;

	@OneToMany(mappedBy = "seller")
	private List<Item> items;

	@OneToMany(mappedBy = "member")
	private List<Like> likes;

	@OneToMany(mappedBy = "member")
	private List<Review> reviews;

	// 양방향 매핑 (옵션)
	@OneToMany(mappedBy = "member", cascade = CascadeType.ALL, orphanRemoval = true)
	private List<Notice> notices;

//   @OneToMany(mappedBy = "")
//   private List<Chat> chats;
//
//   @OneToMany(mappedBy = "sender")
//   private List<Message> messages;

	public static Member create(String email, String encodedPassword, String username, String phone, Role role) {
		Member member = new Member();
		member.setEmail(email);
		member.setPassword(encodedPassword);
		member.setUsername(username);
		member.setPhone(phone);
		member.setCreated(LocalDateTime.now());
		member.setRole(Role.USER);
		member.setLoginType(LoginType.LOCAL);
		return member;
	}
}