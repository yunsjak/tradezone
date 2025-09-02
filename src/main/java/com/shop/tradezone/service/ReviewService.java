package com.shop.tradezone.service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.shop.tradezone.dto.ReviewFormDto;
import com.shop.tradezone.entity.Item;
import com.shop.tradezone.entity.Member;
import com.shop.tradezone.entity.Review;
import com.shop.tradezone.repository.ReviewRepository;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;

@Log4j2
@Service
@Transactional
@RequiredArgsConstructor
public class ReviewService {

	private final ReviewRepository reviewRepository;

	// =================== 관리자 ===================

	// 전체 댓글목록 조회
	@Transactional(readOnly = true)
	@PreAuthorize("hasRole('ADMIN')")
	public Page<Review> getAllReviews(int page) {

		List<Sort.Order> sorts = new ArrayList<>();
		sorts.add(Sort.Order.desc("id")); // 내림차순 정렬
		Pageable pageable = PageRequest.of(page, 10, Sort.by(sorts)); // 한 페이지에 10개

		return reviewRepository.findAll(pageable);
	}

	// 댓글 삭제
	@PreAuthorize("hasRole('ADMIN')")
	public void deletReview(Long id) {
		reviewRepository.deleteById(id);
	}

	// ====================================

	public List<Review> getAll() {
		return reviewRepository.findAll();
	}

	// 특정 리뷰 조회하기
	// 리뷰 ID로 해당 리뷰를 조회하는 메서드
	@Transactional(readOnly = true)
	public Review getById(Long id) {
		// 존재하지 않으면 404에 해당하는 예외
		return reviewRepository.findById(id).orElseThrow(() -> new EntityNotFoundException("리뷰를 찾을 수 없습니다. id=" + id));
	}

	@Transactional(readOnly = true)
	public List<ReviewFormDto> getByItem(Item item) {
		List<Review> review = reviewRepository.findByItemOrderByCreatedDesc(item);
		List<ReviewFormDto> dtoList = new ArrayList<ReviewFormDto>();

		for (Review r : review) {
			ReviewFormDto redto = new ReviewFormDto();
			redto.setUsername(r.getMember().getUsername());
			log.info("엔티티 네임 : " + r.getMember().getUsername());// Member가 null일 수 있으니 체크 필요
			redto.setContent(r.getContent()); // 예시로 content도 넣어봄
			redto.setCreated(r.getCreated());

			dtoList.add(redto);
			log.info("여기가 디티오" + redto.getUsername());
		}
		return dtoList;
	}

	@Transactional(readOnly = true)
	public List<Review> getByMember(Member member) {
		return reviewRepository.findByMemberOrderByCreatedDesc(member);
	}

	// 상품목록 리스트 출력(마이페이지, 상품페이지 컨트롤러)
	public List<Review> getReviewListByUser(Member member) {
		// 사용자가 작성한 리뷰들을 작성일시 기준 내림차순으로 조회
		return reviewRepository.findByMemberOrderByCreatedDesc(member);
	}

	// 리뷰작성
	@Transactional
	public Review create(Item item, Member member, String content) {
		// 나중에 수정 필요: item null이면 예외 던질지, 특정 정책으로 처리할지 결정
		if (member == null) {
			throw new AccessDeniedException("로그인이 필요합니다.");
		}
		if (content == null || content.trim().length() < 2) {
			throw new IllegalArgumentException("내용은 2자 이상이어야 합니다.");
		}

		Review review = new Review();
		review.setItem(item);
		review.setMember(member);
		review.setContent(content.trim());
		review.setCreated(LocalDateTime.now());
		review.setModified(null);
		return reviewRepository.save(review);
	}

	// 리뷰 수정 메서드
	@Transactional
	public Review update(Long id, Member editor, String content) {
		Review review = getById(id);

		// 작성자 검증
		if (editor == null || !review.getMember().getId().equals(editor.getId())) {
			// 서비스 레이어에서 명확한 권한 예외
			throw new AccessDeniedException("리뷰 수정 권한이 없습니다.");
		}

		if (content == null || content.trim().length() < 2) {
			throw new IllegalArgumentException("내용은 2자 이상이어야 합니다.");
		}

		review.setContent(content.trim());
		review.setModified(LocalDateTime.now());
		return review; // JPA dirty checking으로 업데이트 반영
	}

	// 리뷰 삭제
	@Transactional
	public void delete(Long id, Member requester) {
		Review review = getById(id);

		if (requester == null || !review.getMember().getId().equals(requester.getId())) {
			throw new AccessDeniedException("리뷰 삭제 권한이 없습니다.");
		}
		reviewRepository.delete(review);
	}

	// 아이템 삭제 시 해당 아이템에 달린 리뷰 삭제
	// 상품이 삭제될 때 해당 상품과 관련된 모든 리뷰를 삭제
	@Transactional
	public void deleteByItems(Item items) {
		reviewRepository.deleteByItem(items);
	}
}