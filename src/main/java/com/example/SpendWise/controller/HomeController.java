package com.example.SpendWise.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class HomeController {
    @GetMapping("/")
    public String index() {
        // Forward to static index.html under src/main/resources
        return "index";
    }

}
