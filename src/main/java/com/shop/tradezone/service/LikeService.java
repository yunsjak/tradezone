package com.shop.tradezone.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.shop.tradezone.entity.Item;
import com.shop.tradezone.entity.Like;
import com.shop.tradezone.entity.Member;
import com.shop.tradezone.repository.LikeRepository;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Service
public class LikeService {

	private final LikeRepository likeRepository;

	// 찜토글@return true면 ON(생성됨), false면 OFF(삭제됨)
	public boolean toggle(Item item, Member member) {
		if (likeRepository.existsByItemAndMember(item, member)) {
			// 이미 찜되어 있으면 해제(OFF)
			likeRepository.deleteByItemAndMember(item, member);
			return false;
		} else {
			// 없으면 생성(ON)
			Like like = Like.builder().item(item).member(member).build();
			likeRepository.save(like);
			return true;
		}
	}

	// 현재 유저가 해당 아이템을 찜했는지 여부
	@Transactional(readOnly = true)
	public boolean isLiked(Item item, Member member) {
		return likeRepository.existsByItemAndMember(item, member);
	}

	// 찜 개수 가져오기
	// 특정 상품에 대해 몇 명이 찜했는지 개수를 반환하는 메서드
	public long countLikes(Item item) {
		// 특정 상품에 대한 찜 개수를 반환
		return likeRepository.countByItem(item);
	}

	// 유저의 찜 목록 가져오기 (마이페이지 용)
	// 사용자가 찜한 상품들의 목록을 가져오는 메서드
	public List<Like> getLikesByUser(Member member) {
		// 해당 사용자가 찜한 상품 목록을 반환
		return likeRepository.findByMember(member);
	}

	// 아이템 삭제 시 관련 찜도 삭제
	// 상품이 삭제될 때 해당 상품을 찜한 기록들도 함께 삭제하는 메서드
	public void deleteLikesByItem(Item item) {
		// 해당 상품에 대한 찜 기록들을 모두 삭제
		likeRepository.deleteByItem(item);
	}

}