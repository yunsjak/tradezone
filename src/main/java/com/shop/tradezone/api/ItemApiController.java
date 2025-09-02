package com.shop.tradezone.api;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.shop.tradezone.dto.ItemCardDto;
import com.shop.tradezone.service.ItemService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/items")
@RequiredArgsConstructor
public class ItemApiController {

	private final ItemService itemService;

	@GetMapping("/main")
	public List<ItemCardDto> getMainItems() {
		return itemService.getMainItems(0, 12).getContent();
	}
}
