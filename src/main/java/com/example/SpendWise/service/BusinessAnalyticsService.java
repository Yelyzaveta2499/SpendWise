package com.example.SpendWise.service;

import com.example.SpendWise.model.entity.ExpenseEntity;
import com.example.SpendWise.model.entity.TagEntity;
import com.example.SpendWise.model.entity.UserEntity;
import com.example.SpendWise.model.repository.ExpenseRepository;
import com.example.SpendWise.model.repository.ExpenseTagRepository;
import com.example.SpendWise.model.repository.TagRepository;
import com.example.SpendWise.model.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.security.SecureRandom;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class BusinessAnalyticsService {

    private static final String COLOR_KEY = "color";
    private static final String AMOUNT_KEY = "amount";
    private static final String TAG_COLORS_KEY = "tagColors";
    private static final String MONTH_KEY = "month";

    private final UserRepository userRepository;
    private final TagRepository tagRepository;
    private final ExpenseRepository expenseRepository;
    private final ExpenseTagRepository expenseTagRepository;

    private static final SecureRandom SECURE_RANDOM = new SecureRandom();

    public BusinessAnalyticsService(UserRepository userRepository,
                                   TagRepository tagRepository,
                                   ExpenseRepository expenseRepository,
                                   ExpenseTagRepository expenseTagRepository) {
        this.userRepository = userRepository;
        this.tagRepository = tagRepository;
        this.expenseRepository = expenseRepository;
        this.expenseTagRepository = expenseTagRepository;
    }

    public Map<String, Object> getBusinessAnalytics(String username) {
        UserEntity user = userRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + username));

        Map<String, Object> analytics = new HashMap<>();

        // Get all user's tags
        List<TagEntity> tags = tagRepository.findByUserOrderByNameAsc(user);

        // Get all user's expenses
        List<ExpenseEntity> expenses = expenseRepository.findByUser(user);

        // Calculate stats
        analytics.put("stats", calculateStats(expenses, tags));

        // Get expense tags with counts
        analytics.put("expenseTags", getExpenseTagsWithCounts(tags));

        // Get spending by tag
        analytics.put("spendingByTag", getSpendingByTag(tags));

        // Get category data
        analytics.put("categoryData", getCategoryData(expenses));

        // Get recent tagged expenses
        analytics.put("recentExpenses", getRecentTaggedExpenses(expenses));

        // Get chart data for Monthly Tag Report
        analytics.put("monthlyTagData", getMonthlyTagData(tags, expenses));

        // Get chart data for Income & Expenses (using expenses as data)
        analytics.put("incomeExpensesData", getIncomeExpensesData(expenses));

        return analytics;
    }

    private Map<String, Object> calculateStats(List<ExpenseEntity> expenses, List<TagEntity> tags) {
        Map<String, Object> stats = new HashMap<>();

        BigDecimal totalExpenses = expenses.stream()
                .map(ExpenseEntity::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        stats.put("totalExpenses", formatMoney(totalExpenses));
        stats.put("activeTags", tags.size());
        stats.put("totalTransactions", expenses.size());

        return stats;
    }

    private List<Map<String, Object>> getExpenseTagsWithCounts(List<TagEntity> tags) {
        List<Map<String, Object>> result = new ArrayList<>();

        for (TagEntity tag : tags) {
            Map<String, Object> tagData = new HashMap<>();
            tagData.put("id", tag.getId());
            tagData.put("name", tag.getName());
            tagData.put(COLOR_KEY, tag.getColor());
            tagData.put("count", expenseTagRepository.countByTag(tag));
            result.add(tagData);
        }

        return result;
    }

    private List<Map<String, Object>> getSpendingByTag(List<TagEntity> tags) {
        List<Map<String, Object>> result = new ArrayList<>();

        for (TagEntity tag : tags) {
            List<ExpenseEntity> taggedExpenses = expenseTagRepository.findExpensesByTag(tag);

            BigDecimal totalAmount = taggedExpenses.stream()
                    .map(ExpenseEntity::getAmount)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            if (totalAmount.compareTo(BigDecimal.ZERO) > 0) {
                Map<String, Object> tagSpending = new HashMap<>();
                tagSpending.put("name", tag.getName());
                tagSpending.put(AMOUNT_KEY, totalAmount.doubleValue());
                tagSpending.put(COLOR_KEY, tag.getColor());
                result.add(tagSpending);
            }
        }

        // Sort by amount descending and take top 6
        result.sort((a, b) -> Double.compare((Double) b.get(AMOUNT_KEY), (Double) a.get(AMOUNT_KEY)));
        return result.stream().limit(6).collect(Collectors.toList());
    }

    private List<Map<String, Object>> getCategoryData(List<ExpenseEntity> expenses) {
        Map<String, BigDecimal> categoryTotals = new HashMap<>();
        Map<String, List<TagEntity>> categoryTags = new HashMap<>();

        for (ExpenseEntity expense : expenses) {
            String category = expense.getCategory();
            categoryTotals.merge(category, expense.getAmount(), BigDecimal::add);

            List<TagEntity> tags = expenseTagRepository.findTagsByExpense(expense);
            categoryTags.putIfAbsent(category, tags);
        }

        List<Map<String, Object>> result = new ArrayList<>();
        for (Map.Entry<String, BigDecimal> entry : categoryTotals.entrySet()) {
            Map<String, Object> categoryData = new HashMap<>();
            categoryData.put("name", entry.getKey());
            categoryData.put(AMOUNT_KEY, formatMoney(entry.getValue()));

            List<TagEntity> tags = categoryTags.get(entry.getKey());
            if (tags != null && !tags.isEmpty()) {
                categoryData.put("tags", tags.stream().map(TagEntity::getName).collect(Collectors.toList()));
                categoryData.put(TAG_COLORS_KEY, tags.stream().map(TagEntity::getColor).collect(Collectors.toList()));
            } else {
                categoryData.put("tags", new ArrayList<>());
                categoryData.put(TAG_COLORS_KEY, new ArrayList<>());
            }

            result.add(categoryData);
        }

        return result.stream().limit(6).collect(Collectors.toList());
    }

    private List<Map<String, Object>> getRecentTaggedExpenses(List<ExpenseEntity> expenses) {
        List<Map<String, Object>> result = new ArrayList<>();

        // Sort by date descending
        List<ExpenseEntity> sortedExpenses = expenses.stream()
                .sorted((a, b) -> b.getExpenseDate().compareTo(a.getExpenseDate()))
                .limit(12)
                .collect(Collectors.toList());

        for (ExpenseEntity expense : sortedExpenses) {
            List<TagEntity> tags = expenseTagRepository.findTagsByExpense(expense);

            Map<String, Object> expenseData = new HashMap<>();
            expenseData.put("id", expense.getId());
            expenseData.put("name", expense.getName());
            expenseData.put("category", expense.getCategory());
            expenseData.put(AMOUNT_KEY, "-" + formatMoney(expense.getAmount()));
            expenseData.put("icon", getCategoryIcon(expense.getCategory()));
            expenseData.put("iconColor", getCategoryColor(expense.getCategory()));

            if (!tags.isEmpty()) {
                expenseData.put("tags", tags.stream().map(TagEntity::getName).collect(Collectors.toList()));
                expenseData.put(TAG_COLORS_KEY, tags.stream().map(TagEntity::getColor).collect(Collectors.toList()));
            } else {
                expenseData.put("tags", new ArrayList<>());
                expenseData.put(TAG_COLORS_KEY, new ArrayList<>());
            }

            result.add(expenseData);
        }

        return result;
    }

    private String formatMoney(BigDecimal amount) {
        return String.format("$%,.2f", amount);
    }

    private String getCategoryIcon(String category) {
        Map<String, String> icons = new HashMap<>();
        icons.put("Software", "💻");
        icons.put("Travel", "🍽️");
        icons.put("Advertising", "📢");
        icons.put("Marketing", "📢");
        icons.put("Consulting", "⚖️");
        icons.put("Maintenance", "🔧");
        icons.put("Utilities", "⚡");
        icons.put("Office Supplies", "🪑");
        icons.put("Food & Dining", "🍕");
        return icons.getOrDefault(category, "💼");
    }

    private String getCategoryColor(String category) {
        Map<String, String> colors = new HashMap<>();
        colors.put("Software", "#3b82f6");
        colors.put("Travel", "#a855f7");
        colors.put("Advertising", "#ec4899");
        colors.put("Marketing", "#ec4899");
        colors.put("Consulting", "#f59e0b");
        colors.put("Maintenance", "#10b981");
        colors.put("Utilities", "#06b6d4");
        colors.put("Office Supplies", "#8b5cf6");
        colors.put("Food & Dining", "#f59e0b");
        return colors.getOrDefault(category, "#64748b");
    }

    private List<Map<String, Object>> getMonthlyTagData(List<TagEntity> tags, List<ExpenseEntity> expenses) {
        List<Map<String, Object>> result = new ArrayList<>();

        // Get top 3 most used tags
        List<TagEntity> topTags = tags.stream()
                .sorted((a, b) -> Long.compare(
                        expenseTagRepository.countByTag(b),
                        expenseTagRepository.countByTag(a)
                ))
                .limit(3)
                .collect(Collectors.toList());

        // For each top tag, calculate monthly spending
        for (TagEntity tag : topTags) {
            Map<String, Object> tagData = new HashMap<>();
            tagData.put("name", tag.getName());
            tagData.put(COLOR_KEY, tag.getColor());

            // Get expenses for this tag
            List<ExpenseEntity> tagExpenses = expenseTagRepository.findExpensesByTag(tag);

            // Calculate spending for last 6 months
            List<Map<String, Object>> monthlyData = calculateMonthlySpending(tagExpenses);
            tagData.put("data", monthlyData);

            result.add(tagData);
        }

        return result;
    }


    private Map<String, Object> getIncomeExpensesData(List<ExpenseEntity> expenses) {
        Map<String, Object> result = new HashMap<>();

        // Calculate monthly expenses
        List<Map<String, Object>> monthlyExpenses = calculateMonthlySpending(expenses);
        result.put("expenses", monthlyExpenses);

        // Calculate average income (estimate based on expenses + 30% margin)
        List<Map<String, Object>> estimatedIncome = new ArrayList<>();
        for (Map<String, Object> monthExpense : monthlyExpenses) {
            Map<String, Object> incomeData = new HashMap<>();
            incomeData.put(MONTH_KEY, monthExpense.get(MONTH_KEY));

            double expenseAmount = (double) monthExpense.get(AMOUNT_KEY);
            // Estimate income as expenses + 30-50% margin using SecureRandom
            double incomeAmount = expenseAmount * (1.3 + (SECURE_RANDOM.nextDouble() * 0.2)); // Random 30-50% margin
            incomeData.put(AMOUNT_KEY, incomeAmount);

            estimatedIncome.add(incomeData);
        }
        result.put("income", estimatedIncome);

        return result;
    }

    private List<Map<String, Object>> calculateMonthlySpending(List<ExpenseEntity> expenses) {
        List<Map<String, Object>> result = new ArrayList<>();

        // Get months for last 6 months
        String[] months = {"Oct", "Nov", "Dec", "Jan", "Feb", "Mar"};

        // Group expenses by month
        Map<String, BigDecimal> monthlyTotals = new HashMap<>();
        for (String month : months) {
            monthlyTotals.put(month, BigDecimal.ZERO);
        }

        // Calculate totals
        for (ExpenseEntity expense : expenses) {
            String monthName = getMonthName(expense.getExpenseDate().getMonthValue());
            if (monthlyTotals.containsKey(monthName)) {
                monthlyTotals.merge(monthName, expense.getAmount(), BigDecimal::add);
            }
        }

        // Create result list maintaining order
        for (String month : months) {
            Map<String, Object> monthData = new HashMap<>();
            monthData.put(MONTH_KEY, month);
            monthData.put(AMOUNT_KEY, monthlyTotals.get(month).doubleValue());
            result.add(monthData);
        }

        return result;
    }


    private String getMonthName(int monthNumber) {
        String[] monthNames = {"", "Jan", "Feb", "Mar", "Apr", "May", "Jun",
                              "Jul", "Aug", "Sep", "Oct", "Nov", "Dec"};
        if (monthNumber >= 1 && monthNumber <= 12) {
            return monthNames[monthNumber];
        }
        return "Unknown";
    }
}

