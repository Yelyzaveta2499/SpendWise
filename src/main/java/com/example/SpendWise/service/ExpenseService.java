package com.example.SpendWise.service;

import com.example.SpendWise.model.entity.ExpenseEntity;
import com.example.SpendWise.model.entity.UserEntity;
import com.example.SpendWise.model.repository.ExpenseRepository;
import com.example.SpendWise.model.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Service
public class ExpenseService {

    private static final String USER_NOT_FOUND_PREFIX = "User not found: ";

    private final ExpenseRepository expenseRepository;
    private final UserRepository userRepository;

    public ExpenseService(ExpenseRepository expenseRepository, UserRepository userRepository) {
        this.expenseRepository = expenseRepository;
        this.userRepository = userRepository;
    }

    /**
     * Loadong all expenses for the given username.
     */
    public List<ExpenseEntity> getExpensesForUser(String username) {
        UserEntity user = userRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException(USER_NOT_FOUND_PREFIX + username));
        return expenseRepository.findTop20ByUserOrderByExpenseDateDesc(user);
    }

    /**
     * Creatin a new expense for the given username.
     * a simple map-like request object:
     *   name (String), category (String), amount (BigDecimal or Number), date (LocalDate or String "yyyy-MM-dd").
     */
    @SuppressWarnings("unchecked")
    public ExpenseEntity createExpenseForUser(String username, Object expenseCreateRequest) {
        if (!(expenseCreateRequest instanceof java.util.Map<?, ?> rawMap)) {
            throw new IllegalArgumentException("Unsupported request type for expenseCreateRequest");
        }
        java.util.Map<String, Object> map = (java.util.Map<String, Object>) rawMap;

        String name = (String) map.getOrDefault("name", "");
        String category = (String) map.getOrDefault("category", "");
        Object amountRaw = map.get("amount");
        Object dateRaw = map.get("date");

        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("Expense name is required");
        }

        if (category == null || category.isBlank()) {
            throw new IllegalArgumentException("Expense category is required");
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
            throw new IllegalArgumentException("Expense amount is required and must be a number");
        }

        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Expense amount must be greater than zero");
        }

        LocalDate expenseDate;
        if (dateRaw instanceof LocalDate d) {
            expenseDate = d;
        } else if (dateRaw instanceof String s && !s.isBlank()) {
            try {
                expenseDate = LocalDate.parse(s);
            } catch (Exception e) {
                throw new IllegalArgumentException("Expense date must be in format YYYY-MM-DD");
            }
        } else {
            throw new IllegalArgumentException("Expense date is required");
        }

        if (expenseDate.isAfter(LocalDate.now())) {
            throw new IllegalArgumentException("Expense date cannot be in the future");
        }

        UserEntity user = userRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException(USER_NOT_FOUND_PREFIX + username));

        ExpenseEntity entity = new ExpenseEntity();
        entity.setUser(user);
        entity.setName(name);
        entity.setCategory(category);
        entity.setAmount(amount);
        entity.setExpenseDate(expenseDate);

        return expenseRepository.save(entity);
    }

    /**
     * Deleting an expense only if it belongs to the given username.
     */
    public void deleteExpenseForUser(String username, Long expenseId) {
        if (expenseId == null) {
            throw new IllegalArgumentException("expenseId is required");
        }

        UserEntity user = userRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException(USER_NOT_FOUND_PREFIX + username));

        ExpenseEntity expense = expenseRepository.findById(expenseId)
                .orElseThrow(() -> new IllegalArgumentException("Expense not found: " + expenseId));

        if (!expense.getUser().getId().equals(user.getId())) {
            throw new SecurityException("Cannot delete expense that does not belong to user: " + username);
        }

        expenseRepository.delete(expense);
    }

    /**
     * Updating an existing expense for the given username.
     */
    @SuppressWarnings("unchecked")
    public ExpenseEntity updateExpenseForUser(String username, Long expenseId, Object updateRequest) {
        if (expenseId == null) {
            throw new IllegalArgumentException("expenseId is required");
        }

        if (!(updateRequest instanceof java.util.Map<?, ?> rawMap)) {
            throw new IllegalArgumentException("Unsupported request type for expenseCreateRequest");
        }
        java.util.Map<String, Object> map = (java.util.Map<String, Object>) rawMap;

        UserEntity user = userRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException(USER_NOT_FOUND_PREFIX + username));

        ExpenseEntity expense = expenseRepository.findById(expenseId)
                .orElseThrow(() -> new IllegalArgumentException("Expense not found: " + expenseId));

        if (!expense.getUser().getId().equals(user.getId())) {
            throw new SecurityException("Cannot update expense that does not belong to user: " + username);
        }

        Object nameRaw = map.get("name");
        if (nameRaw instanceof String s && !s.isBlank()) {
            expense.setName(s);
        }

        Object categoryRaw = map.get("category");
        if (categoryRaw instanceof String s && !s.isBlank()) {
            expense.setCategory(s.trim());
        }

        Object amountRaw = map.get("amount");
        if (amountRaw != null) {
            BigDecimal amount;
            if (amountRaw instanceof BigDecimal bd) {
                amount = bd;
            } else if (amountRaw instanceof Number num) {
                amount = BigDecimal.valueOf(num.doubleValue());
            } else if (amountRaw instanceof String s && !s.isBlank()) {
                amount = new BigDecimal(s);
            } else {
                throw new IllegalArgumentException("Expense amount must be a number");
            }

            if (amount.compareTo(BigDecimal.ZERO) <= 0) {
                throw new IllegalArgumentException("Expense amount must be greater than zero");
            }

            expense.setAmount(amount);
        }

        Object dateRaw = map.get("date");
        if (dateRaw != null) {
            LocalDate expenseDate;
            if (dateRaw instanceof LocalDate d) {
                expenseDate = d;
            } else if (dateRaw instanceof String s && !s.isBlank()) {
                try {
                    expenseDate = LocalDate.parse(s);
                } catch (Exception e) {
                    throw new IllegalArgumentException("Expense date must be in format YYYY-MM-DD");
                }
            } else {
                throw new IllegalArgumentException("Expense date must be in format YYYY-MM-DD");
            }

            if (expenseDate.isAfter(LocalDate.now())) {
                throw new IllegalArgumentException("Expense date cannot be in the future");
            }

            expense.setExpenseDate(expenseDate);
        }

        return expenseRepository.save(expense);
    }
}
