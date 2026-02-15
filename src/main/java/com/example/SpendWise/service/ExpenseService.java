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
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + username));
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
        String category = (String) map.getOrDefault("category", "Other");
        Object amountRaw = map.get("amount");
        Object dateRaw = map.get("date");

        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("Expense name is required");
        }

        BigDecimal amount;
        if (amountRaw instanceof BigDecimal bd) {
            amount = bd;
        } else if (amountRaw instanceof Number num) {
            amount = BigDecimal.valueOf(num.doubleValue());
        } else if (amountRaw instanceof String s && !s.isBlank()) {
            amount = new BigDecimal(s);
        } else {
            throw new IllegalArgumentException("Expense amount is required");
        }

        LocalDate expenseDate;
        if (dateRaw instanceof LocalDate d) {
            expenseDate = d;
        } else if (dateRaw instanceof String s && !s.isBlank()) {
            expenseDate = LocalDate.parse(s);
        } else {
            expenseDate = LocalDate.now();
        }

        UserEntity user = userRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + username));

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
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + username));

        ExpenseEntity expense = expenseRepository.findById(expenseId)
                .orElseThrow(() -> new IllegalArgumentException("Expense not found: " + expenseId));

        if (!expense.getUser().getId().equals(user.getId())) {
            throw new SecurityException("Cannot delete expense that does not belong to user: " + username);
        }

        expenseRepository.delete(expense);
    }
}
