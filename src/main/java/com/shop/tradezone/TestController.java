package com.shop.tradezone;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

import lombok.RequiredArgsConstructor;

@Controller
@RequiredArgsConstructor
public class TestController {

	@GetMapping("/")
	public String index() {
		return "main";
	}

}
