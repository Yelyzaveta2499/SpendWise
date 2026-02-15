package com.example.SpendWise.controller;

import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class HomeController {
    // post-login loading endpoint
    @GetMapping("/post-login")
    public String postLogin(Authentication authentication, Model model) {
        String accountType = "INDIVIDUAL";
        String accountMessage = "Loading your personal SpendWise experience...";

        if (authentication != null && authentication.getAuthorities() != null) {
            boolean isBusiness = authentication.getAuthorities().stream()
                    .anyMatch(a -> a.getAuthority().equals("ROLE_BUSINESS"));
            //boolean isKids = authentication.getAuthorities().stream()
                    //.anyMatch(a -> a.getAuthority().equals("ROLE_KIDS"));

            if (isBusiness) {
                accountType = "BUSINESS";
                accountMessage = "Loading business insights and cash flow overview...";
            }
            //else if (isKids) {
                //accountType = "KIDS";
                //accountMessage = "Loading your fun money tracker...";
            //}
        }

        model.addAttribute("accountType", accountType);
        model.addAttribute("accountMessage", accountMessage);

        return "loading";
    }

    @GetMapping("/")
    public String index() {
        // Forward to static index.html
        return "index";
    }

}
