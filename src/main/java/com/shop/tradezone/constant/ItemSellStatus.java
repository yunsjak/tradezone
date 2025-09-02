package com.shop.tradezone.constant;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum ItemSellStatus {

	SELL("판매중"), 
	SOLD_OUT("판매완료"),
	TRADING("거래진행중"),
	STOPPED("거래중지"),
	COMPLETED("구매완료");

	private final String description;
}