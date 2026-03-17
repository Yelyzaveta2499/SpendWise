package com.example.SpendWise.unit.service;

import com.example.SpendWise.model.entity.ExpenseEntity;
import com.example.SpendWise.model.entity.UserEntity;
import com.example.SpendWise.model.repository.ExpenseRepository;
import com.example.SpendWise.model.repository.UserRepository;
import com.example.SpendWise.service.ReportsService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

/**
 * Unit tests for ReportsService.

 */
class ReportsServiceTest {


    private ReportsService reportsService;


    private ExpenseRepository expenseRepository;
    private UserRepository userRepository;


    private UserEntity testUser;

    @BeforeEach
    void setUp() {
        // mock repositories
        expenseRepository = Mockito.mock(ExpenseRepository.class);
        userRepository    = Mockito.mock(UserRepository.class);

        // service with the mocks injected
        reportsService = new ReportsService(expenseRepository, userRepository);

        // test user that the mocked userRepository will return
        testUser = new UserEntity();
        testUser.setId(1);
        testUser.setUsername("indiv");

        when(userRepository.findByUsername("indiv")).thenReturn(Optional.of(testUser));
    }

    // ─────────────────────────────────────────────────────────────
    //  Acceptance Criteria 1 (Success):
    //  Given financial data exists, the report contains chart data
    // ─────────────────────────────────────────────────────────────

    @Test
    void buildReport_whenExpensesExist_hasDataIsTrue() {
        // fake the DB returning one expense
        ExpenseEntity expense = makeExpense("Groceries", "Food", new BigDecimal("50.00"), LocalDate.now());
        when(expenseRepository.findByUserAndExpenseDateBetween(eq(testUser), any(), any()))
                .thenReturn(List.of(expense));


        Map<String, Object> result = reportsService.buildReport("indiv", "6months", null, null);

        //  hasData must be true so the UI shows charts
        assertTrue((Boolean) result.get("hasData"), "hasData should be true when expenses exist");
    }

    @Test
    void buildReport_whenExpensesExist_labelsAreReturned() {

        ExpenseEntity expense = makeExpense("Rent", "Housing", new BigDecimal("800.00"), LocalDate.now());
        when(expenseRepository.findByUserAndExpenseDateBetween(eq(testUser), any(), any()))
                .thenReturn(List.of(expense));


        Map<String, Object> result = reportsService.buildReport("indiv", "6months", null, null);

        // labels list must not be empty (these drive the chart x-axis)
        @SuppressWarnings("unchecked")
        List<String> labels = (List<String>) result.get("labels");
        assertNotNull(labels, "labels should not be null");
        assertFalse(labels.isEmpty(), "labels should contain month names");
    }

    @Test
    void buildReport_whenExpensesExist_incomeVsExpensesChartDataIsReturned() {
        // add an income and an expense entry
        ExpenseEntity income  = makeExpense("Salary",    "Income", new BigDecimal("3000.00"), LocalDate.now());
        ExpenseEntity expense = makeExpense("Groceries", "Food",   new BigDecimal("200.00"),  LocalDate.now());

        when(expenseRepository.findByUserAndExpenseDateBetween(eq(testUser), any(), any()))
                .thenReturn(List.of(income, expense));


        Map<String, Object> result = reportsService.buildReport("indiv", "6months", null, null);

        // incomeVsExpenses map must contain income and expenses lists
        @SuppressWarnings("unchecked")
        Map<String, Object> chartData = (Map<String, Object>) result.get("incomeVsExpenses");
        assertNotNull(chartData, "incomeVsExpenses chart data should be present");
        assertTrue(chartData.containsKey("income"),   "should have income data");
        assertTrue(chartData.containsKey("expenses"), "should have expenses data");
    }

    @Test
    void buildReport_whenExpensesExist_savingsTrendIsReturned() {

        ExpenseEntity income = makeExpense("Salary", "Income", new BigDecimal("2000.00"), LocalDate.now());
        when(expenseRepository.findByUserAndExpenseDateBetween(eq(testUser), any(), any()))
                .thenReturn(List.of(income));


        Map<String, Object> result = reportsService.buildReport("indiv", "6months", null, null);

        // savings trend data must be present for the savings chart
        assertNotNull(result.get("savingsTrend"), "savingsTrend should be present");
    }

    @Test
    void buildReport_whenExpensesExist_categoryTrendsAreReturned() {

        ExpenseEntity e1 = makeExpense("Rent",       "Housing", new BigDecimal("800.00"), LocalDate.now());
        ExpenseEntity e2 = makeExpense("Supermarket", "Food",   new BigDecimal("150.00"), LocalDate.now());

        when(expenseRepository.findByUserAndExpenseDateBetween(eq(testUser), any(), any()))
                .thenReturn(List.of(e1, e2));


        Map<String, Object> result = reportsService.buildReport("indiv", "6months", null, null);

        // category trends list must be present for the category chart
        @SuppressWarnings("unchecked")
        List<Map<String, Object>> trends = (List<Map<String, Object>>) result.get("categoryTrends");
        assertNotNull(trends, "categoryTrends should not be null");
        assertFalse(trends.isEmpty(), "categoryTrends should contain at least one category");
    }

    // ─────────────────────────────────────────────────────────────
    //  Acceptance Criteria 2 (Success):
    //  Given a period is selected, data matches that period
    // ─────────────────────────────────────────────────────────────

    @Test
    void buildReport_with6MonthsRange_returns6Labels() {
        //return some data so it goes through the full path
        ExpenseEntity expense = makeExpense("Bus", "Transport", new BigDecimal("30.00"), LocalDate.now());
        when(expenseRepository.findByUserAndExpenseDateBetween(eq(testUser), any(), any()))
                .thenReturn(List.of(expense));


        Map<String, Object> result = reportsService.buildReport("indiv", "6months", null, null);

        // must have exactly 6 month labels
        @SuppressWarnings("unchecked")
        List<String> labels = (List<String>) result.get("labels");
        assertEquals(6, labels.size(), "6months range should produce 6 labels");
    }

    @Test
    void buildReport_with12MonthsRange_returns12Labels() {

        ExpenseEntity expense = makeExpense("Bus", "Transport", new BigDecimal("30.00"), LocalDate.now());
        when(expenseRepository.findByUserAndExpenseDateBetween(eq(testUser), any(), any()))
                .thenReturn(List.of(expense));


        Map<String, Object> result = reportsService.buildReport("indiv", "12months", null, null);


        @SuppressWarnings("unchecked")
        List<String> labels = (List<String>) result.get("labels");
        assertEquals(12, labels.size(), "12months range should produce 12 labels");
    }

    @Test
    void buildReport_withCustomRange_returnsCorrectNumberOfMonths() {
        // custom range of exactly 3 months
        ExpenseEntity expense = makeExpense("Gym", "Health", new BigDecimal("40.00"),
                LocalDate.of(2026, 1, 15));
        when(expenseRepository.findByUserAndExpenseDateBetween(eq(testUser), any(), any()))
                .thenReturn(List.of(expense));

        //  from Jan 2026 to Mar 2026 = 3 months
        Map<String, Object> result = reportsService.buildReport(
                "indiv", "custom", "2026-01-01", "2026-03-31");


        @SuppressWarnings("unchecked")
        List<String> labels = (List<String>) result.get("labels");
        assertEquals(3, labels.size(), "Custom Jan–Mar range should produce 3 labels");
    }

    @Test
    void buildReport_statsContainCorrectTotalSaved() {
        // income 1000, expense 400, so total saved = 600
        ExpenseEntity income  = makeExpense("Salary",  "Income", new BigDecimal("1000.00"), LocalDate.now());
        ExpenseEntity expense = makeExpense("Clothes", "Shopping", new BigDecimal("400.00"), LocalDate.now());

        when(expenseRepository.findByUserAndExpenseDateBetween(eq(testUser), any(), any()))
                .thenReturn(List.of(income, expense));


        Map<String, Object> result = reportsService.buildReport("indiv", "6months", null, null);


        @SuppressWarnings("unchecked")
        Map<String, Object> stats = (Map<String, Object>) result.get("stats");
        BigDecimal totalSaved = (BigDecimal) stats.get("totalSaved");
        assertEquals(new BigDecimal("600.00"), totalSaved, "Total saved should be income minus expenses");
    }

    @Test
    void buildReport_statsContainSavingsRate() {
        //income 1000, expense 200 → savings rate = 80%
        ExpenseEntity income  = makeExpense("Salary", "Income", new BigDecimal("1000.00"), LocalDate.now());
        ExpenseEntity expense = makeExpense("Food",   "Food",   new BigDecimal("200.00"),  LocalDate.now());

        when(expenseRepository.findByUserAndExpenseDateBetween(eq(testUser), any(), any()))
                .thenReturn(List.of(income, expense));


        Map<String, Object> result = reportsService.buildReport("indiv", "6months", null, null);


        @SuppressWarnings("unchecked")
        Map<String, Object> stats = (Map<String, Object>) result.get("stats");
        BigDecimal savingsRate = (BigDecimal) stats.get("savingsRate");
        assertTrue(savingsRate.compareTo(BigDecimal.ZERO) > 0, "Savings rate should be positive when income > expenses");
    }

    // ─────────────────────────────────────────────────────────────
    //  Acceptance Criteria 3 (Failure):
    //  Given no financial data exists, an empty state is returned
    // ─────────────────────────────────────────────────────────────

    @Test
    void buildReport_whenNoExpenses_hasDataIsFalse() {
        // the DB returns an empty list
        when(expenseRepository.findByUserAndExpenseDateBetween(eq(testUser), any(), any()))
                .thenReturn(Collections.emptyList());


        Map<String, Object> result = reportsService.buildReport("indiv", "6months", null, null);

        //  hasData must be false so the UI shows the empty state
        assertFalse((Boolean) result.get("hasData"), "hasData should be false when no expenses exist");
    }

    @Test
    void buildReport_whenNoExpenses_categoryTrendsIsEmpty() {

        when(expenseRepository.findByUserAndExpenseDateBetween(eq(testUser), any(), any()))
                .thenReturn(Collections.emptyList());


        Map<String, Object> result = reportsService.buildReport("indiv", "6months", null, null);

        //  no category data to show
        @SuppressWarnings("unchecked")
        List<?> trends = (List<?>) result.get("categoryTrends");
        assertTrue(trends.isEmpty(), "categoryTrends should be empty when no data exists");
    }

    @Test
    void buildReport_whenNoExpenses_savingsTrendIsEmpty() {

        when(expenseRepository.findByUserAndExpenseDateBetween(eq(testUser), any(), any()))
                .thenReturn(Collections.emptyList());


        Map<String, Object> result = reportsService.buildReport("indiv", "6months", null, null);

        // no savings trend data
        @SuppressWarnings("unchecked")
        List<?> savingsTrend = (List<?>) result.get("savingsTrend");
        assertTrue(savingsTrend.isEmpty(), "savingsTrend should be empty when no data exists");
    }

    @Test
    void buildReport_whenNoExpenses_statsAreAllZero() {

        when(expenseRepository.findByUserAndExpenseDateBetween(eq(testUser), any(), any()))
                .thenReturn(Collections.emptyList());


        Map<String, Object> result = reportsService.buildReport("indiv", "6months", null, null);

        //  all stat box values should be zero
        @SuppressWarnings("unchecked")
        Map<String, Object> stats = (Map<String, Object>) result.get("stats");
        assertEquals(BigDecimal.ZERO, stats.get("totalSaved"),  "totalSaved should be 0 with no data");
        assertEquals(BigDecimal.ZERO, stats.get("avgIncome"),   "avgIncome should be 0 with no data");
        assertEquals(BigDecimal.ZERO, stats.get("avgExpenses"), "avgExpenses should be 0 with no data");
        assertEquals(BigDecimal.ZERO, stats.get("savingsRate"), "savingsRate should be 0 with no data");
    }

    // ─────────────────────────────────────────────────────────────
    //  Edge cases
    // ─────────────────────────────────────────────────────────────

    @Test
    void buildReport_withUnknownUser_throwsException() {
        // this username does not exist in the DB
        when(userRepository.findByUsername("ghost")).thenReturn(Optional.empty());

        // service should throw an exception
        assertThrows(IllegalArgumentException.class,
                () -> reportsService.buildReport("ghost", "6months", null, null),
                "Should throw when user is not found");
    }

    @Test
    void buildReport_withCustomRange_invalidDateOrder_throwsException() {
        // from is AFTER to, which is invalid
        assertThrows(IllegalArgumentException.class,
                () -> reportsService.buildReport("indiv", "custom", "2026-06-01", "2026-01-01"),
                "Should throw when start date is after end date");
    }

    @Test
    void buildReport_incomeOnlyNoExpenses_totalSavedEqualsIncome() {
        // only income, no spending
        ExpenseEntity income = makeExpense("Salary", "Income", new BigDecimal("2000.00"), LocalDate.now());
        when(expenseRepository.findByUserAndExpenseDateBetween(eq(testUser), any(), any()))
                .thenReturn(List.of(income));


        Map<String, Object> result = reportsService.buildReport("indiv", "6months", null, null);

        //everything saved = full income amount
        @SuppressWarnings("unchecked")
        Map<String, Object> stats = (Map<String, Object>) result.get("stats");
        BigDecimal totalSaved = (BigDecimal) stats.get("totalSaved");
        assertEquals(new BigDecimal("2000.00"), totalSaved,
                "When there are no expenses, all income should be saved");
    }

    // ─────────────────────────────────────────────────────────────
    //  Helper — builds a simple ExpenseEntity for testing
    // ─────────────────────────────────────────────────────────────

    private ExpenseEntity makeExpense(String name, String category,
                                       BigDecimal amount, LocalDate date) {
        ExpenseEntity e = new ExpenseEntity();
        e.setUser(testUser);
        e.setName(name);
        e.setCategory(category);
        e.setAmount(amount);
        e.setExpenseDate(date);
        return e;
    }
}

