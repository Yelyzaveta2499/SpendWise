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
import java.util.*;
import java.util.stream.Collectors;

@Service
public class BusinessAnalyticsService {

    private final UserRepository userRepository;
    private final TagRepository tagRepository;
    private final ExpenseRepository expenseRepository;
    private final ExpenseTagRepository expenseTagRepository;

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
            tagData.put("color", tag.getColor());
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
                tagSpending.put("amount", totalAmount.doubleValue());
                tagSpending.put("color", tag.getColor());
                result.add(tagSpending);
            }
        }

        // Sort by amount descending and take top 6
        result.sort((a, b) -> Double.compare((Double) b.get("amount"), (Double) a.get("amount")));
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
            categoryData.put("amount", formatMoney(entry.getValue()));

            List<TagEntity> tags = categoryTags.get(entry.getKey());
            if (tags != null && !tags.isEmpty()) {
                categoryData.put("tags", tags.stream().map(TagEntity::getName).collect(Collectors.toList()));
                categoryData.put("tagColors", tags.stream().map(TagEntity::getColor).collect(Collectors.toList()));
            } else {
                categoryData.put("tags", new ArrayList<>());
                categoryData.put("tagColors", new ArrayList<>());
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
            expenseData.put("amount", "-" + formatMoney(expense.getAmount()));
            expenseData.put("icon", getCategoryIcon(expense.getCategory()));
            expenseData.put("iconColor", getCategoryColor(expense.getCategory()));

            if (!tags.isEmpty()) {
                expenseData.put("tags", tags.stream().map(TagEntity::getName).collect(Collectors.toList()));
                expenseData.put("tagColors", tags.stream().map(TagEntity::getColor).collect(Collectors.toList()));
            } else {
                expenseData.put("tags", new ArrayList<>());
                expenseData.put("tagColors", new ArrayList<>());
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
}


