package com.shop.tradezone.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequestMapping("/toss")
public class TossController {

    @GetMapping("/success")
    public String paymentSuccess(@RequestParam("tossprice") String tossprice,
                                @RequestParam("itemId") String itemId,
                                @RequestParam("itemName") String itemName,
                                Model model) {
        model.addAttribute("tossprice", tossprice);
        model.addAttribute("itemId", itemId);
        model.addAttribute("itemName", itemName);
        return "toss/success";
    }
}
