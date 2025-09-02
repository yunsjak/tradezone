package com.shop.tradezone.constant;

/** 거래 상태 (채팅방과 연결되어 흐름 제어) */
public enum TradeStatus {
	PENDING, // 기본 진행(채팅/협의 중)
	COMPLETE_REQUESTED, // 완료 요청 대기 (상대 승인/거절 필요)
	CANCEL_REQUESTED, // 취소(무산) 요청 대기 (상대 승인/거절 필요)
	COMPLETED, // 양측 확정 완료
	CANCELED, // 양측 확정 종료(무산)
	ENDED // 거래종료
}