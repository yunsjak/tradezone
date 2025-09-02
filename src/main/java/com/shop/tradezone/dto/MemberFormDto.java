package com.shop.tradezone.dto;

import java.util.List;

import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class MemberFormDto {

	@NotBlank(message = "이메일을 입력해 주세요.")
	@Pattern(regexp = "^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+.[A-Za-z]{2,6}$", message = "이메일 주소가 정확한지 확인해 주세요.")
	private String email;

	@NotBlank(message = "비밀번호를 입력해 주세요.")
	@Pattern(regexp = "^(?=.*[A-Za-z])(?=.*\\d)(?=.*[~!@#$%^&*()+|=]).{8,20}$", message = "비밀번호는 8~20자로 영문, 숫자, 특수기호를 조합해 주세요.")
	private String password;

	@NotBlank(message = "비밀번호를 입력해 주세요.")
	private String password_confirm;

	@AssertTrue(message = "비밀번호가 일치하지 않습니다.")
	public boolean isPasswordMatching() {
		return password != null && password.equals(password_confirm);
	}

	@NotBlank(message = "닉네임을 입력해 주세요.")
	private String username;

	@NotBlank(message = "연락처를 입력해 주세요.")
	private String phone;

	private List<ItemCardDto> items;

	private List<LikeFormDto> likes;

	private List<ReviewFormDto> review;

}