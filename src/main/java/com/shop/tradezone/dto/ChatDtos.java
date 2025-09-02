package com.shop.tradezone.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public class ChatDtos {

	public record SendMessageReq(@NotNull Long roomId, @NotBlank String content) {
	}

	public record CreateRoomReq(@NotNull Long itemId, @NotNull Long buyerId, @NotNull Long sellerId) {
	}
}
