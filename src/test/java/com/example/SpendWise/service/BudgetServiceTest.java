package com.example.SpendWise.service;

import com.example.SpendWise.model.entity.BudgetEntity;
import com.example.SpendWise.model.entity.UserEntity;
import com.example.SpendWise.model.repository.BudgetRepository;
import com.example.SpendWise.model.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;

class BudgetServiceTest {

    private BudgetRepository budgetRepository;
    private UserRepository userRepository;
    private BudgetService budgetService;

    @BeforeEach
    void setUp() {
        budgetRepository = Mockito.mock(BudgetRepository.class);
        userRepository = Mockito.mock(UserRepository.class);
        budgetService = new BudgetService(budgetRepository, userRepository);
    }

    @Test
    void createBudget_validRequest_savesBudget() {
        // Arrange
        UserEntity user = new UserEntity();
        user.setId(1);
        user.setUsername("indiv");

        when(userRepository.findByUsername("indiv")).thenReturn(Optional.of(user));
        when(budgetRepository.findByUserAndCategoryAndYearAndMonth(user, "Housing", 2026, 2))
                .thenReturn(Optional.empty());
        when(budgetRepository.save(any(BudgetEntity.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        Map<String, Object> payload = new HashMap<>();
        payload.put("category", "Housing");
        payload.put("amount", new BigDecimal("1200"));
        payload.put("month", 2);
        payload.put("year", 2026);

        // Act
        BudgetEntity created = budgetService.createBudgetForUser("indiv", payload);

        // Assert
        assertEquals("Housing", created.getCategory());
        assertEquals(new BigDecimal("1200"), created.getAmount());
        assertEquals(2, created.getMonth());
        assertEquals(2026, created.getYear());
        assertEquals(user, created.getUser());
    }

    @Test
    void createBudget_amountZero_throwsBadRequest() {
        // Arrange
        UserEntity user = new UserEntity();
        user.setId(1);
        user.setUsername("indiv");

        when(userRepository.findByUsername("indiv")).thenReturn(Optional.of(user));

        Map<String, Object> payload = new HashMap<>();
        payload.put("category", "Housing");
        payload.put("amount", new BigDecimal("0"));
        payload.put("month", 2);
        payload.put("year", 2026);

        // Act + Assert
        assertThrows(IllegalArgumentException.class,
                () -> budgetService.createBudgetForUser("indiv", payload));
    }

    @Test
    void createBudget_duplicateSameMonth_throwsBadRequest() {
        // Arrange
        UserEntity user = new UserEntity();
        user.setId(1);
        user.setUsername("indiv");

        when(userRepository.findByUsername("indiv")).thenReturn(Optional.of(user));
        when(budgetRepository.findByUserAndCategoryAndYearAndMonth(user, "Housing", 2026, 2))
                .thenReturn(Optional.of(new BudgetEntity()));

        Map<String, Object> payload = new HashMap<>();
        payload.put("category", "Housing");
        payload.put("amount", new BigDecimal("1200"));
        payload.put("month", 2);
        payload.put("year", 2026);

        // Act + Assert
        assertThrows(IllegalArgumentException.class,
                () -> budgetService.createBudgetForUser("indiv", payload));
    }

    @Test
    void updateBudget_notOwner_throwsForbidden() {
        // Arrange
        UserEntity loggedInUser = new UserEntity();
        loggedInUser.setId(2);
        loggedInUser.setUsername("indiv");

        UserEntity owner = new UserEntity();
        owner.setId(1);
        owner.setUsername("someoneElse");

        BudgetEntity budget = new BudgetEntity();
        budget.setId(100L);
        budget.setUser(owner);
        budget.setCategory("Housing");
        budget.setAmount(new BigDecimal("1000"));
        budget.setMonth(2);
        budget.setYear(2026);

        when(userRepository.findByUsername("indiv")).thenReturn(Optional.of(loggedInUser));
        when(budgetRepository.findById(100L)).thenReturn(Optional.of(budget));

        Map<String, Object> update = new HashMap<>();
        update.put("amount", 1200);

        // Act + Assert
        assertThrows(SecurityException.class,
                () -> budgetService.updateBudgetForUser("indiv", 100L, update));
    }

    @Test
    void deleteBudget_notOwner_throwsForbidden() {
        // Arrange
        UserEntity loggedInUser = new UserEntity();
        loggedInUser.setId(2);
        loggedInUser.setUsername("indiv");

        UserEntity owner = new UserEntity();
        owner.setId(1);
        owner.setUsername("someoneElse");

        BudgetEntity budget = new BudgetEntity();
        budget.setId(100L);
        budget.setUser(owner);

        when(userRepository.findByUsername("indiv")).thenReturn(Optional.of(loggedInUser));
        when(budgetRepository.findById(anyLong())).thenReturn(Optional.of(budget));

        // Act + Assert
        assertThrows(SecurityException.class,
                () -> budgetService.deleteBudgetForUser("indiv", 100L));
    }

    @Test
    void updateBudget_owner_updatesAmountSuccessfully() {
        // Arrange
        UserEntity user = new UserEntity();
        user.setId(1);
        user.setUsername("indiv");

        BudgetEntity budget = new BudgetEntity();
        budget.setId(100L);
        budget.setUser(user);
        budget.setCategory("Housing");
        budget.setAmount(new BigDecimal("1000"));
        budget.setMonth(2);
        budget.setYear(2026);

        when(userRepository.findByUsername("indiv")).thenReturn(Optional.of(user));
        when(budgetRepository.findById(100L)).thenReturn(Optional.of(budget));
        when(budgetRepository.findByUserAndCategoryAndYearAndMonth(user, "Housing", 2026, 2))
                .thenReturn(Optional.of(budget));
        when(budgetRepository.save(any(BudgetEntity.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        Map<String, Object> update = new HashMap<>();
        update.put("amount", 1200.0);

        // Act
        BudgetEntity updated = budgetService.updateBudgetForUser("indiv", 100L, update);

        // Assert
        assertEquals(new BigDecimal("1200.0"), updated.getAmount());
    }
}
