package com.shop.tradezone.dto;

public class ChatNotificationDto {
	private Long roomId;
	private String userId;
	private String event;

	public ChatNotificationDto(Long roomId, String userId, String event) {
		this.roomId = roomId;
		this.userId = userId;
		this.event = event;
	}

	public Long getRoomId() {
		return roomId;
	}

	public String getUserId() {
		return userId;
	}

	public String getEvent() {
		return event;
	}
}