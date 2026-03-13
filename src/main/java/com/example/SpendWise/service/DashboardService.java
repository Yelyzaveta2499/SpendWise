package com.example.SpendWise.service;

import com.example.SpendWise.model.entity.ExpenseEntity;
import com.example.SpendWise.model.entity.UserEntity;
import com.example.SpendWise.model.repository.ExpenseRepository;
import com.example.SpendWise.model.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.*;

@Service
public class DashboardService {

    private static final String USER_NOT_FOUND_PREFIX = "User not found: ";

    private final ExpenseRepository expenseRepository;
    private final UserRepository userRepository;

    public DashboardService(ExpenseRepository expenseRepository, UserRepository userRepository) {
        this.expenseRepository = expenseRepository;
        this.userRepository = userRepository;
    }

    public Map<String, Object> buildOverview(String username, String period) {
        if (period == null || period.isBlank()) {
            period = "this_month";
        }

        UserEntity user = userRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException(USER_NOT_FOUND_PREFIX + username));

        LocalDate today = LocalDate.now();
        LocalDate start = getStartDate(today, period);
        LocalDate end = getEndDate(today, period);

        List<ExpenseEntity> inRange = expenseRepository.findByUserAndExpenseDateBetween(user, start, end);

        BigDecimal income = BigDecimal.ZERO;
        BigDecimal expenses = BigDecimal.ZERO;

        for (ExpenseEntity e : (inRange == null ? List.<ExpenseEntity>of() : inRange)) {
            if (e == null || e.getAmount() == null) continue;
            BigDecimal amt = e.getAmount().abs();
            if (isIncome(e)) {
                income = income.add(amt);
            } else {
                expenses = expenses.add(amt);
            }
        }

        BigDecimal balance = income.subtract(expenses);
        BigDecimal savingsRate = BigDecimal.ZERO;
        if (income.compareTo(BigDecimal.ZERO) > 0) {
            savingsRate = balance.divide(income, 4, RoundingMode.HALF_UP).multiply(BigDecimal.valueOf(100));
        }

        List<Map<String, Object>> recent = mapRecent(expenseRepository.findTop20ByUserOrderByExpenseDateDesc(user));
        List<Map<String, Object>> chart = buildLast6MonthsChart(user, today);

        Map<String, Object> out = new LinkedHashMap<>();
        out.put("period", period);
        out.put("startDate", start);
        out.put("endDate", end);
        out.put("income", income);
        out.put("expenses", expenses);
        out.put("balance", balance);
        out.put("savingsRate", savingsRate);
        out.put("recentTransactions", recent);
        out.put("chart", chart);
        out.put("hasData", inRange != null && !inRange.isEmpty());
        return out;
    }

    private LocalDate getStartDate(LocalDate today, String period) {
        switch (period) {
            case "this_month": {
                YearMonth ym = YearMonth.from(today);
                return ym.atDay(1);
            }
            case "last_month": {
                YearMonth ym = YearMonth.from(today).minusMonths(1);
                return ym.atDay(1);
            }
            case "last_30": {
                return today.minusDays(30);
            }
            case "this_year": {
                return LocalDate.of(today.getYear(), 1, 1);
            }
            default:
                throw new IllegalArgumentException("Unsupported period: " + period);
        }
    }

    private LocalDate getEndDate(LocalDate today, String period) {
        switch (period) {
            case "this_month":
            case "last_30":
            case "this_year":
                return today;
            case "last_month": {
                YearMonth ym = YearMonth.from(today).minusMonths(1);
                return ym.atEndOfMonth();
            }
            default:
                throw new IllegalArgumentException("Unsupported period: " + period);
        }
    }

    private boolean isIncome(ExpenseEntity e) {
        String cat = e.getCategory() == null ? "" : e.getCategory().trim();
        return "Income".equalsIgnoreCase(cat);
    }

    private List<Map<String, Object>> mapRecent(List<ExpenseEntity> top) {
        List<Map<String, Object>> out = new ArrayList<>();
        if (top == null) return out;

        for (int i = 0; i < top.size() && i < 10; i++) {
            ExpenseEntity e = top.get(i);
            if (e == null) continue;
            Map<String, Object> dto = new LinkedHashMap<>();
            dto.put("id", e.getId());
            dto.put("name", e.getName());
            dto.put("category", e.getCategory());
            dto.put("amount", e.getAmount());
            dto.put("expenseDate", e.getExpenseDate());
            out.add(dto);
        }
        return out;
    }

    private List<Map<String, Object>> buildLast6MonthsChart(UserEntity user, LocalDate today) {
        YearMonth thisMonth = YearMonth.from(today);

        List<YearMonth> months = new ArrayList<>();
        for (int i = 5; i >= 0; i--) {
            months.add(thisMonth.minusMonths(i));
        }

        LocalDate chartStart = months.get(0).atDay(1);
        LocalDate chartEnd = months.get(months.size() - 1).atEndOfMonth();
        List<ExpenseEntity> all = expenseRepository.findByUserAndExpenseDateBetween(user, chartStart, chartEnd);

        Map<String, BigDecimal> incByMonth = new HashMap<>();
        Map<String, BigDecimal> expByMonth = new HashMap<>();

        for (ExpenseEntity e : (all == null ? List.<ExpenseEntity>of() : all)) {
            if (e == null || e.getExpenseDate() == null || e.getAmount() == null) continue;
            YearMonth ym = YearMonth.from(e.getExpenseDate());
            String key = ym.toString(); // yyyy-MM

            BigDecimal amt = e.getAmount().abs();
            if (isIncome(e)) {
                incByMonth.put(key, incByMonth.getOrDefault(key, BigDecimal.ZERO).add(amt));
            } else {
                expByMonth.put(key, expByMonth.getOrDefault(key, BigDecimal.ZERO).add(amt));
            }
        }

        List<Map<String, Object>> points = new ArrayList<>();
        for (YearMonth ym : months) {
            String key = ym.toString();
            Map<String, Object> p = new LinkedHashMap<>();
            p.put("month", key);
            p.put("label", ym.getMonth().toString().substring(0, 1) + ym.getMonth().toString().substring(1, 3).toLowerCase());
            p.put("income", incByMonth.getOrDefault(key, BigDecimal.ZERO));
            p.put("expenses", expByMonth.getOrDefault(key, BigDecimal.ZERO));
            points.add(p);
        }

        return points;
    }
}

