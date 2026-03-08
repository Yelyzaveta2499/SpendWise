package com.example.SpendWise.controller;

import com.example.SpendWise.service.BusinessAnalyticsService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

/**
 * Test class for BusinessController
 * Tests business-only access to analytics endpoints
 */
class BusinessControllerTest {

    private BusinessAnalyticsService businessAnalyticsService;
    private BusinessController businessController;
    private Authentication authentication;

    @BeforeEach
    void setup() {
        businessAnalyticsService = Mockito.mock(BusinessAnalyticsService.class);
        businessController = new BusinessController(businessAnalyticsService);
        authentication = Mockito.mock(Authentication.class);
        when(authentication.getName()).thenReturn("business");
    }

    @Test
    void getBusinessAnalytics_withBusinessUser_shouldReturnAnalytics() {

        Map<String, Object> mockAnalytics = new HashMap<>();
        Map<String, Object> stats = new HashMap<>();
        stats.put("totalExpenses", "$1,500.00");
        stats.put("activeTags", 5);
        stats.put("totalTransactions", 10);
        mockAnalytics.put("stats", stats);

        when(businessAnalyticsService.getBusinessAnalytics(anyString()))
                .thenReturn(mockAnalytics);


        ResponseEntity<Map<String, Object>> response = businessController.getBusinessAnalytics(authentication);


        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());

        @SuppressWarnings("unchecked")
        Map<String, Object> resultStats = (Map<String, Object>) response.getBody().get("stats");
        assertEquals("$1,500.00", resultStats.get("totalExpenses"));
        assertEquals(5, resultStats.get("activeTags"));
        assertEquals(10, resultStats.get("totalTransactions"));
    }

    @Test
    void getBusinessAnalytics_withIndividualUser_shouldAllowAccess() {
        // Given - Individual users should also be able to access if they have business tags
        when(authentication.getName()).thenReturn("indiv");

        Map<String, Object> mockAnalytics = new HashMap<>();
        Map<String, Object> stats = new HashMap<>();
        stats.put("totalExpenses", "$500.00");
        stats.put("activeTags", 2);
        stats.put("totalTransactions", 3);
        mockAnalytics.put("stats", stats);

        when(businessAnalyticsService.getBusinessAnalytics(anyString()))
                .thenReturn(mockAnalytics);


        ResponseEntity<Map<String, Object>> response = businessController.getBusinessAnalytics(authentication);


        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().containsKey("stats"));
    }

    @Test
    void getBusinessAnalytics_withEmptyData_shouldReturnEmptyAnalytics() {

        Map<String, Object> emptyAnalytics = new HashMap<>();
        Map<String, Object> stats = new HashMap<>();
        stats.put("totalExpenses", "$0.00");
        stats.put("activeTags", 0);
        stats.put("totalTransactions", 0);
        emptyAnalytics.put("stats", stats);

        when(businessAnalyticsService.getBusinessAnalytics(anyString()))
                .thenReturn(emptyAnalytics);


        ResponseEntity<Map<String, Object>> response = businessController.getBusinessAnalytics(authentication);


        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());

        @SuppressWarnings("unchecked")
        Map<String, Object> resultStats = (Map<String, Object>) response.getBody().get("stats");
        assertEquals("$0.00", resultStats.get("totalExpenses"));
        assertEquals(0, resultStats.get("activeTags"));
    }

    @Test
    void getBusinessAnalytics_shouldReturnAllRequiredFields() {

        Map<String, Object> mockAnalytics = new HashMap<>();
        mockAnalytics.put("stats", new HashMap<>());
        mockAnalytics.put("expenseTags", new HashMap<>());
        mockAnalytics.put("spendingByTag", new HashMap<>());
        mockAnalytics.put("categoryData", new HashMap<>());
        mockAnalytics.put("recentExpenses", new HashMap<>());
        mockAnalytics.put("monthlyTagData", new HashMap<>());
        mockAnalytics.put("incomeExpensesData", new HashMap<>());

        when(businessAnalyticsService.getBusinessAnalytics(anyString()))
                .thenReturn(mockAnalytics);


        ResponseEntity<Map<String, Object>> response = businessController.getBusinessAnalytics(authentication);


        assertEquals(HttpStatus.OK, response.getStatusCode());
        Map<String, Object> body = response.getBody();
        assertNotNull(body);
        assertTrue(body.containsKey("stats"));
        assertTrue(body.containsKey("expenseTags"));
        assertTrue(body.containsKey("spendingByTag"));
        assertTrue(body.containsKey("categoryData"));
        assertTrue(body.containsKey("recentExpenses"));
        assertTrue(body.containsKey("monthlyTagData"));
        assertTrue(body.containsKey("incomeExpensesData"));
    }
}



