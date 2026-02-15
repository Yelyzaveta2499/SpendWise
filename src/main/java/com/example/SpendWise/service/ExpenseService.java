package com.example.SpendWise.service;

import com.example.SpendWise.model.entity.ExpenseEntity;
import com.example.SpendWise.model.repository.ExpenseRepository;
import com.example.SpendWise.model.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ExpenseService {

    private final ExpenseRepository expenseRepository;
    private final UserRepository userRepository;

    public ExpenseService(ExpenseRepository expenseRepository, UserRepository userRepository) {
        this.expenseRepository = expenseRepository;
        this.userRepository = userRepository;
    }

    // implement in a later subtask this part:
    public List<ExpenseEntity> getExpensesForUser(String username) {
        throw new UnsupportedOperationException("Not implemented yet");
    }

    // Placeholder DTO type; will be defined and implemented later
    public ExpenseEntity createExpenseForUser(String username, Object expenseCreateRequest) {
        throw new UnsupportedOperationException("Not implemented yet");
    }

    public void deleteExpenseForUser(String username, Long expenseId) {
        throw new UnsupportedOperationException("Not implemented yet");
    }
}
