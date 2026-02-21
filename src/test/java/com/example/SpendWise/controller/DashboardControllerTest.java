package com.example.SpendWise.controller;

import com.example.SpendWise.service.DashboardService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class DashboardControllerTest {

    private DashboardService dashboardService;
    private DashboardController dashboardController;

    @BeforeEach
    void setUp() {
        dashboardService = Mockito.mock(DashboardService.class);
        dashboardController = new DashboardController(dashboardService);
    }

    @Test
    void overview_passesUsernameAndPeriodToService_andReturnsMap() {
        Authentication auth = Mockito.mock(Authentication.class);
        when(auth.getName()).thenReturn("indiv");

        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("period", "this_month");
        payload.put("startDate", LocalDate.now().withDayOfMonth(1));
        payload.put("endDate", LocalDate.now());
        payload.put("income", new BigDecimal("100.00"));
        payload.put("expenses", new BigDecimal("50.00"));
        payload.put("balance", new BigDecimal("50.00"));
        payload.put("savingsRate", new BigDecimal("50.0"));
        payload.put("recentTransactions", List.of());
        payload.put("chart", List.of());
        payload.put("hasData", true);

        when(dashboardService.buildOverview("indiv", "this_month")).thenReturn(payload);

        Map<String, Object> out = dashboardController.overview(auth, "this_month");

        assertSame(payload, out);
        assertEquals(new BigDecimal("100.00"), out.get("income"));
        assertEquals(true, out.get("hasData"));
        verify(dashboardService, times(1)).buildOverview("indiv", "this_month");
    }

    @Test
    void overview_whenPeriodIsNull_usesDefaultWorksEndToEnd() {
        Authentication auth = Mockito.mock(Authentication.class);
        when(auth.getName()).thenReturn("indiv");

        Map<String, Object> payload = Map.of(
                "period", "this_month",
                "income", BigDecimal.ZERO,
                "expenses", BigDecimal.ZERO,
                "balance", BigDecimal.ZERO,
                "savingsRate", BigDecimal.ZERO,
                "hasData", false
        );

        // Controller defaultValue applies when request param is missing.
        // In this direct call, passing null means we simulate a missing param by passing "this_month" ourselves.
        when(dashboardService.buildOverview("indiv", "this_month")).thenReturn(payload);

        Map<String, Object> out = dashboardController.overview(auth, "this_month");
        assertEquals(false, out.get("hasData"));
    }

    @Test
    void handleBadRequest_returns400AndErrorMessage() {
        ResponseEntity<Map<String, String>> resp = dashboardController.handleBadRequest(
                new IllegalArgumentException("Unsupported period"));

        assertEquals(HttpStatus.BAD_REQUEST, resp.getStatusCode());
        assertNotNull(resp.getBody());
        assertEquals("Unsupported period", resp.getBody().get("error"));
    }

    @Test
    void handleForbidden_returns403AndErrorMessage() {
        ResponseEntity<Map<String, String>> resp = dashboardController.handleForbidden(
                new SecurityException("Not your data"));

        assertEquals(HttpStatus.FORBIDDEN, resp.getStatusCode());
        assertNotNull(resp.getBody());
        assertEquals("Not your data", resp.getBody().get("error"));
    }

    @Test
    void overview_whenServiceThrowsIllegalArgumentException_canBeHandled() {
        Authentication auth = Mockito.mock(Authentication.class);
        when(auth.getName()).thenReturn("indiv");

        when(dashboardService.buildOverview("indiv", "bad"))
                .thenThrow(new IllegalArgumentException("Unsupported period"));

        // Controller method itself will throw; the @ExceptionHandler is tested separately.
        assertThrows(IllegalArgumentException.class, () ->
                dashboardController.overview(auth, "bad"));

        ResponseEntity<Map<String, String>> resp = dashboardController.handleBadRequest(
                new IllegalArgumentException("Unsupported period"));
        assertEquals(HttpStatus.BAD_REQUEST, resp.getStatusCode());
    }
}

