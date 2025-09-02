package com.shop.tradezone.dto;

import com.shop.tradezone.entity.Member;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class MemberUpdateDto {

	private Long id;

	@NotBlank(message = "비밀번호를 입력해 주세요.")
	@Pattern(regexp = "^(?=.*[A-Za-z])(?=.*\\d)(?=.*[~!@#$%^&*()+|=]).{8,20}$", message = "비밀번호는 8~20자로 영문, 숫자, 특수기호를 조합해 주세요.")
	private String password;

	@NotBlank(message = "연락처를 입력해 주세요.")
	private String phone;

	@NotBlank(message = "닉네임을 입력해 주세요.")
	private String username;

	private String email;

	public MemberUpdateDto(Member member) {

		this.id = member.getId();
		this.password = member.getPassword();
		this.phone = member.getPhone();
		this.username = member.getUsername();
	}
}