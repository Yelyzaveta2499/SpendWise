package com.example.SpendWise.controller;

import com.example.SpendWise.model.entity.ExpenseEntity;
import com.example.SpendWise.service.ExpenseService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/expenses")
public class ExpenseController {

    private final ExpenseService expenseService;

    public ExpenseController(ExpenseService expenseService) {
        this.expenseService = expenseService;
    }

    // Listing expenses for authenticated user
    @GetMapping
    public List<ExpenseEntity> listExpenses(Authentication authentication) {
        String username = authentication.getName();
        return expenseService.getExpensesForUser(username);
    }
    // Creating expense for authenticated user
    @PostMapping
    public ResponseEntity<ExpenseEntity> createExpense(@RequestBody Map<String, Object> body,
                                                       Authentication authentication) {
        String username = authentication.getName();
        ExpenseEntity created = expenseService.createExpenseForUser(username, body);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }
    // Deleting expense if owned by authenticated user
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteExpense(@PathVariable("id") Long id,
                                              Authentication authentication) {
        String username = authentication.getName();
        expenseService.deleteExpenseForUser(username, id);
        return ResponseEntity.noContent().build();
    }
    // Mapping invalid arguments (e.g., user/expense not found, bad amount) to 400
    @ExceptionHandler({IllegalArgumentException.class})
    public ResponseEntity<Map<String, String>> handleBadRequest(IllegalArgumentException ex) {
        Map<String, String> error = new HashMap<>();
        error.put("error", ex.getMessage());
        return ResponseEntity.badRequest().body(error);
    }
    // Map ownership violations to 403
    @ExceptionHandler({SecurityException.class})
    public ResponseEntity<Map<String, String>> handleForbidden(SecurityException ex) {
        Map<String, String> error = new HashMap<>();
        error.put("error", ex.getMessage());
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(error);
    }
}

