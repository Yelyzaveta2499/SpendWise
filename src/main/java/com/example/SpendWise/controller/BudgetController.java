package com.example.SpendWise.controller;

import com.example.SpendWise.model.entity.BudgetEntity;
import com.example.SpendWise.service.BudgetService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/budgets")
public class BudgetController {

    private final BudgetService budgetService;

    public BudgetController(BudgetService budgetService) {
        this.budgetService = budgetService;
    }

    @GetMapping
    public List<BudgetEntity> listBudgets(Authentication authentication) {
        String username = authentication.getName();
        return budgetService.getBudgetsForUser(username);
    }

    @PostMapping
    public ResponseEntity<BudgetEntity> createBudget(@RequestBody Map<String, Object> body,
                                                     Authentication authentication) {
        String username = authentication.getName();
        BudgetEntity created = budgetService.createBudgetForUser(username, body);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteBudget(@PathVariable("id") Long id,
                                             Authentication authentication) {
        String username = authentication.getName();
        budgetService.deleteBudgetForUser(username, id);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/{id}")
    public ResponseEntity<BudgetEntity> updateBudget(@PathVariable("id") Long id,
                                                     @RequestBody Map<String, Object> body,
                                                     Authentication authentication) {
        String username = authentication.getName();
        BudgetEntity updated = budgetService.updateBudgetForUser(username, id, body);
        return ResponseEntity.ok(updated);
    }

    @ExceptionHandler({IllegalArgumentException.class})
    public ResponseEntity<Map<String, String>> handleBadRequest(IllegalArgumentException ex) {
        Map<String, String> error = new HashMap<>();
        error.put("error", ex.getMessage());
        return ResponseEntity.badRequest().body(error);
    }

    @ExceptionHandler({SecurityException.class})
    public ResponseEntity<Map<String, String>> handleForbidden(SecurityException ex) {
        Map<String, String> error = new HashMap<>();
        error.put("error", ex.getMessage());
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(error);
    }
}
