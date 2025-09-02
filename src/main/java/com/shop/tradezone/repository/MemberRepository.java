package com.shop.tradezone.repository;

import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import com.shop.tradezone.entity.Member;

public interface MemberRepository extends JpaRepository<Member, Long> {

	Optional<Member> findByKakaoId(String kakaoId);

	Optional<Member> findByUsername(String username);

	Optional<Member> findByEmail(String email);

	boolean existsByEmail(String email);
	// 해당 이메일을 가진 회원이 존재하는지 확인 (중복 체크)

	boolean existsByUsername(String username);
	// 해당 닉네임이 이미 존재하는지 확인 (중복 체크)

	Page<Member> findAll(Pageable pageable);
	// 관리자페이지에서 회원목록 조회

	Member findByEmailAndPhone(String email, String phone);

}
