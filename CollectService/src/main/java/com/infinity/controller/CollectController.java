package com.infinity.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.infinity.service.CollectService;

@Controller
public class CollectController {

    private final CollectService collectService;

    public CollectController(CollectService collectService) {
        this.collectService = collectService;
    }

    @GetMapping("/collect")
    public String showCollectPage() {
        return "collect";
    }

    @PostMapping("/collect")
    public String processCollect(
            @RequestParam("virtualAddress") String virtualAddress,
            Model model) {

        String response = collectService.processCollect(virtualAddress);

        model.addAttribute("response", response);

        return "collect";
    }

//FOR POSTMAN********************
//@RestController
//@RequestMapping("/collect")
//public class CollectController {
//
//    private final CollectService collectService;
//
//    public CollectController(CollectService collectService) {
//        this.collectService = collectService;
//    }
//
//    @PostMapping
//    public String collect(
//            @RequestParam("virtualAddress") String virtualAddress) {
//
//        return collectService.processCollect(virtualAddress);
//    }
}