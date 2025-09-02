package com.shop.tradezone.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.shop.tradezone.repository.TradeRepository;
import com.shop.tradezone.service.ChatRoomService;

import lombok.RequiredArgsConstructor;

@Controller
@RequiredArgsConstructor
public class ChatViewController {

	private final TradeRepository tradeRepo;
	private final ChatRoomService chatRoomService;

	@GetMapping("/chat/list")
	public String list() {
		return "chat_list";
	}

	@GetMapping("/chat")
	public String chatRoom(@RequestParam Long roomId, Model model) {
		model.addAttribute("roomId", roomId);
		// 거래 버튼용 tradeId 주입(있을 때만)
		tradeRepo.findByChatRoom_Id(roomId).ifPresent(t -> model.addAttribute("tradeId", t.getId()));
		return "chat";
	}

}