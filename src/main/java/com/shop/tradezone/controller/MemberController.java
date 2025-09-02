package com.shop.tradezone.controller;

import java.util.ArrayList;
import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.shop.tradezone.constant.Role;
import com.shop.tradezone.dto.MemberFormDto;
import com.shop.tradezone.dto.MemberUpdateDto;
import com.shop.tradezone.service.MemberPrincipal;
import com.shop.tradezone.service.MemberService;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;

@Log4j2
@Controller
@RequiredArgsConstructor
@RequestMapping("/member")
public class MemberController {

	private final MemberService memberService;
	private final PasswordEncoder passwordEncoder;
	private final HttpServletRequest request;

	// 회원가입
	@GetMapping("/join")
	public String join(Model model) {
		model.addAttribute("memberFormDto", new MemberFormDto());

		return "join";
	}

	@PostMapping("/join")
	public String join(@Valid @ModelAttribute MemberFormDto dto, BindingResult bindingResult, Model model,
			RedirectAttributes redirectAttributes) {

		if (bindingResult.hasErrors()) {
			return "join";
		}

		try {
			memberService.create(dto.getEmail(), dto.getPassword(), dto.getUsername(), dto.getPhone(), Role.USER);

		} catch (IllegalStateException e) {
			e.printStackTrace();
			bindingResult.rejectValue("email", "emailDuplicate", "이미 사용 중인 이메일입니다.");
			return "join";

		} catch (Exception e) {
			e.printStackTrace();
			bindingResult.reject("joinFailed", "오류가 발생했습니다. 잠시 후 다시 시도해주세요.");
			return "join";
		}

		redirectAttributes.addFlashAttribute("message", "회원가입에 성공했습니다!");
		return "redirect:/member/login";
	}

	@Value("${kakao.client_id}")
	private String client_id;

	@Value("${kakao.redirect_uri}")
	private String redirect_uri;

	// 로그인
	@GetMapping("/login")
	public String login(Model model) {
		String location = "https://kauth.kakao.com/oauth/authorize?response_type=code&client_id=" + client_id
				+ "&redirect_uri=" + redirect_uri;
		model.addAttribute("location", location);
		return "login";
	}

	// 회원정보 수정
	@PreAuthorize("isAuthenticated()") // 로그인한 사용자만 접근 가능
	@GetMapping("/mypage")
	public String modify(@AuthenticationPrincipal MemberPrincipal memberPrincipal, Model model) {
		// @AuthenticationPrincipal → Spring Security에서 인증된 사용자 정보를
		// 컨트롤러 메서드의 파라미터로 쉽게 주입받을 수 있도록 하는 어노테이션

		String email = memberPrincipal.getUsername();

		model.addAttribute("email", email); // 화면 표시용
		model.addAttribute("memberUpdateDto", memberService.getMemberInfoByEmail(email));

		return "mypage";
	}

	@PreAuthorize("isAuthenticated()")
	@PostMapping("/mypage")
	public String modify(@Valid MemberUpdateDto dto, BindingResult bindingResult,
			@AuthenticationPrincipal MemberPrincipal memberPrincipal, RedirectAttributes redirectAttributes) {

		if (bindingResult.hasErrors()) {
			return "mypage";
		}

		try {
			memberService.modify(dto, memberPrincipal.getUsername());

		} catch (Exception e) {
			e.printStackTrace();
			bindingResult.reject("modifyFailed", "오류가 발생했습니다. 잠시 후 다시 시도해주세요.");
			return "mypage";
		}

		redirectAttributes.addFlashAttribute("message", "회원정보가 성공적으로 수정되었습니다.");
		return "redirect:/member/mypage";
	}

	// 마이페이지 상품목록, 찜목록, 후기목록 조회
	@PreAuthorize("isAuthenticated()")
	@GetMapping("/list")
	public String myPage(@AuthenticationPrincipal MemberPrincipal principal, Model model) {

		String email = principal.getUsername();

		MemberFormDto myPageData = memberService.getMyPageData(email);

		if (myPageData == null)
			myPageData = new MemberFormDto();

		if (myPageData.getItems() == null)
			myPageData.setItems(new ArrayList<>());

		if (myPageData.getLikes() == null)
			myPageData.setLikes(new ArrayList<>());

		if (myPageData.getReview() == null)
			myPageData.setReview(new ArrayList<>());

		model.addAttribute("myPageData", myPageData);

		return "mypage";
	}

	// 이메일 본인 인증
	@GetMapping("/updateidentity")
	public String verifyIdentity() {
		return "passwordreset";
	}

	@ResponseBody
	@PostMapping("/updateidentity")
	public ResponseEntity<?> verifyIdentity(@RequestBody MemberUpdateDto dto, HttpServletRequest request) {

		String email = dto.getEmail();
		String phone = dto.getPhone();

		try {
			dto = memberService.findByEmailAndPhone(email, phone);

			if (dto == null) {
				log.info("일치하는 정보 없음");

				return ResponseEntity.ok(Map.of("success", false, "message", "일치하는 정보가 없습니다."));
			}

			// 인증번호 생성 및 이메일 전송
			String verificationCode = String.format("%06d", (int) (Math.random() * 1000000));
			memberService.sendVerificationCode(email, verificationCode);

			// 세션에 인증번호 저장
			HttpSession session = request.getSession();
			session.setAttribute("verificationCode", verificationCode);
			session.setAttribute("email", email);

			log.info("본인 확인 성공 - 이메일: {}", email);

			return ResponseEntity.ok(Map.of("success", true));

		} catch (Exception e) {
			log.error("본인 확인 중 오류 발생: ", e);

			return ResponseEntity.ok(Map.of("success", false, "message", "본인 확인 중 오류가 발생했습니다."));
		}
	}

	// 인증번호 확인
	@ResponseBody
	@PostMapping("/passwordcode")
	public ResponseEntity<?> verifyCode(
			@RequestParam(value = "verificationCode", required = false) String verificationCode,
			HttpServletRequest request) {
		try {
			HttpSession session = request.getSession();

			String savedCode = (String) session.getAttribute("verificationCode");

			if (savedCode == null || !savedCode.equals(verificationCode)) {
				return ResponseEntity.ok(Map.of("success", false, "message", "인증번호가 일치하지 않습니다."));
			}

			return ResponseEntity.ok(Map.of("success", true));

		} catch (Exception e) {
			log.error("인증번호 확인 중 오류 발생: ", e);

			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("success", false, "message", "인증번호 확인 중 오류가 발생했습니다."));
		}
	}

	// 새 비밀번호 변경
	@ResponseBody
	@PostMapping("/resetpassword")
	public ResponseEntity<?> resetPassword(@RequestParam("newPassword") String newPassword,
			@RequestParam("confirmPassword") String confirmPassword, HttpServletRequest request) {

		try {

			if (!newPassword.equals(confirmPassword)) {
				return ResponseEntity.ok(Map.of("success", false, "message", "비밀번호가 일치하지 않습니다."));
			}

			HttpSession session = request.getSession();
			String email = (String) session.getAttribute("email");

			if (email == null) {
				return ResponseEntity.ok(Map.of("success", false, "message", "시간이 만료된 요청입니다."));
			}

			// 비밀번호 변경
			String password = passwordEncoder.encode(newPassword);
			memberService.passwordreset(email, password);

			session.removeAttribute("verificationCode");
			session.removeAttribute("email");

			return ResponseEntity.ok(Map.of("success", true));

		} catch (Exception e) {
			log.error("비밀번호 변경 중 오류 발생: ", e);

			return ResponseEntity.ok(Map.of("success", false, "message", "비밀번호 변경 중 오류가 발생했습니다."));
		}
	}
}