package com.example.SpendWise.unit.controller;

import com.example.SpendWise.controller.ExpenseController;
import com.example.SpendWise.service.ExpenseService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.Authentication;

import java.util.*;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

class ExpenseControllerUnitTest {

    private ExpenseService expenseService;
    private ExpenseController expenseController;
    private Authentication authentication;

    @BeforeEach
    void setUp() {
        expenseService = mock(ExpenseService.class);
        expenseController = new ExpenseController(expenseService);
        authentication = mock(Authentication.class);
        when(authentication.getName()).thenReturn("alice");
    }

    @Test
    void listExpenses_withTagNameFilter_returnsOnlyMatchingExpenses() {
        Map<String, Object> dto1 = new HashMap<>();
        dto1.put("id", 1L);
        dto1.put("tags", Arrays.asList("Food", "Health"));

        Map<String, Object> dto2 = new HashMap<>();
        dto2.put("id", 2L);
        dto2.put("tags", Collections.singletonList("Travel"));

        when(expenseService.getExpenseDtosForUser("alice")).thenReturn(Arrays.asList(dto1, dto2));

        List<Map<String, Object>> result = expenseController.listExpenses(null, "Food", authentication);

        assertEquals(1, result.size());
        assertEquals(1L, result.get(0).get("id"));
    }

    @Test
    void listExpenses_withoutFilters_returnsAllExpenses() {
        Map<String, Object> dto1 = new HashMap<>();
        dto1.put("id", 1L);
        Map<String, Object> dto2 = new HashMap<>();
        dto2.put("id", 2L);

        when(expenseService.getExpenseDtosForUser("alice")).thenReturn(Arrays.asList(dto1, dto2));

        List<Map<String, Object>> result = expenseController.listExpenses(null, null, authentication);

        assertEquals(2, result.size());
    }
}

