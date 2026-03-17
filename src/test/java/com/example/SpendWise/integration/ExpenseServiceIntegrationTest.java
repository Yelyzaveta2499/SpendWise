package com.example.SpendWise.integration;

import com.example.SpendWise.model.entity.ExpenseEntity;
import com.example.SpendWise.model.repository.ExpenseRepository;
import com.example.SpendWise.service.ExpenseService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
class ExpenseServiceIntegrationTest {

    @Autowired
    private ExpenseService expenseService;
    @Autowired
    private ExpenseRepository expenseRepository;

    @Test
    void createAndRetrieveExpense() {
        Map<String, Object> payload = new HashMap<>();
        payload.put("name", "Test Expense");
        payload.put("category", "Test Category");
        payload.put("amount", new BigDecimal("42.00"));
        payload.put("date", LocalDate.now().toString());

        ExpenseEntity created = expenseService.createExpenseForUser("indiv", payload);
        assertNotNull(created.getId());
        ExpenseEntity found = expenseRepository.findById(created.getId()).orElse(null);
        assertNotNull(found);
        assertEquals("Test Expense", found.getName());
        assertEquals("Test Category", found.getCategory());
        assertEquals(new BigDecimal("42.00"), found.getAmount());
    }
}

