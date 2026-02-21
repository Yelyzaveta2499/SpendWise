package com.example.SpendWise.service;

import com.example.SpendWise.model.entity.ExpenseEntity;
import com.example.SpendWise.model.entity.UserEntity;
import com.example.SpendWise.model.repository.ExpenseRepository;
import com.example.SpendWise.model.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

class DashboardServiceTest {

    private ExpenseRepository expenseRepository;
    private UserRepository userRepository;
    private DashboardService dashboardService;

    @BeforeEach
    void setUp() {
        expenseRepository = Mockito.mock(ExpenseRepository.class);
        userRepository = Mockito.mock(UserRepository.class);
        dashboardService = new DashboardService(expenseRepository, userRepository);
    }

    @Test
    void buildOverview_whenUserNotFound_throwsIllegalArgumentException() {
        when(userRepository.findByUsername("missing"))
                .thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class, () ->
                dashboardService.buildOverview("missing", "this_month"));
    }

    @Test
    void buildOverview_whenNoFinancialData_returnsZerosAndHasDataFalse() {
        UserEntity user = new UserEntity();
        user.setId(1);
        user.setUsername("u1");

        when(userRepository.findByUsername("u1")).thenReturn(Optional.of(user));
        when(expenseRepository.findByUserAndExpenseDateBetween(eq(user), any(LocalDate.class), any(LocalDate.class)))
                .thenReturn(List.of());
        when(expenseRepository.findTop20ByUserOrderByExpenseDateDesc(user)).thenReturn(List.of());

        Map<String, Object> out = dashboardService.buildOverview("u1", "this_month");

        assertEquals(BigDecimal.ZERO, out.get("income"));
        assertEquals(BigDecimal.ZERO, out.get("expenses"));
        assertEquals(BigDecimal.ZERO, out.get("balance"));
        assertEquals(BigDecimal.ZERO, out.get("savingsRate"));
        assertEquals(false, out.get("hasData"));

        assertNotNull(out.get("chart"));
        assertNotNull(out.get("recentTransactions"));
    }

    @Test
    void buildOverview_calculatesIncomeExpensesBalanceAndSavingsRate() {
        UserEntity user = new UserEntity();
        user.setId(1);
        user.setUsername("u1");

        LocalDate today = LocalDate.now();
        List<ExpenseEntity> inPeriod = List.of(
                exp("Salary", "Income", "5200.00", today),
                exp("Coffee", "Food & Dining", "6.75", today),
                exp("Rent", "Housing", "1200.00", today)
        );

        when(userRepository.findByUsername("u1")).thenReturn(Optional.of(user));

        // Used twice: first for totals (period range), second for chart range.
        // For this test, return same list for both calls.
        when(expenseRepository.findByUserAndExpenseDateBetween(eq(user), any(LocalDate.class), any(LocalDate.class)))
                .thenReturn(inPeriod);

        when(expenseRepository.findTop20ByUserOrderByExpenseDateDesc(user)).thenReturn(inPeriod);

        Map<String, Object> out = dashboardService.buildOverview("u1", "this_month");

        BigDecimal income = (BigDecimal) out.get("income");
        BigDecimal expenses = (BigDecimal) out.get("expenses");
        BigDecimal balance = (BigDecimal) out.get("balance");
        BigDecimal savingsRate = (BigDecimal) out.get("savingsRate");

        assertEquals(new BigDecimal("5200.00"), income);
        assertEquals(new BigDecimal("1206.75"), expenses);
        assertEquals(new BigDecimal("3993.25"), balance);

        // savingsRate = (balance / income) * 100 ≈ 76.79
        assertTrue(savingsRate.doubleValue() > 76.0);
        assertTrue(savingsRate.doubleValue() < 77.0);

        assertEquals(true, out.get("hasData"));
    }

    @Test
    void buildOverview_chartAlwaysReturnsSixPoints() {
        UserEntity user = new UserEntity();
        user.setId(1);
        user.setUsername("u1");

        when(userRepository.findByUsername("u1")).thenReturn(Optional.of(user));

        // return no in-period data (empty state)
        when(expenseRepository.findByUserAndExpenseDateBetween(eq(user), any(LocalDate.class), any(LocalDate.class)))
                .thenReturn(List.of());
        when(expenseRepository.findTop20ByUserOrderByExpenseDateDesc(user)).thenReturn(List.of());

        Map<String, Object> out = dashboardService.buildOverview("u1", "this_month");

        @SuppressWarnings("unchecked")
        List<Map<String, Object>> chart = (List<Map<String, Object>>) out.get("chart");
        assertNotNull(chart);
        assertEquals(6, chart.size());

        for (Map<String, Object> p : chart) {
            assertNotNull(p.get("month"));
            assertNotNull(p.get("label"));
            assertNotNull(p.get("income"));
            assertNotNull(p.get("expenses"));
        }
    }

    @Test
    void buildOverview_invalidPeriod_throwsIllegalArgumentException() {
        UserEntity user = new UserEntity();
        user.setId(1);
        user.setUsername("u1");

        when(userRepository.findByUsername("u1")).thenReturn(Optional.of(user));

        assertThrows(IllegalArgumentException.class, () ->
                dashboardService.buildOverview("u1", "not_supported"));
    }

    private static ExpenseEntity exp(String name, String category, String amount, LocalDate date) {
        ExpenseEntity e = new ExpenseEntity();
        e.setName(name);
        e.setCategory(category);
        e.setAmount(new BigDecimal(amount));
        e.setExpenseDate(date);
        return e;
    }
}

