package com.example.SpendWise.unit.service;

import com.example.SpendWise.model.entity.ExpenseEntity;
import com.example.SpendWise.model.entity.TagEntity;
import com.example.SpendWise.model.entity.UserEntity;
import com.example.SpendWise.model.repository.ExpenseRepository;
import com.example.SpendWise.model.repository.ExpenseTagRepository;
import com.example.SpendWise.model.repository.TagRepository;
import com.example.SpendWise.model.repository.UserRepository;
import com.example.SpendWise.service.BusinessAnalyticsService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Unit tests for BusinessAnalyticsService
 * Tests business analytics calculations and data aggregation
 */
class BusinessAnalyticsServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private TagRepository tagRepository;

    @Mock
    private ExpenseRepository expenseRepository;

    @Mock
    private ExpenseTagRepository expenseTagRepository;

    @InjectMocks
    private BusinessAnalyticsService businessAnalyticsService;

    private UserEntity testUser;
    private TagEntity essentialTag;
    private TagEntity healthTag;
    private ExpenseEntity expense1;
    private ExpenseEntity expense2;

    @BeforeEach
    void setup() {
        MockitoAnnotations.openMocks(this);

        // Setup test user
        testUser = new UserEntity();
        testUser.setId(1);
        testUser.setUsername("business");

        // Setup test tags
        essentialTag = new TagEntity();
        essentialTag.setId(1L);
        essentialTag.setUser(testUser);
        essentialTag.setName("Essential");
        essentialTag.setColor("#FF5722");

        healthTag = new TagEntity();
        healthTag.setId(2L);
        healthTag.setUser(testUser);
        healthTag.setName("Health");
        healthTag.setColor("#4CAF50");

        // Setup test expenses
        expense1 = new ExpenseEntity();
        expense1.setId(1L);
        expense1.setUser(testUser);
        expense1.setName("Office Supplies");
        expense1.setCategory("Business");
        expense1.setAmount(new BigDecimal("150.00"));
        expense1.setExpenseDate(LocalDate.now());

        expense2 = new ExpenseEntity();
        expense2.setId(2L);
        expense2.setUser(testUser);
        expense2.setName("Medical Bill");
        expense2.setCategory("Healthcare");
        expense2.setAmount(new BigDecimal("200.00"));
        expense2.setExpenseDate(LocalDate.now());
    }

    @Test
    void getBusinessAnalytics_withValidUser_shouldReturnCompleteAnalytics() {

        when(userRepository.findByUsername("business")).thenReturn(Optional.of(testUser));
        when(tagRepository.findByUserOrderByNameAsc(testUser)).thenReturn(Arrays.asList(essentialTag, healthTag));
        when(expenseRepository.findByUser(testUser)).thenReturn(Arrays.asList(expense1, expense2));
        when(expenseTagRepository.countByTag(any(TagEntity.class))).thenReturn(1L);
        when(expenseTagRepository.findExpensesByTag(any(TagEntity.class))).thenReturn(Arrays.asList(expense1));
        when(expenseTagRepository.findTagsByExpense(any(ExpenseEntity.class))).thenReturn(Collections.emptyList());


        Map<String, Object> analytics = businessAnalyticsService.getBusinessAnalytics("business");


        assertNotNull(analytics);
        assertTrue(analytics.containsKey("stats"));
        assertTrue(analytics.containsKey("expenseTags"));
        assertTrue(analytics.containsKey("spendingByTag"));
        assertTrue(analytics.containsKey("categoryData"));
        assertTrue(analytics.containsKey("recentExpenses"));
        assertTrue(analytics.containsKey("monthlyTagData"));
        assertTrue(analytics.containsKey("incomeExpensesData"));

        verify(userRepository).findByUsername("business");
        verify(tagRepository).findByUserOrderByNameAsc(testUser);
        verify(expenseRepository).findByUser(testUser);
    }

    @Test
    void getBusinessAnalytics_withNonExistentUser_shouldThrowException() {

        when(userRepository.findByUsername("unknown")).thenReturn(Optional.empty());


        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            businessAnalyticsService.getBusinessAnalytics("unknown");
        });

        assertEquals("User not found: unknown", exception.getMessage());
    }

    @Test
    void getBusinessAnalytics_withNoExpenses_shouldReturnZeroStats() {
        // Given
        when(userRepository.findByUsername("business")).thenReturn(Optional.of(testUser));
        when(tagRepository.findByUserOrderByNameAsc(testUser)).thenReturn(Collections.emptyList());
        when(expenseRepository.findByUser(testUser)).thenReturn(Collections.emptyList());

        // When
        Map<String, Object> analytics = businessAnalyticsService.getBusinessAnalytics("business");

        // Then
        assertNotNull(analytics);

        @SuppressWarnings("unchecked")
        Map<String, Object> stats = (Map<String, Object>) analytics.get("stats");
        assertNotNull(stats);
        assertEquals("$0.00", stats.get("totalExpenses"));
        assertEquals(0, stats.get("activeTags"));
        assertEquals(0, stats.get("totalTransactions"));
    }

    @Test
    void getBusinessAnalytics_withExpenses_shouldCalculateCorrectTotals() {
        // Given
        when(userRepository.findByUsername("business")).thenReturn(Optional.of(testUser));
        when(tagRepository.findByUserOrderByNameAsc(testUser)).thenReturn(Arrays.asList(essentialTag, healthTag));
        when(expenseRepository.findByUser(testUser)).thenReturn(Arrays.asList(expense1, expense2));
        when(expenseTagRepository.countByTag(any(TagEntity.class))).thenReturn(1L);
        when(expenseTagRepository.findExpensesByTag(essentialTag))
                .thenReturn(Collections.singletonList(expense1));
        when(expenseTagRepository.findExpensesByTag(healthTag))
                .thenReturn(Collections.singletonList(expense2));
        when(expenseTagRepository.findTagsByExpense(any(ExpenseEntity.class))).thenReturn(Collections.emptyList());

        // When
        Map<String, Object> analytics = businessAnalyticsService.getBusinessAnalytics("business");

        // Then
        @SuppressWarnings("unchecked")
        Map<String, Object> stats = (Map<String, Object>) analytics.get("stats");
        assertEquals("$350.00", stats.get("totalExpenses")); // 150 + 200
        assertEquals(2, stats.get("activeTags"));
        assertEquals(2, stats.get("totalTransactions"));
    }

    @Test
    void getBusinessAnalytics_shouldReturnExpenseTagsWithCounts() {
        // Given
        when(userRepository.findByUsername("business")).thenReturn(Optional.of(testUser));
        when(tagRepository.findByUserOrderByNameAsc(testUser)).thenReturn(Arrays.asList(essentialTag, healthTag));
        when(expenseRepository.findByUser(testUser)).thenReturn(Arrays.asList(expense1, expense2));
        when(expenseTagRepository.countByTag(essentialTag)).thenReturn(3L);
        when(expenseTagRepository.countByTag(healthTag)).thenReturn(1L);
        when(expenseTagRepository.findExpensesByTag(any(TagEntity.class))).thenReturn(Collections.emptyList());
        when(expenseTagRepository.findTagsByExpense(any(ExpenseEntity.class))).thenReturn(Collections.emptyList());

        // When
        Map<String, Object> analytics = businessAnalyticsService.getBusinessAnalytics("business");

        // Then
        @SuppressWarnings("unchecked")
        List<Map<String, Object>> expenseTags = (List<Map<String, Object>>) analytics.get("expenseTags");
        assertNotNull(expenseTags);
        assertEquals(2, expenseTags.size());

        Map<String, Object> firstTag = expenseTags.get(0);
        assertEquals("Essential", firstTag.get("name"));
        assertEquals("#FF5722", firstTag.get("color"));
        assertEquals(3L, firstTag.get("count"));
    }

    @Test
    void getBusinessAnalytics_shouldReturnSpendingByTag() {

        when(userRepository.findByUsername("business")).thenReturn(Optional.of(testUser));
        when(tagRepository.findByUserOrderByNameAsc(testUser)).thenReturn(Arrays.asList(essentialTag, healthTag));
        when(expenseRepository.findByUser(testUser)).thenReturn(Arrays.asList(expense1, expense2));
        when(expenseTagRepository.countByTag(any(TagEntity.class))).thenReturn(1L);
        when(expenseTagRepository.findExpensesByTag(essentialTag))
                .thenReturn(Collections.singletonList(expense1));
        when(expenseTagRepository.findExpensesByTag(healthTag))
                .thenReturn(Collections.singletonList(expense2));
        when(expenseTagRepository.findTagsByExpense(any(ExpenseEntity.class))).thenReturn(Collections.emptyList());


        Map<String, Object> analytics = businessAnalyticsService.getBusinessAnalytics("business");


        @SuppressWarnings("unchecked")
        List<Map<String, Object>> spendingByTag = (List<Map<String, Object>>) analytics.get("spendingByTag");
        assertNotNull(spendingByTag);
        assertTrue(spendingByTag.size() > 0);

        // Should be sorted by amount descending
        Map<String, Object> topSpending = spendingByTag.get(0);
        assertTrue(topSpending.containsKey("name"));
        assertTrue(topSpending.containsKey("amount"));
        assertTrue(topSpending.containsKey("color"));
    }

    @Test
    void getBusinessAnalytics_shouldReturnRecentExpenses() {

        when(userRepository.findByUsername("business")).thenReturn(Optional.of(testUser));
        when(tagRepository.findByUserOrderByNameAsc(testUser)).thenReturn(Arrays.asList(essentialTag));
        when(expenseRepository.findByUser(testUser)).thenReturn(Arrays.asList(expense1, expense2));
        when(expenseTagRepository.countByTag(any(TagEntity.class))).thenReturn(1L);
        when(expenseTagRepository.findExpensesByTag(any(TagEntity.class))).thenReturn(Collections.emptyList());
        when(expenseTagRepository.findTagsByExpense(expense1))
                .thenReturn(Collections.singletonList(essentialTag));
        when(expenseTagRepository.findTagsByExpense(expense2))
                .thenReturn(Collections.emptyList());


        Map<String, Object> analytics = businessAnalyticsService.getBusinessAnalytics("business");


        @SuppressWarnings("unchecked")
        List<Map<String, Object>> recentExpenses = (List<Map<String, Object>>) analytics.get("recentExpenses");
        assertNotNull(recentExpenses);
        assertEquals(2, recentExpenses.size());

        Map<String, Object> firstExpense = recentExpenses.get(0);
        assertTrue(firstExpense.containsKey("name"));
        assertTrue(firstExpense.containsKey("category"));
        assertTrue(firstExpense.containsKey("amount"));
        assertTrue(firstExpense.containsKey("tags"));
        assertTrue(firstExpense.containsKey("tagColors"));
    }

    @Test
    void getBusinessAnalytics_shouldReturnCategoryData() {

        when(userRepository.findByUsername("business")).thenReturn(Optional.of(testUser));
        when(tagRepository.findByUserOrderByNameAsc(testUser)).thenReturn(Arrays.asList(essentialTag));
        when(expenseRepository.findByUser(testUser)).thenReturn(Arrays.asList(expense1, expense2));
        when(expenseTagRepository.countByTag(any(TagEntity.class))).thenReturn(1L);
        when(expenseTagRepository.findExpensesByTag(any(TagEntity.class))).thenReturn(Collections.emptyList());
        when(expenseTagRepository.findTagsByExpense(any(ExpenseEntity.class)))
                .thenReturn(Collections.singletonList(essentialTag));


        Map<String, Object> analytics = businessAnalyticsService.getBusinessAnalytics("business");


        @SuppressWarnings("unchecked")
        List<Map<String, Object>> categoryData = (List<Map<String, Object>>) analytics.get("categoryData");
        assertNotNull(categoryData);
        assertTrue(categoryData.size() > 0);

        Map<String, Object> category = categoryData.get(0);
        assertTrue(category.containsKey("name"));
        assertTrue(category.containsKey("amount"));
        assertTrue(category.containsKey("tags"));
        assertTrue(category.containsKey("tagColors"));
    }

    @Test
    void getBusinessAnalytics_withMultipleExpensesInSameCategory_shouldAggregateTotals() {

        ExpenseEntity expense3 = new ExpenseEntity();
        expense3.setId(3L);
        expense3.setUser(testUser);
        expense3.setName("More Office Supplies");
        expense3.setCategory("Business");
        expense3.setAmount(new BigDecimal("50.00"));
        expense3.setExpenseDate(LocalDate.now());

        when(userRepository.findByUsername("business")).thenReturn(Optional.of(testUser));
        when(tagRepository.findByUserOrderByNameAsc(testUser)).thenReturn(Arrays.asList(essentialTag));
        when(expenseRepository.findByUser(testUser)).thenReturn(Arrays.asList(expense1, expense3));
        when(expenseTagRepository.countByTag(any(TagEntity.class))).thenReturn(2L);
        when(expenseTagRepository.findExpensesByTag(any(TagEntity.class))).thenReturn(Collections.emptyList());
        when(expenseTagRepository.findTagsByExpense(any(ExpenseEntity.class))).thenReturn(Collections.emptyList());


        Map<String, Object> analytics = businessAnalyticsService.getBusinessAnalytics("business");


        @SuppressWarnings("unchecked")
        Map<String, Object> stats = (Map<String, Object>) analytics.get("stats");
        assertEquals("$200.00", stats.get("totalExpenses")); // 150 + 50
    }
}

