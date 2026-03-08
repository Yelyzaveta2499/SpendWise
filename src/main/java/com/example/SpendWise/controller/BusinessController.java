package com.example.SpendWise.controller;

import com.example.SpendWise.service.BusinessAnalyticsService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/business")
public class BusinessController {

    private final BusinessAnalyticsService businessAnalyticsService;

    public BusinessController(BusinessAnalyticsService businessAnalyticsService) {
        this.businessAnalyticsService = businessAnalyticsService;
    }


    @GetMapping("/analytics")
    public ResponseEntity<Map<String, Object>> getBusinessAnalytics(Authentication authentication) {
        String username = authentication.getName();
        Map<String, Object> analytics = businessAnalyticsService.getBusinessAnalytics(username);
        return ResponseEntity.ok(analytics);
    }
}

