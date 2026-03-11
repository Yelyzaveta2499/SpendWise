package com.example.SpendWise.controller;

import com.example.SpendWise.service.ReportsService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/reports")
public class ReportsController {

    private final ReportsService reportsService;

    public ReportsController(ReportsService reportsService) {
        this.reportsService = reportsService;
    }

    /**
     * GET /api/reports/data?range=6months|12months|ytd
     */
    @GetMapping("/data")
    public ResponseEntity<Map<String, Object>> getData(
            Authentication authentication,
            @RequestParam(value = "range", required = false, defaultValue = "6months") String range,
            @RequestParam(value = "from",  required = false) String from,
            @RequestParam(value = "to",    required = false) String to) {

        String username = authentication.getName();
        Map<String, Object> result = reportsService.buildReport(username, range, from, to);
        return ResponseEntity.ok(result);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, String>> handleBadRequest(IllegalArgumentException ex) {
        Map<String, String> error = new HashMap<>();
        error.put("error", ex.getMessage());
        return ResponseEntity.badRequest().body(error);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, String>> handleError(Exception ex) {
        Map<String, String> error = new HashMap<>();
        error.put("error", "Failed to load report data: " + ex.getMessage());
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
    }
}


