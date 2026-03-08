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
import java.time.LocalDate;
import java.util.List;

@Service
public class ExpenseService {

    private static final String USER_NOT_FOUND_PREFIX = "User not found: ";

    private final ExpenseRepository expenseRepository;
    private final UserRepository userRepository;
    private final TagRepository tagRepository;
    private final ExpenseTagRepository expenseTagRepository;

    public ExpenseService(ExpenseRepository expenseRepository,
                         UserRepository userRepository,
                         TagRepository tagRepository,
                         ExpenseTagRepository expenseTagRepository) {
        this.expenseRepository = expenseRepository;
        this.userRepository = userRepository;
        this.tagRepository = tagRepository;
        this.expenseTagRepository = expenseTagRepository;
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

        ExpenseEntity saved = expenseRepository.save(entity);

        // Handle tags if provided
        Object tagsRaw = map.get("tags");
        if (tagsRaw instanceof List<?> tagsList) {
            processTags(saved, tagsList, user);
        }

        return saved;
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

        // Handle tags if provided
        Object tagsRaw = map.get("tags");
        if (tagsRaw instanceof List<?> tagsList) {
            // Clear existing tags and add new ones
            expense.clearTags();
            processTags(expense, tagsList, user);
        }

        return expenseRepository.save(expense);
    }

    /**
     * Process tags for an expense - accepts tag names or IDs
     */
    @SuppressWarnings("unchecked")
    private void processTags(ExpenseEntity expense, List<?> tagsList, UserEntity user) {
        for (Object tagObj : tagsList) {
            TagEntity tag = null;

            if (tagObj instanceof String tagName) {
                // Tag name provided - find or skip
                tag = tagRepository.findByUserAndName(user, tagName).orElse(null);
            } else if (tagObj instanceof Number tagId) {
                // Tag ID provided
                tag = tagRepository.findById(tagId.longValue()).orElse(null);
                // Verify tag belongs to user
                if (tag != null && !tag.getUser().getId().equals(user.getId())) {
                    tag = null;
                }
            } else if (tagObj instanceof java.util.Map<?, ?> tagMap) {
                // Tag object with name or id
                java.util.Map<String, Object> map = (java.util.Map<String, Object>) tagMap;
                Object nameObj = map.get("name");
                Object idObj = map.get("id");

                if (idObj instanceof Number tagId) {
                    tag = tagRepository.findById(tagId.longValue()).orElse(null);
                    if (tag != null && !tag.getUser().getId().equals(user.getId())) {
                        tag = null;
                    }
                } else if (nameObj instanceof String tagName) {
                    tag = tagRepository.findByUserAndName(user, tagName).orElse(null);
                }
            }

            if (tag != null) {
                expense.addTag(tag);
            }
        }
    }

    /**
     * Get all expenses filtered by tag name
     */
    public List<ExpenseEntity> getExpensesByTag(String username, String tagName) {
        UserEntity user = userRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException(USER_NOT_FOUND_PREFIX + username));

        TagEntity tag = tagRepository.findByUserAndName(user, tagName)
                .orElseThrow(() -> new IllegalArgumentException("Tag not found: " + tagName));

        return expenseTagRepository.findExpensesByTag(tag);
    }

    /**
     * Get all expenses filtered by tag ID
     */
    public List<ExpenseEntity> getExpensesByTagId(String username, Long tagId) {
        UserEntity user = userRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException(USER_NOT_FOUND_PREFIX + username));

        TagEntity tag = tagRepository.findById(tagId)
                .orElseThrow(() -> new IllegalArgumentException("Tag not found: " + tagId));

        // Verify tag belongs to user
        if (!tag.getUser().getId().equals(user.getId())) {
            throw new SecurityException("Cannot access tag that does not belong to user: " + username);
        }

        return expenseTagRepository.findExpensesByTag(tag);
    }

    /**
     * Get all tags for a specific expense
     */
    public List<TagEntity> getTagsForExpense(String username, Long expenseId) {
        UserEntity user = userRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException(USER_NOT_FOUND_PREFIX + username));

        ExpenseEntity expense = expenseRepository.findById(expenseId)
                .orElseThrow(() -> new IllegalArgumentException("Expense not found: " + expenseId));

        // Verify expense belongs to user
        if (!expense.getUser().getId().equals(user.getId())) {
            throw new SecurityException("Cannot access expense that does not belong to user: " + username);
        }

        return expenseTagRepository.findTagsByExpense(expense);
    }

    /**
     * Add a tag to an expense
     */
    public ExpenseEntity addTagToExpense(String username, Long expenseId, Long tagId) {
        UserEntity user = userRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException(USER_NOT_FOUND_PREFIX + username));

        ExpenseEntity expense = expenseRepository.findById(expenseId)
                .orElseThrow(() -> new IllegalArgumentException("Expense not found: " + expenseId));

        if (!expense.getUser().getId().equals(user.getId())) {
            throw new SecurityException("Cannot modify expense that does not belong to user: " + username);
        }

        TagEntity tag = tagRepository.findById(tagId)
                .orElseThrow(() -> new IllegalArgumentException("Tag not found: " + tagId));

        if (!tag.getUser().getId().equals(user.getId())) {
            throw new SecurityException("Cannot use tag that does not belong to user: " + username);
        }

        // Check if tag is already added
        if (!expenseTagRepository.existsByExpenseAndTag(expense, tag)) {
            expense.addTag(tag);
            expenseRepository.save(expense);
        }

        return expense;
    }

    /**
     * Remove a tag from an expense
     */
    public ExpenseEntity removeTagFromExpense(String username, Long expenseId, Long tagId) {
        UserEntity user = userRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException(USER_NOT_FOUND_PREFIX + username));

        ExpenseEntity expense = expenseRepository.findById(expenseId)
                .orElseThrow(() -> new IllegalArgumentException("Expense not found: " + expenseId));

        if (!expense.getUser().getId().equals(user.getId())) {
            throw new SecurityException("Cannot modify expense that does not belong to user: " + username);
        }

        TagEntity tag = tagRepository.findById(tagId)
                .orElseThrow(() -> new IllegalArgumentException("Tag not found: " + tagId));

        if (!tag.getUser().getId().equals(user.getId())) {
            throw new SecurityException("Cannot use tag that does not belong to user: " + username);
        }

        expense.removeTag(tag);
        expenseRepository.save(expense);

        return expense;
    }
}
