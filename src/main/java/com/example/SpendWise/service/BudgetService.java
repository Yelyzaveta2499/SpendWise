package com.example.SpendWise.service;

import com.example.SpendWise.model.entity.BudgetEntity;
import com.example.SpendWise.model.entity.UserEntity;
import com.example.SpendWise.model.repository.BudgetRepository;
import com.example.SpendWise.model.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Service
public class BudgetService {

    private static final String USER_NOT_FOUND_PREFIX = "User not found: ";

    private final BudgetRepository budgetRepository;
    private final UserRepository userRepository;

    public BudgetService(BudgetRepository budgetRepository, UserRepository userRepository) {
        this.budgetRepository = budgetRepository;
        this.userRepository = userRepository;
    }

    public List<BudgetEntity> getBudgetsForUser(String username) {
        UserEntity user = userRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException(USER_NOT_FOUND_PREFIX + username));
        return budgetRepository.findByUserOrderByYearDescMonthDescCategoryAsc(user);
    }

    @SuppressWarnings("unchecked")
    public BudgetEntity createBudgetForUser(String username, Object createRequest) {
        if (!(createRequest instanceof java.util.Map<?, ?> rawMap)) {
            throw new IllegalArgumentException("Unsupported request type for budget");
        }
        java.util.Map<String, Object> map = (java.util.Map<String, Object>) rawMap;

        String category = (String) map.getOrDefault("category", "");
        Object amountRaw = map.get("amount");
        Object monthRaw = map.get("month");
        Object yearRaw = map.get("year");

        if (category == null || category.isBlank()) {
            throw new IllegalArgumentException("Budget category is required");
        }
        category = category.trim();

        BigDecimal amount;
        if (amountRaw instanceof BigDecimal bd) {
            amount = bd;
        } else if (amountRaw instanceof Number num) {
            amount = BigDecimal.valueOf(num.doubleValue());
        } else if (amountRaw instanceof String s && !s.isBlank()) {
            amount = new BigDecimal(s);
        } else {
            throw new IllegalArgumentException("Budget amount is required and must be a number");
        }

        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Budget amount must be greater than zero");
        }

        int month = parseIntOrDefault(monthRaw, LocalDate.now().getMonthValue());
        int year = parseIntOrDefault(yearRaw, LocalDate.now().getYear());

        if (month < 1 || month > 12) {
            throw new IllegalArgumentException("Month must be between 1 and 12");
        }

        if (year < 2000 || year > 2100) {
            throw new IllegalArgumentException("Year must be between 2000 and 2100");
        }

        UserEntity user = userRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException(USER_NOT_FOUND_PREFIX + username));

        if (budgetRepository.findByUserAndCategoryAndYearAndMonth(user, category, year, month).isPresent()) {
            throw new IllegalArgumentException("Budget already exists for this category and month");
        }

        BudgetEntity entity = new BudgetEntity();
        entity.setUser(user);
        entity.setCategory(category);
        entity.setAmount(amount);
        entity.setMonth(month);
        entity.setYear(year);

        return budgetRepository.save(entity);
    }

    public void deleteBudgetForUser(String username, Long budgetId) {
        if (budgetId == null) {
            throw new IllegalArgumentException("budgetId is required");
        }

        UserEntity user = userRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException(USER_NOT_FOUND_PREFIX + username));

        BudgetEntity budget = budgetRepository.findById(budgetId)
                .orElseThrow(() -> new IllegalArgumentException("Budget not found: " + budgetId));

        if (!budget.getUser().getId().equals(user.getId())) {
            throw new SecurityException("Cannot delete budget that does not belong to user: " + username);
        }

        budgetRepository.delete(budget);
    }

    private int parseIntOrDefault(Object raw, int def) {
        if (raw == null) return def;
        if (raw instanceof Number n) return n.intValue();
        if (raw instanceof String s && !s.isBlank()) {
            try {
                return Integer.parseInt(s);
            } catch (Exception ignored) {
                return def;
            }
        }
        return def;
    }
}

