package com.example.SpendWise.controller;

import com.example.SpendWise.model.entity.ExpenseEntity;
import com.example.SpendWise.model.entity.TagEntity;
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
    public List<Map<String, Object>> listExpenses(
            @RequestParam(value = "tagId", required = false) Long tagId,
            @RequestParam(value = "tagName", required = false) String tagName,
            Authentication authentication) {
        String username = authentication.getName();

        if (tagId != null) {
            return expenseService.getExpenseDtosForUser(username).stream()
                    .filter(dto -> {
                        @SuppressWarnings("unchecked")
                        List<String> tags = (List<String>) dto.get("tags");
                        return tags != null && !tags.isEmpty();
                    })
                    .toList();
        } else if (tagName != null && !tagName.isBlank()) {
            return expenseService.getExpenseDtosForUser(username).stream()
                    .filter(dto -> {
                        @SuppressWarnings("unchecked")
                        List<String> tags = (List<String>) dto.get("tags");
                        return tags != null && tags.contains(tagName);
                    })
                    .toList();
        }

        return expenseService.getExpenseDtosForUser(username);
    }

    // Get tags for a specific expense
    @GetMapping("/{id}/tags")
    public ResponseEntity<List<TagEntity>> getExpenseTags(
            @PathVariable("id") Long expenseId,
            Authentication authentication) {
        String username = authentication.getName();
        List<TagEntity> tags = expenseService.getTagsForExpense(username, expenseId);
        return ResponseEntity.ok(tags);
    }

    // Add a tag to an expense
    @PostMapping("/{id}/tags/{tagId}")
    public ResponseEntity<ExpenseEntity> addTagToExpense(
            @PathVariable("id") Long expenseId,
            @PathVariable("tagId") Long tagId,
            Authentication authentication) {
        String username = authentication.getName();
        ExpenseEntity updated = expenseService.addTagToExpense(username, expenseId, tagId);
        return ResponseEntity.ok(updated);
    }

    // Remove a tag from an expense
    @DeleteMapping("/{id}/tags/{tagId}")
    public ResponseEntity<ExpenseEntity> removeTagFromExpense(
            @PathVariable("id") Long expenseId,
            @PathVariable("tagId") Long tagId,
            Authentication authentication) {
        String username = authentication.getName();
        ExpenseEntity updated = expenseService.removeTagFromExpense(username, expenseId, tagId);
        return ResponseEntity.ok(updated);
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
    // Updating expense if owned by authenticated user
    @PutMapping("/{id}")
    public ResponseEntity<ExpenseEntity> updateExpense(@PathVariable("id") Long id,
                                                       @RequestBody Map<String, Object> body,
                                                       Authentication authentication) {
        String username = authentication.getName();
        ExpenseEntity updated = expenseService.updateExpenseForUser(username, id, body);
        return ResponseEntity.ok(updated);
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
