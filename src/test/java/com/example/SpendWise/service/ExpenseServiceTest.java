package com.example.SpendWise.service;

import com.example.SpendWise.model.entity.ExpenseEntity;
import com.example.SpendWise.model.entity.UserEntity;
import com.example.SpendWise.model.repository.ExpenseRepository;
import com.example.SpendWise.model.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

class ExpenseServiceTest {

    private ExpenseRepository expenseRepository;
    private UserRepository userRepository;
    private ExpenseService expenseService;

    @BeforeEach
    void setUp() {
        expenseRepository = Mockito.mock(ExpenseRepository.class);
        userRepository = Mockito.mock(UserRepository.class);
        expenseService = new ExpenseService(expenseRepository, userRepository);
    }

    @Test
    void getExpensesForUser_returnsList() {
        UserEntity user = new UserEntity();
        user.setId(1);
        user.setUsername("indiv");

        when(userRepository.findByUsername("indiv")).thenReturn(Optional.of(user));
        when(expenseRepository.findTop20ByUserOrderByExpenseDateDesc(user))
                .thenReturn(Collections.emptyList());

        assertNotNull(expenseService.getExpensesForUser("indiv"));
    }

    @Test
    void createExpenseForUser_withValidData_createsExpense() {
        UserEntity user = new UserEntity();
        user.setId(1);
        user.setUsername("indiv");

        when(userRepository.findByUsername("indiv")).thenReturn(Optional.of(user));
        when(expenseRepository.save(any(ExpenseEntity.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        Map<String, Object> payload = new HashMap<>();
        payload.put("name", "Groceries");
        payload.put("category", "Food & Dining");
        payload.put("amount", new BigDecimal("10.50"));
        payload.put("date", LocalDate.now().toString());

        ExpenseEntity created = expenseService.createExpenseForUser("indiv", payload);
        assertEquals("Groceries", created.getName());
        assertEquals("Food & Dining", created.getCategory());
        assertEquals(new BigDecimal("10.50"), created.getAmount());
    }

    @Test
    void deleteExpenseForUser_notOwner_throwsSecurityException() {
        UserEntity owner = new UserEntity();
        owner.setId(1);
        owner.setUsername("owner");

        UserEntity otherUser = new UserEntity();
        otherUser.setId(2);
        otherUser.setUsername("other");

        ExpenseEntity expense = new ExpenseEntity();
        expense.setId(100L);
        expense.setUser(owner);

        when(userRepository.findByUsername("other")).thenReturn(Optional.of(otherUser));
        when(expenseRepository.findById(100L)).thenReturn(Optional.of(expense));

        assertThrows(SecurityException.class, () ->
                expenseService.deleteExpenseForUser("other", 100L));
    }
}

