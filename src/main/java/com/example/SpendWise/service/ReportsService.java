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
import java.time.format.TextStyle;
import java.util.*;

@Service
public class ReportsService {

    private final ExpenseRepository expenseRepository;
    private final UserRepository    userRepository;

    public ReportsService(ExpenseRepository expenseRepository,
                          UserRepository userRepository) {
        this.expenseRepository = expenseRepository;
        this.userRepository    = userRepository;
    }

    // ─────────────────────────────────────────────
    //  Main entry point
    // ─────────────────────────────────────────────

    public Map<String, Object> buildReport(String username, String range) {
        UserEntity user = userRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + username));

        int months = parseMonths(range);
        LocalDate today    = LocalDate.now();
        YearMonth thisMonth = YearMonth.from(today);

        // Build ordered list of months to report on
        List<YearMonth> period = new ArrayList<>();
        for (int i = months - 1; i >= 0; i--) {
            period.add(thisMonth.minusMonths(i));
        }

        LocalDate start = period.get(0).atDay(1);
        LocalDate end   = period.get(period.size() - 1).atEndOfMonth();

        List<ExpenseEntity> all = expenseRepository.findByUserAndExpenseDateBetween(user, start, end);

        boolean hasData = all != null && !all.isEmpty();

        if (!hasData) {
            return emptyResponse(period);
        }

        // ── per-month buckets ──────────────────────────────────────────────
        Map<String, BigDecimal> incomeByMonth   = new LinkedHashMap<>();
        Map<String, BigDecimal> expensesByMonth = new LinkedHashMap<>();
        Map<String, BigDecimal> savingsByMonth  = new LinkedHashMap<>();

        // category trends: category → (month-key → total)
        Map<String, Map<String, BigDecimal>> categoryByMonth = new LinkedHashMap<>();

        // pre-populate every month key so gaps show as zero
        for (YearMonth ym : period) {
            String key = ym.toString();
            incomeByMonth.put(key,   BigDecimal.ZERO);
            expensesByMonth.put(key, BigDecimal.ZERO);
        }

        for (ExpenseEntity e : all) {
            if (e == null || e.getAmount() == null || e.getExpenseDate() == null) continue;
            String key = YearMonth.from(e.getExpenseDate()).toString();
            if (!incomeByMonth.containsKey(key)) continue; // outside window

            BigDecimal amt = e.getAmount().abs();

            if (isIncome(e)) {
                incomeByMonth.put(key, incomeByMonth.get(key).add(amt));
            } else {
                expensesByMonth.put(key, expensesByMonth.get(key).add(amt));

                // category trends
                String cat = normaliseCategory(e.getCategory());
                categoryByMonth.computeIfAbsent(cat, k -> {
                    Map<String, BigDecimal> m = new LinkedHashMap<>();
                    for (YearMonth ym : period) m.put(ym.toString(), BigDecimal.ZERO);
                    return m;
                });
                categoryByMonth.get(cat).put(key,
                        categoryByMonth.get(cat).get(key).add(amt));
            }
        }

        // savings per month = income – expenses
        for (YearMonth ym : period) {
            String key = ym.toString();
            savingsByMonth.put(key,
                    incomeByMonth.get(key).subtract(expensesByMonth.get(key)));
        }

        // ── aggregate totals ───────────────────────────────────────────────
        BigDecimal totalIncome   = sum(incomeByMonth.values());
        BigDecimal totalExpenses = sum(expensesByMonth.values());
        BigDecimal totalSaved    = totalIncome.subtract(totalExpenses);
        BigDecimal avgIncome     = avg(incomeByMonth.values(), months);
        BigDecimal avgExpenses   = avg(expensesByMonth.values(), months);
        BigDecimal savingsRate   = BigDecimal.ZERO;
        if (totalIncome.compareTo(BigDecimal.ZERO) > 0) {
            savingsRate = totalSaved
                    .divide(totalIncome, 4, RoundingMode.HALF_UP)
                    .multiply(BigDecimal.valueOf(100))
                    .setScale(1, RoundingMode.HALF_UP);
        }

        // ── chart series ───────────────────────────────────────────────────
        List<String>     labels       = new ArrayList<>();
        List<BigDecimal> incomeData   = new ArrayList<>();
        List<BigDecimal> expensesData = new ArrayList<>();
        List<BigDecimal> savingsData  = new ArrayList<>();

        for (YearMonth ym : period) {
            String key = ym.toString();
            labels.add(ym.getMonth().getDisplayName(TextStyle.SHORT, Locale.ENGLISH));
            incomeData.add(incomeByMonth.get(key));
            expensesData.add(expensesByMonth.get(key));
            savingsData.add(savingsByMonth.get(key));
        }

        // ── category trends series ─────────────────────────────────────────
        List<Map<String, Object>> categoryTrends = buildCategoryTrends(categoryByMonth, period);

        // ── assemble response ──────────────────────────────────────────────
        Map<String, Object> response = new LinkedHashMap<>();
        response.put("hasData", true);
        response.put("range", range);
        response.put("labels", labels);

        // Income vs Expenses chart data
        Map<String, Object> incomeVsExpenses = new LinkedHashMap<>();
        incomeVsExpenses.put("income",   incomeData);
        incomeVsExpenses.put("expenses", expensesData);
        response.put("incomeVsExpenses", incomeVsExpenses);

        // Savings trend chart data
        response.put("savingsTrend", savingsData);

        // Category trends chart data
        response.put("categoryTrends", categoryTrends);

        // Summary stats
        Map<String, Object> stats = new LinkedHashMap<>();
        stats.put("totalSaved",   totalSaved.setScale(2, RoundingMode.HALF_UP));
        stats.put("avgIncome",    avgIncome.setScale(2, RoundingMode.HALF_UP));
        stats.put("avgExpenses",  avgExpenses.setScale(2, RoundingMode.HALF_UP));
        stats.put("savingsRate",  savingsRate);
        response.put("stats", stats);

        return response;
    }

    // ─────────────────────────────────────────────
    //  Helpers
    // ─────────────────────────────────────────────

    private int parseMonths(String range) {
        if (range == null) return 6;
        switch (range) {
            case "12months": return 12;
            case "ytd":      return LocalDate.now().getMonthValue(); // Jan=1 … Dec=12
            default:         return 6; // "6months" or unknown
        }
    }

    private boolean isIncome(ExpenseEntity e) {
        String cat = e.getCategory() == null ? "" : e.getCategory().trim();
        return "Income".equalsIgnoreCase(cat);
    }

    private String normaliseCategory(String raw) {
        if (raw == null || raw.isBlank()) return "Other";
        String trimmed = raw.trim();
        return Character.toUpperCase(trimmed.charAt(0)) + trimmed.substring(1).toLowerCase();
    }

    private BigDecimal sum(Collection<BigDecimal> values) {
        return values.stream()
                .filter(Objects::nonNull)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private BigDecimal avg(Collection<BigDecimal> values, int divisor) {
        if (divisor == 0) return BigDecimal.ZERO;
        return sum(values).divide(BigDecimal.valueOf(divisor), 2, RoundingMode.HALF_UP);
    }

    /**
     * Build per-category line series sorted by total spend descending.
     * Returns up to the top 6 categories
     */
    private List<Map<String, Object>> buildCategoryTrends(
            Map<String, Map<String, BigDecimal>> categoryByMonth,
            List<YearMonth> period) {

        // Sort by total descending and keep top 6
        List<Map.Entry<String, Map<String, BigDecimal>>> sorted =
                new ArrayList<>(categoryByMonth.entrySet());
        sorted.sort((a, b) -> sum(b.getValue().values())
                .compareTo(sum(a.getValue().values())));

        int limit = Math.min(6, sorted.size());
        List<Map<String, Object>> result = new ArrayList<>();

        String[] palette = {"#06b6d4", "#22c55e", "#f59e0b", "#a855f7", "#ef4444", "#f97316"};

        for (int i = 0; i < limit; i++) {
            Map.Entry<String, Map<String, BigDecimal>> entry = sorted.get(i);
            String cat = entry.getKey();
            Map<String, BigDecimal> monthlyTotals = entry.getValue();

            List<BigDecimal> data = new ArrayList<>();
            for (YearMonth ym : period) {
                data.add(monthlyTotals.getOrDefault(ym.toString(), BigDecimal.ZERO));
            }

            Map<String, Object> series = new LinkedHashMap<>();
            series.put("label", cat);
            series.put("color", palette[i % palette.length]);
            series.put("data",  data);
            result.add(series);
        }

        return result;
    }

    /** Empty response when the user has no data in the selected period */
    private Map<String, Object> emptyResponse(List<YearMonth> period) {
        List<String> labels = new ArrayList<>();
        for (YearMonth ym : period) {
            labels.add(ym.getMonth().getDisplayName(TextStyle.SHORT, Locale.ENGLISH));
        }

        Map<String, Object> incomeVsExpenses = new LinkedHashMap<>();
        incomeVsExpenses.put("income",   Collections.emptyList());
        incomeVsExpenses.put("expenses", Collections.emptyList());

        Map<String, Object> stats = new LinkedHashMap<>();
        stats.put("totalSaved",  BigDecimal.ZERO);
        stats.put("avgIncome",   BigDecimal.ZERO);
        stats.put("avgExpenses", BigDecimal.ZERO);
        stats.put("savingsRate", BigDecimal.ZERO);

        Map<String, Object> response = new LinkedHashMap<>();
        response.put("hasData",         false);
        response.put("labels",          labels);
        response.put("incomeVsExpenses", incomeVsExpenses);
        response.put("savingsTrend",    Collections.emptyList());
        response.put("categoryTrends",  Collections.emptyList());
        response.put("stats",           stats);
        return response;
    }
}


