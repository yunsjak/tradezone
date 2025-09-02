package com.shop.tradezone.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.shop.tradezone.constant.Role;
import com.shop.tradezone.dto.ItemCardDto;
import com.shop.tradezone.dto.LikeFormDto;
import com.shop.tradezone.dto.MemberFormDto;
import com.shop.tradezone.dto.MemberUpdateDto;
import com.shop.tradezone.dto.MyPageDto;
import com.shop.tradezone.dto.ReviewFormDto;
import com.shop.tradezone.entity.Member;
import com.shop.tradezone.repository.MemberRepository;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;

@Log4j2
@Service
@Transactional
@RequiredArgsConstructor
public class MemberService {

	private final MemberRepository memberRepository;

	private final PasswordEncoder passwordEncoder;

	public Member findByUsername(String username) {
		return memberRepository.findByUsername(username)
				.orElseThrow(() -> new IllegalArgumentException("해당 이름의 관리자 없음: " + username));
	}

	public Member findByEmail(String email) {
		return memberRepository.findByEmail(email)
				.orElseThrow(() -> new IllegalArgumentException("해당 이메일의 관리자 없음: " + email));
	}

	// 회원가입
	public Member create(String email, String password, String username, String phone, Role role) {

		if (memberRepository.existsByEmail(email)) {
			throw new IllegalStateException("이미 사용 중인 이메일입니다.");
		}

		String encodedPassword = passwordEncoder.encode(password);
		Member member = Member.create(email, encodedPassword, username, phone, role);

		return memberRepository.save(member);
	}

	// 회원정보 조회
	@Transactional(readOnly = true)
	public MemberUpdateDto getMemberInfoByEmail(String email) {

		Member member = memberRepository.findByEmail(email)
				.orElseThrow(() -> new EntityNotFoundException("회원 정보를 찾을 수 없습니다."));
		// orElseThrow() → Optional 클래스 메서드 / 값이 존재하면 반환, 없으면 예외 발생

		return new MemberUpdateDto(member);
	}

	// 회원정보 수정
	public void modify(MemberUpdateDto dto, String email) {

		Member member = memberRepository.findByEmail(email)
				.orElseThrow(() -> new EntityNotFoundException("회원 정보를 찾을 수 없습니다."));

		if (dto.getPassword() != null && !dto.getPassword().isBlank()) {
			String encodePassword = passwordEncoder.encode(dto.getPassword());
			member.setPassword(encodePassword);
		}

		member.setPhone(dto.getPhone());
		member.setUsername(dto.getUsername());
	}

	// 닉네임 중복체크
	public boolean isUsernameDuplicate(String username) {
		return memberRepository.existsByUsername(username);
	}

	// 상품목록, 찜목록, 후기목록 조회
	@Transactional(readOnly = true)
	public MyPageDto getMyPageData(String email) {

		MyPageDto dto = new MyPageDto();
		dto.setEmail(email);
		dto.setItems(List.of());
		dto.setLikes(List.of());
		dto.setReview(List.of());

		try {
			Member member = memberRepository.findByEmail(email)
					.orElseThrow(() -> new EntityNotFoundException("회원 정보를 찾을 수 없습니다."));

			dto.setEmail(member.getEmail());
			
			// 상품 목록
			if (member.getItems() != null) {
				dto.setItems(member.getItems().stream().map(ItemCardDto::new).toList());
			}
			
			// 찜 목록
			if (member.getLikes() != null) {
				dto.setLikes(member.getLikes().stream().map(like -> {
					LikeFormDto likeDto = new LikeFormDto(like);
					return likeDto;
				}).toList());
			}
			
			// 후기 목록
			if (member.getReviews() != null) {
				dto.setReview(member.getReviews().stream().map(ReviewFormDto::new).toList());
			}

		} catch (Exception e) {
			log.error("마이페이지 데이터 조회 실패: {}", e.getMessage(), e);
		}

		return dto;
	}

	// 이메일 인증 회원 조회
	public MemberUpdateDto findByEmailAndPhone(String email, String phone) {

		Member member = memberRepository.findByEmailAndPhone(email, phone);
		MemberUpdateDto dto = new MemberUpdateDto();

		dto.setEmail(member.getEmail());
		dto.setPhone(member.getPhone());

		return dto;
	}

	// 구글 SMTP
	private JavaMailSender getHardcodedMailSender() {
		// JavaMailSender → Spring에서 메일 전송을 구현하는 인터페이스

		JavaMailSenderImpl mailSender = new JavaMailSenderImpl();
		// JavaMailSenderImpl → Spring에서 메일 전송을 구현하는 클래스

		mailSender.setHost("smtp.gmail.com"); // Gmail의 SMTP 서버 주소와 포트 설정
		mailSender.setPort(587); // TLS(STARTTLS)를 사용하는 표준 포트
		mailSender.setUsername("s2.xptmxm@gmail.com"); // 보내는 이메일
		mailSender.setPassword("quzkjehsmtaupatx"); // 앱 비밀번호(외부 노출되면 안 됨)⚠

		Properties props = mailSender.getJavaMailProperties();

		props.put("mail.transport.protocol", "smtp"); // smtp → 메일 전송 프로토콜
		props.put("mail.smtp.auth", "true"); // auth=true → 인증 필요
		props.put("mail.smtp.starttls.enable", "true"); // starttls.enable=true → TLS 암호화 사용
		props.put("mail.debug", "false"); // debug=false → 디버그 로그 비활성화

		return mailSender;
	}

	// 인증 코드 생성
	@Async // @Async → 특정 함수(또는 메소드)를 비동기적으로 실행하게 만드는 어노테이션
	public void sendVerificationCode(String email, String verificationCode) {

		String subject = "이메일 인증 코드";
		String content = String.format("""
				<html>
				<body>
				    <h2>이메일 인증 코드</h2>
				    <p>아래의 인증 코드를 입력해 주세요:</p>
				    <h3 style="color: #1D6F42;">%s</h3>
				    <p>이 인증 코드는 5분간 유효합니다.</p>
				</body>
				</html>
				""", verificationCode);

		email = "pdw05027@gmail.com"; // 테스트 하려면 받을 수 있는 이메일 선언해야 됨

		try {
			sendEmail(email, subject, content);

		} catch (MessagingException e) {
			throw new RuntimeException("이메일 발송 실패", e);
		}
	}

	// 이메일 발송
	public void sendEmail(String to, String subject, String content) throws MessagingException {

		MimeMessage message = getHardcodedMailSender().createMimeMessage(); // 이메일 메시지 생성
		MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
		// MimeMessageHelper → 이메일에 제목/본문 등을 쉽게 설정할 수 있게 도와주는 클래스

		helper.setTo(to); // 받는 사람
		helper.setSubject(subject); // 메일 제목
		helper.setText(content, true); // 메일 본문

		getHardcodedMailSender().send(message);
	}

	// 비밀번호 변경
	public void passwordreset(String email, String password) {

		Member member = memberRepository.findByEmail(email)
				.orElseThrow(() -> new EntityNotFoundException("회원 정보를 찾을 수 없습니다."));

		member.setPassword(password);
		memberRepository.save(member);

	}

	// =================== 관리자 ===================

	// 전체 회원목록 조회
	@Transactional(readOnly = true)
	@PreAuthorize("hasRole('ADMIN')")
	public Page<Member> getAllMembers(int page) {

		List<Sort.Order> sorts = new ArrayList<>();
		sorts.add(Sort.Order.desc("id")); // 내림차순 정렬
		Pageable pageable = PageRequest.of(page, 10, Sort.by(sorts)); // 한 페이지에 10개

		return memberRepository.findAll(pageable);
	}

	// 특정 회원정보 조회
	@Transactional(readOnly = true)
	@PreAuthorize("hasRole('ADMIN')")
	public MemberUpdateDto getMemberById(Long id) {

		Member member = memberRepository.findById(id)
				.orElseThrow(() -> new EntityNotFoundException("회원 정보를 찾을 수 없습니다."));

		return new MemberUpdateDto(member);
	}

	// 특정 회원정보 수정
	@PreAuthorize("hasRole('ADMIN')")
	public void updateMember(MemberUpdateDto dto, Long id) {

		Member member = memberRepository.findById(id)
				.orElseThrow(() -> new EntityNotFoundException("회원 정보를 찾을 수 없습니다."));

		if (dto.getPassword() != null && !dto.getPassword().isBlank()) {
			String encodePassword = passwordEncoder.encode(dto.getPassword());
			member.setPassword(encodePassword);
		}

		member.setPhone(dto.getPhone());
		member.setUsername(dto.getUsername());
	}

	// 회원 삭제
	@PreAuthorize("hasRole('ADMIN')")
	public void deleteMember(Long id) {
		memberRepository.deleteById(id);
	}
}