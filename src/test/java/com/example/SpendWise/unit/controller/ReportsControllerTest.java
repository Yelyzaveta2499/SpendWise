package com.example.SpendWise.unit.controller;

import com.example.SpendWise.controller.ReportsController;
import com.example.SpendWise.service.ReportsService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Controller tests for ReportsController
 */
class ReportsControllerTest {

    private ReportsService reportsService;
    private ReportsController reportsController;
    private Authentication authentication;

    @BeforeEach
    void setUp() {
        reportsService    = Mockito.mock(ReportsService.class);
        reportsController = new ReportsController(reportsService);
        authentication    = Mockito.mock(Authentication.class);

        // Default: logged-in user is "indiv"
        when(authentication.getName()).thenReturn("indiv");
    }

    // ─────────────────────────────────────────────────────────────
    //  Acceptance Criteria 1 (Success):
    //  Given financial data exists, endpoint returns 200 with chart data
    // ─────────────────────────────────────────────────────────────

    @Test
    void getData_withData_returns200() {

        when(reportsService.buildReport(eq("indiv"), any(), any(), any()))
                .thenReturn(buildFakeReportWithData());


        ResponseEntity<Map<String, Object>> response =
                reportsController.getData(authentication, "6months", null, null);


        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
    }

    @Test
    void getData_withData_hasDataIsTrue() {

        when(reportsService.buildReport(eq("indiv"), any(), any(), any()))
                .thenReturn(buildFakeReportWithData());


        ResponseEntity<Map<String, Object>> response =
                reportsController.getData(authentication, "6months", null, null);

        // hasData must be true so the UI renders charts
        assertEquals(true, response.getBody().get("hasData"));
    }

    @Test
    void getData_withData_labelsArePresent() {

        when(reportsService.buildReport(eq("indiv"), any(), any(), any()))
                .thenReturn(buildFakeReportWithData());


        ResponseEntity<Map<String, Object>> response =
                reportsController.getData(authentication, "6months", null, null);


        List<?> labels = (List<?>) response.getBody().get("labels");
        assertNotNull(labels);
        assertFalse(labels.isEmpty());
    }

    @Test
    void getData_withData_statsArePresent() {

        when(reportsService.buildReport(eq("indiv"), any(), any(), any()))
                .thenReturn(buildFakeReportWithData());


        ResponseEntity<Map<String, Object>> response =
                reportsController.getData(authentication, "6months", null, null);

        // all four stat boxes must have values
        @SuppressWarnings("unchecked")
        Map<String, Object> stats = (Map<String, Object>) response.getBody().get("stats");
        assertNotNull(stats);
        assertTrue(stats.containsKey("totalSaved"));
        assertTrue(stats.containsKey("avgIncome"));
        assertTrue(stats.containsKey("avgExpenses"));
        assertTrue(stats.containsKey("savingsRate"));
    }

    @Test
    void getData_withData_incomeVsExpensesChartDataPresent() {

        when(reportsService.buildReport(eq("indiv"), any(), any(), any()))
                .thenReturn(buildFakeReportWithData());


        ResponseEntity<Map<String, Object>> response =
                reportsController.getData(authentication, "6months", null, null);


        @SuppressWarnings("unchecked")
        Map<String, Object> chartData = (Map<String, Object>) response.getBody().get("incomeVsExpenses");
        assertNotNull(chartData);
        assertTrue(chartData.containsKey("income"));
        assertTrue(chartData.containsKey("expenses"));
    }

    // ─────────────────────────────────────────────────────────────
    //  Acceptance Criteria 2 (Success):
    //  Period parameter is passed through correctly to the service
    // ─────────────────────────────────────────────────────────────

    @Test
    void getData_with6MonthsRange_callsServiceWith6months() {

        when(reportsService.buildReport(eq("indiv"), eq("6months"), isNull(), isNull()))
                .thenReturn(buildFakeReportWithData());


        reportsController.getData(authentication, "6months", null, null);

        // verify the service received the exact range value
        verify(reportsService).buildReport("indiv", "6months", null, null);
    }

    @Test
    void getData_with12MonthsRange_callsServiceWith12months() {

        when(reportsService.buildReport(eq("indiv"), eq("12months"), isNull(), isNull()))
                .thenReturn(buildFakeReportWithData());


        reportsController.getData(authentication, "12months", null, null);


        verify(reportsService).buildReport("indiv", "12months", null, null);
    }

    @Test
    void getData_withYtdRange_callsServiceWithYtd() {

        when(reportsService.buildReport(eq("indiv"), eq("ytd"), isNull(), isNull()))
                .thenReturn(buildFakeReportWithData());


        reportsController.getData(authentication, "ytd", null, null);

        verify(reportsService).buildReport("indiv", "ytd", null, null);
    }

    @Test
    void getData_withCustomRange_passesFromAndToToService() {

        when(reportsService.buildReport(eq("indiv"), eq("custom"), eq("2026-01-01"), eq("2026-03-31")))
                .thenReturn(buildFakeReportWithData());


        reportsController.getData(authentication, "custom", "2026-01-01", "2026-03-31");

        //  from and to must be forwarded to the service
        verify(reportsService).buildReport("indiv", "custom", "2026-01-01", "2026-03-31");
    }

    @Test
    void getData_responseContainsRangeValue() {

        when(reportsService.buildReport(eq("indiv"), eq("12months"), isNull(), isNull()))
                .thenReturn(buildFakeReportWithData());


        ResponseEntity<Map<String, Object>> response =
                reportsController.getData(authentication, "12months", null, null);

        // range value echoed back in response
        assertEquals("12months", response.getBody().get("range"));
    }

    // ─────────────────────────────────────────────────────────────
    //  Acceptance Criteria 3 (Failure):
    //  Given no data exists, endpoint returns 200 with hasData false
    // ─────────────────────────────────────────────────────────────

    @Test
    void getData_withNoData_returns200() {

        when(reportsService.buildReport(eq("indiv"), any(), any(), any()))
                .thenReturn(buildFakeEmptyReport());


        ResponseEntity<Map<String, Object>> response =
                reportsController.getData(authentication, "6months", null, null);

        // still 200, not an error
        assertEquals(HttpStatus.OK, response.getStatusCode());
    }

    @Test
    void getData_withNoData_hasDataIsFalse() {

        when(reportsService.buildReport(eq("indiv"), any(), any(), any()))
                .thenReturn(buildFakeEmptyReport());


        ResponseEntity<Map<String, Object>> response =
                reportsController.getData(authentication, "6months", null, null);

        // hasData false tells the UI to show the empty state
        assertEquals(false, response.getBody().get("hasData"));
    }

    @Test
    void getData_withNoData_categoryTrendsIsEmpty() {

        when(reportsService.buildReport(eq("indiv"), any(), any(), any()))
                .thenReturn(buildFakeEmptyReport());


        ResponseEntity<Map<String, Object>> response =
                reportsController.getData(authentication, "6months", null, null);


        List<?> trends = (List<?>) response.getBody().get("categoryTrends");
        assertNotNull(trends);
        assertTrue(trends.isEmpty());
    }

    @Test
    void getData_withNoData_statsAreAllZero() {

        when(reportsService.buildReport(eq("indiv"), any(), any(), any()))
                .thenReturn(buildFakeEmptyReport());

        ResponseEntity<Map<String, Object>> response =
                reportsController.getData(authentication, "6months", null, null);

        // Aall stat boxes should show zero
        @SuppressWarnings("unchecked")
        Map<String, Object> stats = (Map<String, Object>) response.getBody().get("stats");
        assertEquals(BigDecimal.ZERO, stats.get("totalSaved"));
        assertEquals(BigDecimal.ZERO, stats.get("avgIncome"));
        assertEquals(BigDecimal.ZERO, stats.get("avgExpenses"));
        assertEquals(BigDecimal.ZERO, stats.get("savingsRate"));
    }

    // ─────────────────────────────────────────────────────────────
    //  Error handling
    // ─────────────────────────────────────────────────────────────

    @Test
    void handleBadRequest_returnsErrorMessage() {
        // call the exception handler directly
        IllegalArgumentException ex = new IllegalArgumentException("Start date must be before end date.");


        ResponseEntity<Map<String, String>> response = reportsController.handleBadRequest(ex);


        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals("Start date must be before end date.", response.getBody().get("error"));
    }

    @Test
    void handleError_returns500WithMessage() {
        // Arrange
        Exception ex = new RuntimeException("Database unavailable");

        // Act
        ResponseEntity<Map<String, String>> response = reportsController.handleError(ex);

        // Assert
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertNotNull(response.getBody().get("error"));
    }

    // ─────────────────────────────────────────────────────────────
    //  Helpers
    // ─────────────────────────────────────────────────────────────

    private Map<String, Object> buildFakeReportWithData() {
        Map<String, Object> stats = new HashMap<>();
        stats.put("totalSaved",  new BigDecimal("600.00"));
        stats.put("avgIncome",   new BigDecimal("1000.00"));
        stats.put("avgExpenses", new BigDecimal("400.00"));
        stats.put("savingsRate", new BigDecimal("60.0"));

        Map<String, Object> incomeVsExpenses = new HashMap<>();
        incomeVsExpenses.put("income",   List.of(new BigDecimal("1000.00")));
        incomeVsExpenses.put("expenses", List.of(new BigDecimal("400.00")));

        Map<String, Object> report = new HashMap<>();
        report.put("hasData",          true);
        report.put("range",            "12months");
        report.put("labels",           List.of("Jan", "Feb", "Mar", "Apr", "May", "Jun"));
        report.put("incomeVsExpenses", incomeVsExpenses);
        report.put("savingsTrend",     List.of(new BigDecimal("600.00")));
        report.put("categoryTrends",   List.of(Map.of("label", "Food", "color", "#22c55e", "data", List.of())));
        report.put("stats",            stats);
        return report;
    }

    private Map<String, Object> buildFakeEmptyReport() {
        Map<String, Object> stats = new HashMap<>();
        stats.put("totalSaved",  BigDecimal.ZERO);
        stats.put("avgIncome",   BigDecimal.ZERO);
        stats.put("avgExpenses", BigDecimal.ZERO);
        stats.put("savingsRate", BigDecimal.ZERO);

        Map<String, Object> incomeVsExpenses = new HashMap<>();
        incomeVsExpenses.put("income",   Collections.emptyList());
        incomeVsExpenses.put("expenses", Collections.emptyList());

        Map<String, Object> report = new HashMap<>();
        report.put("hasData",          false);
        report.put("range",            "6months");
        report.put("labels",           List.of("Jan", "Feb", "Mar", "Apr", "May", "Jun"));
        report.put("incomeVsExpenses", incomeVsExpenses);
        report.put("savingsTrend",     Collections.emptyList());
        report.put("categoryTrends",   Collections.emptyList());
        report.put("stats",            stats);
        return report;
    }
}
