package com.example.SpendWise.controller;

import com.example.SpendWise.service.DashboardService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/dashboard")
public class DashboardController {

    private final DashboardService dashboardService;

    public DashboardController(DashboardService dashboardService) {
        this.dashboardService = dashboardService;
    }

    /**
     * Dashboard overview endpoint.
     * period: this_month | last_month | last_30 | this_year
     */
    @GetMapping("/overview")
    public Map<String, Object> overview(Authentication authentication,
                                        @RequestParam(value = "period", required = false, defaultValue = "this_month") String period) {
        String username = authentication.getName();
        return dashboardService.buildOverview(username, period);
    }

    @ExceptionHandler({IllegalArgumentException.class})
    public ResponseEntity<Map<String, String>> handleBadRequest(IllegalArgumentException ex) {
        Map<String, String> error = new HashMap<>();
        error.put("error", ex.getMessage());
        return ResponseEntity.badRequest().body(error);
    }

    @ExceptionHandler({SecurityException.class})
    public ResponseEntity<Map<String, String>> handleForbidden(SecurityException ex) {
        Map<String, String> error = new HashMap<>();
        error.put("error", ex.getMessage());
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(error);
    }
}

