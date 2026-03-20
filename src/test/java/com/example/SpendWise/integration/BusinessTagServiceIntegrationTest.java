package com.example.SpendWise.integration;

import com.example.SpendWise.model.entity.ExpenseEntity;
import com.example.SpendWise.model.entity.TagEntity;
import com.example.SpendWise.model.entity.UserEntity;
import com.example.SpendWise.model.repository.ExpenseRepository;
import com.example.SpendWise.model.repository.ExpenseTagRepository;
import com.example.SpendWise.model.repository.TagRepository;
import com.example.SpendWise.model.repository.UserRepository;
import com.example.SpendWise.service.BusinessAnalyticsService;
import com.example.SpendWise.service.ExpenseService;
import com.example.SpendWise.service.TagService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDate;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Integration tests for Business Tag functionality
 * Tests the complete workflow of business expense tagging using service layer
 */
@SpringBootTest
@Transactional
class BusinessTagServiceIntegrationTest {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private TagRepository tagRepository;

    @Autowired
    private ExpenseRepository expenseRepository;

    @Autowired
    private ExpenseTagRepository expenseTagRepository;

    @Autowired
    private ExpenseService expenseService;

    @Autowired
    private TagService tagService;

    @Autowired
    private BusinessAnalyticsService businessAnalyticsService;

    private UserEntity businessUser;
    private UserEntity individualUser;

    @BeforeEach
    void setup() {
        // Clean up
        expenseTagRepository.deleteAll();
        expenseRepository.deleteAll();
        tagRepository.deleteAll();

        // Get users (created by SecurityConfig)
        businessUser = userRepository.findByUsername("business").orElseThrow();
        individualUser = userRepository.findByUsername("indiv").orElseThrow();
    }

    @Test
    void businessUser_canCreateTagsAndTagExpenses() {
        // Create a business tag
        Map<String, Object> tagData = new HashMap<>();
        tagData.put("name", "Essential");
        tagData.put("color", "#FF5722");
        TagEntity tag = tagService.createTagForUser("business", tagData);

        assertNotNull(tag);
        assertEquals("Essential", tag.getName());
        assertEquals("#FF5722", tag.getColor());

        // Create an expense with the tag
        Map<String, Object> expenseData = new HashMap<>();
        expenseData.put("name", "Office Supplies");
        expenseData.put("category", "Business");
        expenseData.put("amount", 150.00);
        expenseData.put("date", LocalDate.now().toString());

        List<Map<String, Object>> tags = new ArrayList<>();
        Map<String, Object> tagRef = new HashMap<>();
        tagRef.put("id", tag.getId());
        tags.add(tagRef);
        expenseData.put("tags", tags);

        ExpenseEntity expense = expenseService.createExpenseForUser("business", expenseData);

        assertNotNull(expense);
        assertEquals("Office Supplies", expense.getName());

        // Verify expense has the tag
        List<TagEntity> expenseTags = expenseTagRepository.findTagsByExpense(expense);
        assertEquals(1, expenseTags.size());
        assertEquals("Essential", expenseTags.get(0).getName());
    }

//    @Test
//    void updateExpense_shouldReplaceExistingTags() {
//        // Create two tags
//        Map<String, Object> tag1Data = new HashMap<>();
//        tag1Data.put("name", "Essential");
//        tag1Data.put("color", "#FF5722");
//        TagEntity tag1 = tagService.createTagForUser("business", tag1Data);
//
//        Map<String, Object> tag2Data = new HashMap<>();
//        tag2Data.put("name", "Health");
//        tag2Data.put("color", "#4CAF50");
//        TagEntity tag2 = tagService.createTagForUser("business", tag2Data);
//
//        // Create expense with tag1
//        Map<String, Object> expenseData = new HashMap<>();
//        expenseData.put("name", "Medical Bill");
//        expenseData.put("category", "Healthcare");
//        expenseData.put("amount", 200.00);
//        expenseData.put("date", LocalDate.now().toString());
//
//        List<Map<String, Object>> tags = new ArrayList<>();
//        Map<String, Object> tagRef = new HashMap<>();
//        tagRef.put("id", tag1.getId());
//        tags.add(tagRef);
//        expenseData.put("tags", tags);
//
//        ExpenseEntity expense = expenseService.createExpenseForUser("business", expenseData);
//
//        // Verify initial tag
//        List<TagEntity> initialTags = expenseTagRepository.findTagsByExpense(expense);
//        System.out.println("Initial Tags: " + initialTags);
//        assertEquals(1, initialTags.size());
//        assertEquals("Essential", initialTags.getFirst().getName());
//
//        // Update to use tag2 instead
//        Map<String, Object> updateData = new HashMap<>();
//        updateData.put("name", "Medical Bill");
//        updateData.put("category", "Healthcare");
//        updateData.put("amount", 200.00);
//        updateData.put("date", LocalDate.now().toString());
//
//        List<Map<String, Object>> newTags = new ArrayList<>();
//        Map<String, Object> newTagRef = new HashMap<>();
//        newTagRef.put("id", tag2.getId());
//        newTags.add(newTagRef);
//        updateData.put("tags", newTags);
//
//        ExpenseEntity updated = expenseService.updateExpenseForUser("business", expense.getId(), updateData);
//
//        // Verify tags were replaced
//        List<TagEntity> updatedTags = expenseTagRepository.findTagsByExpense(updated);
//        System.out.println("Updated Tags: " + updatedTags);
//        assertEquals(1, updatedTags.size());
//        assertEquals("Health", updatedTags.getFirst().getName());
//    }

//    @Test
//    void updateExpenseWithMultipleTags_shouldNotCreateDuplicates() {
//        // Create two tags
//        Map<String, Object> tag1Data = new HashMap<>();
//        tag1Data.put("name", "Essential");
//        tag1Data.put("color", "#FF5722");
//        TagEntity tag1 = tagService.createTagForUser("business", tag1Data);
//
//        Map<String, Object> tag2Data = new HashMap<>();
//        tag2Data.put("name", "Health");
//        tag2Data.put("color", "#4CAF50");
//        TagEntity tag2 = tagService.createTagForUser("business", tag2Data);
//
//        // Create expense without tags
//        Map<String, Object> expenseData = new HashMap<>();
//        expenseData.put("name", "Business Lunch");
//        expenseData.put("category", "Food");
//        expenseData.put("amount", 50.00);
//        expenseData.put("date", LocalDate.now().toString());
//
//        ExpenseEntity expense = expenseService.createExpenseForUser("business", expenseData);
//
//        // Update with both tags
//        Map<String, Object> updateData = new HashMap<>();
//        updateData.put("name", "Business Lunch");
//        updateData.put("category", "Food");
//        updateData.put("amount", 50.00);
//        updateData.put("date", LocalDate.now().toString());
//
//        List<Map<String, Object>> tags = new ArrayList<>();
//        Map<String, Object> tagRef1 = new HashMap<>();
//        tagRef1.put("id", tag1.getId());
//        tags.add(tagRef1);
//
//        Map<String, Object> tagRef2 = new HashMap<>();
//        tagRef2.put("id", tag2.getId());
//        tags.add(tagRef2);
//
//        updateData.put("tags", tags);
//
//        ExpenseEntity updated = expenseService.updateExpenseForUser("business", expense.getId(), updateData);
//
//        // Verify 2 tags
//        List<TagEntity> expenseTags = expenseTagRepository.findTagsByExpense(updated);
//        assertEquals(2, expenseTags.size());
//
//        // Update again with same tags - should not create duplicates
//        expenseService.updateExpenseForUser("business", expense.getId(), updateData);
//
//        // Verify still only 2 tags
//        updated = expenseRepository.findById(expense.getId()).orElseThrow();
//        expenseTags = expenseTagRepository.findTagsByExpense(updated);
//        assertEquals(2, expenseTags.size());
//    }

    @Test
    void businessAnalytics_shouldReturnTaggedExpenses() {
        // Create tags
        Map<String, Object> tag1Data = new HashMap<>();
        tag1Data.put("name", "Essential");
        tag1Data.put("color", "#FF5722");
        TagEntity tag1 = tagService.createTagForUser("business", tag1Data);

        Map<String, Object> tag2Data = new HashMap<>();
        tag2Data.put("name", "Health");
        tag2Data.put("color", "#4CAF50");
        TagEntity tag2 = tagService.createTagForUser("business", tag2Data);

        // Create expenses with tags
        Map<String, Object> expense1Data = new HashMap<>();
        expense1Data.put("name", "Software License");
        expense1Data.put("category", "Software");
        expense1Data.put("amount", 500.00);
        expense1Data.put("date", LocalDate.now().toString());

        List<Map<String, Object>> tags1 = new ArrayList<>();
        Map<String, Object> tagRef1 = new HashMap<>();
        tagRef1.put("id", tag1.getId());
        tags1.add(tagRef1);
        expense1Data.put("tags", tags1);

        expenseService.createExpenseForUser("business", expense1Data);

        Map<String, Object> expense2Data = new HashMap<>();
        expense2Data.put("name", "Health Insurance");
        expense2Data.put("category", "Healthcare");
        expense2Data.put("amount", 300.00);
        expense2Data.put("date", LocalDate.now().toString());

        List<Map<String, Object>> tags2 = new ArrayList<>();
        Map<String, Object> tagRef2 = new HashMap<>();
        tagRef2.put("id", tag2.getId());
        tags2.add(tagRef2);
        expense2Data.put("tags", tags2);

        expenseService.createExpenseForUser("business", expense2Data);

        // Get analytics
        Map<String, Object> analytics = businessAnalyticsService.getBusinessAnalytics("business");

        assertNotNull(analytics);
        assertTrue(analytics.containsKey("stats"));

        @SuppressWarnings("unchecked")
        Map<String, Object> stats = (Map<String, Object>) analytics.get("stats");
        assertEquals("$800.00", stats.get("totalExpenses"));
        assertEquals(2, stats.get("activeTags"));
        assertEquals(2, stats.get("totalTransactions"));
    }

    @Test
    void deleteExpenseWithTags_shouldRemoveTagAssociations() {
        // Create tag and expense
        Map<String, Object> tagData = new HashMap<>();
        tagData.put("name", "Essential");
        tagData.put("color", "#FF5722");
        TagEntity tag = tagService.createTagForUser("business", tagData);

        Map<String, Object> expenseData = new HashMap<>();
        expenseData.put("name", "Conference Fee");
        expenseData.put("category", "Travel");
        expenseData.put("amount", 1000.00);
        expenseData.put("date", LocalDate.now().toString());

        List<Map<String, Object>> tags = new ArrayList<>();
        Map<String, Object> tagRef = new HashMap<>();
        tagRef.put("id", tag.getId());
        tags.add(tagRef);
        expenseData.put("tags", tags);

        ExpenseEntity expense = expenseService.createExpenseForUser("business", expenseData);

        Long expenseId = expense.getId();

        // Verify tag association exists
        assertEquals(1, expenseTagRepository.findByExpense(expense).size());

        // Delete expense
        expenseService.deleteExpenseForUser("business", expenseId);

        // Verify expense deleted
        assertFalse(expenseRepository.findById(expenseId).isPresent());

        // Verify tag still exists
        assertTrue(tagRepository.findById(tag.getId()).isPresent());
    }

    @Test
    void filterExpensesByTag_shouldReturnOnlyTaggedExpenses() {
        // Create tags
        Map<String, Object> tag1Data = new HashMap<>();
        tag1Data.put("name", "Essential");
        tag1Data.put("color", "#FF5722");
        TagEntity tag1 = tagService.createTagForUser("business", tag1Data);

        Map<String, Object> tag2Data = new HashMap<>();
        tag2Data.put("name", "Health");
        tag2Data.put("color", "#4CAF50");
        TagEntity tag2 = tagService.createTagForUser("business", tag2Data);

        // Create expenses with different tags
        Map<String, Object> expense1Data = new HashMap<>();
        expense1Data.put("name", "Office Rent");
        expense1Data.put("category", "Rent");
        expense1Data.put("amount", 2000.00);
        expense1Data.put("date", LocalDate.now().toString());

        List<Map<String, Object>> tags1 = new ArrayList<>();
        Map<String, Object> tagRef1 = new HashMap<>();
        tagRef1.put("id", tag1.getId());
        tags1.add(tagRef1);
        expense1Data.put("tags", tags1);

        expenseService.createExpenseForUser("business", expense1Data);

        Map<String, Object> expense2Data = new HashMap<>();
        expense2Data.put("name", "Gym Membership");
        expense2Data.put("category", "Fitness");
        expense2Data.put("amount", 100.00);
        expense2Data.put("date", LocalDate.now().toString());

        List<Map<String, Object>> tags2 = new ArrayList<>();
        Map<String, Object> tagRef2 = new HashMap<>();
        tagRef2.put("id", tag2.getId());
        tags2.add(tagRef2);
        expense2Data.put("tags", tags2);

        expenseService.createExpenseForUser("business", expense2Data);

        Map<String, Object> expense3Data = new HashMap<>();
        expense3Data.put("name", "Untagged Expense");
        expense3Data.put("category", "Other");
        expense3Data.put("amount", 50.00);
        expense3Data.put("date", LocalDate.now().toString());

        expenseService.createExpenseForUser("business", expense3Data);

        // Filter by tag1
        List<ExpenseEntity> tag1Expenses = expenseService.getExpensesByTagId("business", tag1.getId());
        assertEquals(1, tag1Expenses.size());
        assertEquals("Office Rent", tag1Expenses.get(0).getName());

        // Filter by tag2
        List<ExpenseEntity> tag2Expenses = expenseService.getExpensesByTagId("business", tag2.getId());
        assertEquals(1, tag2Expenses.size());
        assertEquals("Gym Membership", tag2Expenses.get(0).getName());
    }

    @Test
    void usersShouldHaveIsolatedTags() {
        // Business user creates a tag
        Map<String, Object> businessTagData = new HashMap<>();
        businessTagData.put("name", "Business Tag");
        businessTagData.put("color", "#FF5722");
        TagEntity businessTag = tagService.createTagForUser("business", businessTagData);

        // Individual user creates a tag
        Map<String, Object> individualTagData = new HashMap<>();
        individualTagData.put("name", "Personal Tag");
        individualTagData.put("color", "#4CAF50");
        TagEntity individualTag = tagService.createTagForUser("indiv", individualTagData);

        // Business user should only see their tags
        List<TagEntity> businessTags = tagService.getTagsForUser("business");
        assertEquals(1, businessTags.size());
        assertEquals("Business Tag", businessTags.get(0).getName());

        // Individual user should only see their tags
        List<TagEntity> individualTags = tagService.getTagsForUser("indiv");
        assertEquals(1, individualTags.size());
        assertEquals("Personal Tag", individualTags.get(0).getName());
    }
}

