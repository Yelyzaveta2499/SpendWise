package com.example.SpendWise.service;

import com.example.SpendWise.model.entity.ExpenseEntity;
import com.example.SpendWise.model.entity.ExpenseTagEntity;
import com.example.SpendWise.model.entity.TagEntity;
import com.example.SpendWise.model.entity.UserEntity;
import com.example.SpendWise.model.repository.ExpenseRepository;
import com.example.SpendWise.model.repository.ExpenseTagRepository;
import com.example.SpendWise.model.repository.TagRepository;
import com.example.SpendWise.model.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Service
public class ExpenseService {

    private static final String USER_NOT_FOUND_PREFIX = "User not found: ";
    private static final String CATEGORY_KEY = "category";
    private static final String AMOUNT_KEY = "amount";
    private static final String EXPENSE_NOT_FOUND_PREFIX = "Expense not found: ";
    private static final String TAG_NOT_FOUND_MESSAGE = "Tag not found: ";

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


    public List<ExpenseEntity> getExpensesForUser(String username) {
        UserEntity user = userRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException(USER_NOT_FOUND_PREFIX + username));
        return expenseRepository.findTop20ByUserOrderByExpenseDateDesc(user);
    }

    // Helper to map an ExpenseEntity to a lightweight DTO for API responses
    public Map<String, Object> toExpenseDto(ExpenseEntity expense) {
        Map<String, Object> dto = new java.util.HashMap<>();
        dto.put("id", expense.getId());
        dto.put("name", expense.getName());
        dto.put(CATEGORY_KEY, expense.getCategory());
        dto.put(AMOUNT_KEY, expense.getAmount());
        dto.put("expenseDate", expense.getExpenseDate());

        // Flatten tag names only to avoid deep nesting / recursion
        List<String> tagNames = new ArrayList<>();
        if (expense.getExpenseTags() != null) {
            for (ExpenseTagEntity et : expense.getExpenseTags()) {
                if (et.getTag() != null && et.getTag().getName() != null) {
                    tagNames.add(et.getTag().getName());
                }
            }
        }
        dto.put("tags", tagNames);
        return dto;
    }

    public List<Map<String, Object>> getExpenseDtosForUser(String username) {
        List<ExpenseEntity> entities = getExpensesForUser(username);
        List<Map<String, Object>> result = new ArrayList<>();
        for (ExpenseEntity e : entities) {
            result.add(toExpenseDto(e));
        }
        return result;
    }

    @SuppressWarnings("unchecked")
    public ExpenseEntity createExpenseForUser(String username, Object expenseCreateRequest) {
        if (!(expenseCreateRequest instanceof java.util.Map<?, ?> rawMap)) {
            throw new IllegalArgumentException("Unsupported request type for expenseCreateRequest");
        }
        java.util.Map<String, Object> map = (java.util.Map<String, Object>) rawMap;

        String name = extractAndValidateName(map);
        String category = extractAndValidateCategory(map);
        BigDecimal amount = extractAndValidateAmount(map);
        LocalDate expenseDate = extractAndValidateDate(map);

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

    private String extractAndValidateName(Map<String, Object> map) {
        String name = (String) map.getOrDefault("name", "");
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("Expense name is required");
        }
        return name;
    }

    private String extractAndValidateCategory(Map<String, Object> map) {
        String category = (String) map.getOrDefault(CATEGORY_KEY, "");
        if (category == null || category.isBlank()) {
            throw new IllegalArgumentException("Expense category is required");
        }
        return category.trim();
    }

    private BigDecimal extractAndValidateAmount(Map<String, Object> map) {
        Object amountRaw = map.get(AMOUNT_KEY);
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
        return amount;
    }

    private LocalDate extractAndValidateDate(Map<String, Object> map) {
        Object dateRaw = map.get("date");
        if (dateRaw instanceof LocalDate d) {
            return d;
        } else if (dateRaw instanceof String s && !s.isBlank()) {
            try {
                return LocalDate.parse(s);
            } catch (Exception e) {
                throw new IllegalArgumentException("Expense date must be in format YYYY-MM-DD");
            }
        } else {
            throw new IllegalArgumentException("Expense date is required");
        }
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
                .orElseThrow(() -> new IllegalArgumentException(EXPENSE_NOT_FOUND_PREFIX + expenseId));

        if (!expense.getUser().getId().equals(user.getId())) {
            throw new SecurityException("Cannot delete expense that does not belong to user: " + username);
        }

        expenseRepository.delete(expense);
    }

    /**
     * Updating an existing expense for the authenticated user.
     */
    @Transactional
    public ExpenseEntity updateExpenseForUser(String username, Long expenseId, Object updateRequest) {
        validateExpenseId(expenseId);
        java.util.Map<String, Object> map = validateAndCastUpdateRequest(updateRequest);

        UserEntity user = getUserByUsername(username);
        ExpenseEntity expense = getExpenseById(expenseId);
        validateExpenseOwnership(expense, user, username);

        updateExpenseFields(expense, map);
        updateExpenseTagsIfPresent(expense, map, user);

        return expenseRepository.save(expense);
    }

    private void validateExpenseId(Long expenseId) {
        if (expenseId == null) {
            throw new IllegalArgumentException("expenseId is required");
        }
    }

    private java.util.Map<String, Object> validateAndCastUpdateRequest(Object updateRequest) {
        if (!(updateRequest instanceof java.util.Map)) {
            throw new IllegalArgumentException("Unsupported request type for expenseCreateRequest");
        }
        return (java.util.Map<String, Object>) updateRequest;
    }

    private UserEntity getUserByUsername(String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException(USER_NOT_FOUND_PREFIX + username));
    }

    private ExpenseEntity getExpenseById(Long expenseId) {
        return expenseRepository.findById(expenseId)
                .orElseThrow(() -> new IllegalArgumentException(EXPENSE_NOT_FOUND_PREFIX + expenseId));
    }

    private void validateExpenseOwnership(ExpenseEntity expense, UserEntity user, String username) {
        if (!expense.getUser().getId().equals(user.getId())) {
            throw new SecurityException("Cannot update expense that does not belong to user: " + username);
        }
    }

    private void updateExpenseFields(ExpenseEntity expense, java.util.Map<String, Object> map) {
        Object nameRaw = map.get("name");
        if (nameRaw instanceof String s && !s.isBlank()) {
            expense.setName(s);
        }

        Object categoryRaw = map.get(CATEGORY_KEY);
        if (categoryRaw instanceof String s && !s.isBlank()) {
            expense.setCategory(s.trim());
        }

        Object amountRaw = map.get(AMOUNT_KEY);
        if (amountRaw != null) {
            BigDecimal amount = parseAmount(amountRaw);
            if (amount.compareTo(BigDecimal.ZERO) <= 0) {
                throw new IllegalArgumentException("Expense amount must be greater than zero");
            }
            expense.setAmount(amount);
        }

        Object dateRaw = map.get("date");
        if (dateRaw != null) {
            LocalDate expenseDate = parseDate(dateRaw);
            if (expenseDate.isAfter(LocalDate.now())) {
                throw new IllegalArgumentException("Expense date cannot be in the future");
            }
            expense.setExpenseDate(expenseDate);
        }
    }

    private BigDecimal parseAmount(Object amountRaw) {
        // Reverting to if-else for amount parsing due to Java version compatibility
        if (amountRaw instanceof BigDecimal bd) {
            return bd;
        } else if (amountRaw instanceof Number num) {
            return BigDecimal.valueOf(num.doubleValue());
        } else if (amountRaw instanceof String s && !s.isBlank()) {
            return new BigDecimal(s);
        } else {
            throw new IllegalArgumentException("Expense amount must be a number");
        }
    }

    private LocalDate parseDate(Object dateRaw) {
        if (dateRaw instanceof LocalDate d) {
            return d;
        } else if (dateRaw instanceof String s && !s.isBlank()) {
            try {
                return LocalDate.parse(s);
            } catch (Exception e) {
                throw new IllegalArgumentException("Expense date must be in format YYYY-MM-DD");
            }
        } else {
            throw new IllegalArgumentException("Expense date must be in format YYYY-MM-DD");
        }
    }

    private void updateExpenseTagsIfPresent(ExpenseEntity expense, java.util.Map<String, Object> map, UserEntity user) {
        Object tagsRaw = map.get("tags");
        if (tagsRaw instanceof List<?> tagsList) {
            updateExpenseTags(expense, tagsList, user);
        }
    }

    @Transactional
    protected void updateExpenseTags(ExpenseEntity expense, List<?> tagsList, UserEntity user) {
        // Delete all existing tag associations from database to prevent duplicates
        expenseTagRepository.deleteByExpense(expense);
        
        // Log the state after deletion
        System.out.println("Tags after deletion: " + expense.getExpenseTags());

        // Clear the in-memory collection
        expense.getExpenseTags().clear();

        // Log the state after clearing in-memory tags
        System.out.println("Tags after clearing in-memory collection: " + expense.getExpenseTags());

        // Process and collect desired tag IDs
        Set<Long> desiredTagIds = extractTagIds(tagsList);

        // Add new tags to the expense
        addTagsToExpense(expense, desiredTagIds, user);

        // Log the final state of tags
        System.out.println("Final tags after update: " + expense.getExpenseTags());
    }

    private void updateExpenseTags(ExpenseEntity expense, Set<Long> desiredTagIds, UserEntity user) {
        for (Long tagId : desiredTagIds) {
            TagEntity tag = tagRepository.findById(tagId).orElse(null);
            if (tag != null && tag.getUser().getId().equals(user.getId())) {
                expense.addTag(tag);
            }
        }
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
                // Check if tag is not already added to avoid duplicates
                TagEntity finalTag = tag;
                boolean alreadyAdded = expense.getExpenseTags().stream()
                    .anyMatch(et -> et.getTag().getId().equals(finalTag.getId()));

                if (!alreadyAdded) {
                    expense.addTag(tag);
                }
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
                .orElseThrow(() -> new IllegalArgumentException(TAG_NOT_FOUND_MESSAGE + tagName));

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
                .orElseThrow(() -> new IllegalArgumentException(EXPENSE_NOT_FOUND_PREFIX + expenseId));

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
                .orElseThrow(() -> new IllegalArgumentException(EXPENSE_NOT_FOUND_PREFIX + expenseId));

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
                .orElseThrow(() -> new IllegalArgumentException(EXPENSE_NOT_FOUND_PREFIX + expenseId));

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

    private Set<Long> extractTagIds(List<?> tagsList) {
        Set<Long> tagIds = new HashSet<>();
        for (Object tagObj : tagsList) {
            if (tagObj instanceof Map<?, ?> tagMap) {
                Object idObj = tagMap.get("id");
                if (idObj instanceof Number tagId) {
                    tagIds.add(tagId.longValue());
                }
            }
        }
        return tagIds;
    }

    private void addTagsToExpense(ExpenseEntity expense, Set<Long> tagIds, UserEntity user) {
        for (Long tagId : tagIds) {
            TagEntity tag = tagRepository.findById(tagId).orElse(null);
            if (tag != null && tag.getUser().getId().equals(user.getId())) {
                expense.addTag(tag);
            }
        }
    }
}
