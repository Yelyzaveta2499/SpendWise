package com.example.SpendWise.controller;

import com.example.SpendWise.model.Contribution;
import com.example.SpendWise.model.Goal;
import com.example.SpendWise.service.GoalService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/goals")
public class GoalController {

    @Autowired
    GoalService goalService;

    // Get all goals for the authenticated user
    @GetMapping
    public ResponseEntity<List<Goal>> getAllGoals(Authentication authentication) {
        String userId = authentication.getName();
        List<Goal> goals = goalService.getAllGoalsByUser(userId);
        return ResponseEntity.ok(goals);
    }

    // Get summary statistics for goals
    @GetMapping("/summary")
    public ResponseEntity<Map<String, Object>> getGoalsSummary(Authentication authentication) {
        String userId = authentication.getName();

        Map<String, Object> summary = new HashMap<>();
        summary.put("totalSaved", goalService.getTotalSaved(userId));
        summary.put("totalTarget", goalService.getTotalTarget(userId));
        summary.put("activeGoals", goalService.getActiveGoalsCount(userId));

        return ResponseEntity.ok(summary);
    }

    // Get a single goal by ID
    @GetMapping("/{id}")
    public ResponseEntity<Goal> getGoalById(@PathVariable Long id) {
        Optional<Goal> goal = goalService.getGoalById(id);

        if (goal.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.ok(goal.get());
    }

    // Create a new goal
    @PostMapping
    public ResponseEntity<?> createGoal(@RequestBody Goal goal, Authentication authentication) {
        try {
            String userId = authentication.getName();
            goal.setUserId(userId);

            Goal createdGoal = goalService.createGoal(goal);
            return ResponseEntity.status(HttpStatus.CREATED).body(createdGoal);
        } catch (IllegalArgumentException e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }

    // Update a goal
    @PutMapping("/{id}")
    public ResponseEntity<?> updateGoal(@PathVariable Long id, @RequestBody Goal goal) {
        try {
            Goal updatedGoal = goalService.updateGoal(id, goal);
            return ResponseEntity.ok(updatedGoal);
        } catch (IllegalArgumentException e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }

    // Delete a goal
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteGoal(@PathVariable Long id) {
        goalService.deleteGoal(id);
        return ResponseEntity.noContent().build();
    }

    // Add contribution to a goal
    @PostMapping("/{id}/contributions")
    public ResponseEntity<?> addContribution(
            @PathVariable Long id,
            @RequestBody Map<String, Object> contributionData) {
        try {
            Double amount = Double.parseDouble(contributionData.get("amount").toString());
            String note = contributionData.get("note") != null ?
                    contributionData.get("note").toString() : null;

            Goal updatedGoal = goalService.addContribution(id, amount, note);
            return ResponseEntity.ok(updatedGoal);
        } catch (IllegalArgumentException e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Invalid request data");
            return ResponseEntity.badRequest().body(error);
        }
    }

    // Get all contributions for a goal
    @GetMapping("/{id}/contributions")
    public ResponseEntity<List<Contribution>> getContributions(@PathVariable Long id) {
        List<Contribution> contributions = goalService.getContributionsByGoal(id);
        return ResponseEntity.ok(contributions);
    }
}


