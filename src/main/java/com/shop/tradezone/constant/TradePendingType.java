package com.shop.tradezone.constant;

// 무슨 대기상태인지
public enum TradePendingType {
	NONE, // 대기아님
	COMPLETE, // 완료요청대기
	CANCEL // 취소요청대기
}