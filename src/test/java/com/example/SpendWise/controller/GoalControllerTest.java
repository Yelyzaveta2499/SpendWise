package com.example.SpendWise.controller;

import com.example.SpendWise.model.entity.Goal;
import com.example.SpendWise.service.GoalService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;

import java.time.LocalDate;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class GoalControllerTest {

    private GoalService goalService;
    private GoalController goalController;
    private Authentication authentication;

    @BeforeEach
    void setUp() {
        goalService = Mockito.mock(GoalService.class);
        goalController = new GoalController();
        goalController.goalService = goalService;
        authentication = Mockito.mock(Authentication.class);
        when(authentication.getName()).thenReturn("testUser");
    }



    @Test
    void getAllGoals_authenticatedUser_returnsGoalsList() {
        // Arrange
        List<Goal> goals = Arrays.asList(
                new Goal("Emergency Fund", 10000.0, LocalDate.of(2026, 12, 31), "testUser"),
                new Goal("Vacation", 5000.0, LocalDate.of(2026, 8, 15), "testUser")
        );

        when(goalService.getAllGoalsByUser("testUser")).thenReturn(goals);

        // Act
        ResponseEntity<List<Goal>> response = goalController.getAllGoals(authentication);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(2, response.getBody().size());
        verify(goalService, times(1)).getAllGoalsByUser("testUser");
    }

    @Test
    void getAllGoals_noGoals_returnsEmptyList() {
        // Arrange
        when(goalService.getAllGoalsByUser("testUser")).thenReturn(Collections.emptyList());

        // Act
        ResponseEntity<List<Goal>> response = goalController.getAllGoals(authentication);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertTrue(response.getBody().isEmpty());
    }



    @Test
    void getGoalsSummary_authenticatedUser_returnsCorrectSummary() {
        // Arrange
        when(goalService.getTotalSaved("testUser")).thenReturn(18900.0);
        when(goalService.getTotalTarget("testUser")).thenReturn(40000.0);
        when(goalService.getActiveGoalsCount("testUser")).thenReturn(4);

        // Act
        ResponseEntity<Map<String, Object>> response = goalController.getGoalsSummary(authentication);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(18900.0, response.getBody().get("totalSaved"));
        assertEquals(40000.0, response.getBody().get("totalTarget"));
        assertEquals(4, response.getBody().get("activeGoals"));
    }

    @Test
    void getGoalsSummary_noGoals_returnsZeroValues() {
        // Arrange
        when(goalService.getTotalSaved("testUser")).thenReturn(0.0);
        when(goalService.getTotalTarget("testUser")).thenReturn(0.0);
        when(goalService.getActiveGoalsCount("testUser")).thenReturn(0);

        // Act
        ResponseEntity<Map<String, Object>> response = goalController.getGoalsSummary(authentication);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(0.0, response.getBody().get("totalSaved"));
        assertEquals(0.0, response.getBody().get("totalTarget"));
        assertEquals(0, response.getBody().get("activeGoals"));
    }



    @Test
    void createGoal_validGoal_returnsCreated() {
        // Arrange
        Goal goal = new Goal("New Car", 25000.0, LocalDate.of(2027, 6, 1), null);
        goal.setIcon("🚗");
        goal.setColor("#475569");

        Goal savedGoal = new Goal("New Car", 25000.0, LocalDate.of(2027, 6, 1), "testUser");
        savedGoal.setId(1L);
        savedGoal.setIcon("🚗");
        savedGoal.setColor("#475569");

        when(goalService.createGoal(any(Goal.class))).thenReturn(savedGoal);

        // Act
        ResponseEntity<?> response = goalController.createGoal(goal, authentication);

        // Assert
        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        Goal responseGoal = (Goal) response.getBody();
        assertEquals("New Car", responseGoal.getName());
        assertEquals(25000.0, responseGoal.getTargetAmount());
        assertEquals("testUser", responseGoal.getUserId());
    }

    @Test
    void createGoal_invalidTargetAmount_returnsBadRequest() {
        // Arrange
        Goal goal = new Goal("Invalid Goal", -100.0, null, null);

        when(goalService.createGoal(any(Goal.class)))
                .thenThrow(new IllegalArgumentException("Target amount must be greater than zero"));

        // Act
        ResponseEntity<?> response = goalController.createGoal(goal, authentication);

        // Assert
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        Map<String, String> error = (Map<String, String>) response.getBody();
        assertEquals("Target amount must be greater than zero", error.get("error"));
    }

    @Test
    void createGoal_emptyName_returnsBadRequest() {
        // Arrange
        Goal goal = new Goal("", 5000.0, null, null);

        when(goalService.createGoal(any(Goal.class)))
                .thenThrow(new IllegalArgumentException("Goal name is required"));

        // Act
        ResponseEntity<?> response = goalController.createGoal(goal, authentication);

        // Assert
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }



    @Test
    void updateGoal_validUpdate_returnsUpdatedGoal() {
        // Arrange
        Goal updatedGoal = new Goal();
        updatedGoal.setName("Updated Name");
        updatedGoal.setTargetAmount(15000.0);

        Goal returnedGoal = new Goal("Updated Name", 15000.0, null, "testUser");
        returnedGoal.setId(1L);

        when(goalService.updateGoal(eq(1L), any(Goal.class))).thenReturn(returnedGoal);

        // Act
        ResponseEntity<?> response = goalController.updateGoal(1L, updatedGoal);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        Goal responseGoal = (Goal) response.getBody();
        assertEquals("Updated Name", responseGoal.getName());
        assertEquals(15000.0, responseGoal.getTargetAmount());
    }

    @Test
    void updateGoal_goalNotFound_returnsBadRequest() {
        // Arrange
        Goal updatedGoal = new Goal();
        updatedGoal.setName("Updated");

        when(goalService.updateGoal(eq(999L), any(Goal.class)))
                .thenThrow(new IllegalArgumentException("Goal not found"));

        // Act
        ResponseEntity<?> response = goalController.updateGoal(999L, updatedGoal);

        // Assert
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }



    @Test
    void deleteGoal_existingGoal_returnsNoContent() {
        // Arrange
        doNothing().when(goalService).deleteGoal(1L);

        // Act
        ResponseEntity<Void> response = goalController.deleteGoal(1L);

        // Assert
        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
        verify(goalService, times(1)).deleteGoal(1L);
    }



    @Test
    void getGoalById_existingGoal_returnsGoal() {
        // Arrange
        Goal goal = new Goal("Emergency Fund", 10000.0, null, "testUser");
        goal.setId(1L);

        when(goalService.getGoalById(1L)).thenReturn(Optional.of(goal));

        // Act
        ResponseEntity<Goal> response = goalController.getGoalById(1L);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("Emergency Fund", response.getBody().getName());
    }

    @Test
    void getGoalById_nonExistingGoal_returnsNotFound() {
        // Arrange
        when(goalService.getGoalById(999L)).thenReturn(Optional.empty());

        // Act
        ResponseEntity<Goal> response = goalController.getGoalById(999L);

        // Assert
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }



    @Test
    void addContribution_validContribution_returnsUpdatedGoal() {
        // Arrange
        Map<String, Object> contributionData = new HashMap<>();
        contributionData.put("amount", 500.0);
        contributionData.put("note", "Monthly savings");

        Goal updatedGoal = new Goal("Vacation", 5000.0, null, "testUser");
        updatedGoal.setId(1L);
        updatedGoal.setCurrentAmount(2500.0);

        when(goalService.addContribution(eq(1L), eq(500.0), eq("Monthly savings")))
                .thenReturn(updatedGoal);

        // Act
        ResponseEntity<?> response = goalController.addContribution(1L, contributionData);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        Goal responseGoal = (Goal) response.getBody();
        assertEquals(2500.0, responseGoal.getCurrentAmount());
    }

    @Test
    void addContribution_invalidAmount_returnsBadRequest() {
        // Arrange
        Map<String, Object> contributionData = new HashMap<>();
        contributionData.put("amount", -100.0);
        contributionData.put("note", "Invalid");

        when(goalService.addContribution(eq(1L), eq(-100.0), anyString()))
                .thenThrow(new IllegalArgumentException("Contribution amount must be greater than zero"));

        // Act
        ResponseEntity<?> response = goalController.addContribution(1L, contributionData);

        // Assert
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }

    @Test
    void addContribution_goalNotFound_returnsBadRequest() {
        // Arrange
        Map<String, Object> contributionData = new HashMap<>();
        contributionData.put("amount", 100.0);
        contributionData.put("note", "Test");

        when(goalService.addContribution(eq(999L), anyDouble(), anyString()))
                .thenThrow(new IllegalArgumentException("Goal not found"));

        // Act
        ResponseEntity<?> response = goalController.addContribution(999L, contributionData);

        // Assert
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }
}

