package com.example.SpendWise.service;

import com.example.SpendWise.model.Contribution;
import com.example.SpendWise.model.Goal;
import com.example.SpendWise.repository.ContributionRepository;
import com.example.SpendWise.repository.GoalRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class GoalServiceTest {

    private GoalRepository goalRepository;
    private ContributionRepository contributionRepository;
    private GoalService goalService;

    @BeforeEach
    void setUp() {
        goalRepository = Mockito.mock(GoalRepository.class);
        contributionRepository = Mockito.mock(ContributionRepository.class);
        goalService = new GoalService();
        goalService.goalRepository = goalRepository;
        goalService.contributionRepository = contributionRepository;
    }



    @Test
    void createGoal_validGoal_savesSuccessfully() {
        // Arrange
        Goal goal = new Goal("Emergency Fund", 10000.0, LocalDate.of(2026, 12, 31), "user123");
        goal.setIcon("🛡️");
        goal.setColor("#10b981");

        when(goalRepository.save(any(Goal.class))).thenAnswer(invocation -> {
            Goal savedGoal = invocation.getArgument(0);
            savedGoal.setId(1L);
            return savedGoal;
        });

        // Act
        Goal created = goalService.createGoal(goal);

        // Assert
        assertNotNull(created);
        assertEquals("Emergency Fund", created.getName());
        assertEquals(10000.0, created.getTargetAmount());
        assertEquals(0.0, created.getCurrentAmount());
        assertEquals("user123", created.getUserId());
        verify(goalRepository, times(1)).save(goal);
    }

    @Test
    void createGoal_zeroTargetAmount_throwsException() {
        // Arrange
        Goal goal = new Goal("Invalid Goal", 0.0, null, "user123");

        // Act & Assert
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> goalService.createGoal(goal)
        );
        assertEquals("Target amount must be greater than zero", exception.getMessage());
        verify(goalRepository, never()).save(any());
    }

    @Test
    void createGoal_negativeTargetAmount_throwsException() {
        // Arrange
        Goal goal = new Goal("Invalid Goal", -100.0, null, "user123");

        // Act & Assert
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> goalService.createGoal(goal)
        );
        assertEquals("Target amount must be greater than zero", exception.getMessage());
    }

    @Test
    void createGoal_nullName_throwsException() {
        // Arrange
        Goal goal = new Goal(null, 5000.0, null, "user123");

        // Act & Assert
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> goalService.createGoal(goal)
        );
        assertEquals("Goal name is required", exception.getMessage());
    }

    @Test
    void createGoal_emptyName_throwsException() {
        // Arrange
        Goal goal = new Goal("   ", 5000.0, null, "user123");

        // Act & Assert
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> goalService.createGoal(goal)
        );
        assertEquals("Goal name is required", exception.getMessage());
    }



    @Test
    void addContribution_validAmount_updatesGoalProgress() {
        // Arrange
        Goal goal = new Goal("Vacation Fund", 5000.0, LocalDate.of(2026, 8, 1), "user123");
        goal.setId(1L);
        goal.setCurrentAmount(2000.0);

        when(goalRepository.findById(1L)).thenReturn(Optional.of(goal));
        when(contributionRepository.save(any(Contribution.class))).thenReturn(null);
        when(goalRepository.save(any(Goal.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        Goal updated = goalService.addContribution(1L, 500.0, "Monthly savings");

        // Assert
        assertEquals(2500.0, updated.getCurrentAmount());
        verify(contributionRepository, times(1)).save(any(Contribution.class));
        verify(goalRepository, times(1)).save(goal);
    }

    @Test
    void addContribution_multipleContributions_accumulatesCorrectly() {
        // Arrange
        Goal goal = new Goal("New Car", 25000.0, LocalDate.of(2027, 6, 1), "user123");
        goal.setId(2L);
        goal.setCurrentAmount(5000.0);

        when(goalRepository.findById(2L)).thenReturn(Optional.of(goal));
        when(contributionRepository.save(any(Contribution.class))).thenReturn(null);
        when(goalRepository.save(any(Goal.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Act - Add multiple contributions
        goalService.addContribution(2L, 1000.0, "First contribution");
        Goal afterFirst = goalService.addContribution(2L, 2000.0, "Second contribution");
        Goal afterSecond = goalService.addContribution(2L, 1500.0, "Third contribution");

        // Assert
        assertEquals(9500.0, afterSecond.getCurrentAmount()); // 5000 + 1000 + 2000 + 1500
    }

    @Test
    void addContribution_zeroAmount_throwsException() {
        // Arrange
        when(goalRepository.findById(1L)).thenReturn(Optional.of(new Goal()));

        // Act & Assert
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> goalService.addContribution(1L, 0.0, "Invalid")
        );
        assertEquals("Contribution amount must be greater than zero", exception.getMessage());
    }

    @Test
    void addContribution_negativeAmount_throwsException() {
        // Act & Assert
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> goalService.addContribution(1L, -100.0, "Invalid")
        );
        assertEquals("Contribution amount must be greater than zero", exception.getMessage());
    }

    @Test
    void addContribution_goalNotFound_throwsException() {
        // Arrange
        when(goalRepository.findById(999L)).thenReturn(Optional.empty());

        // Act & Assert
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> goalService.addContribution(999L, 100.0, "Test")
        );
        assertEquals("Goal not found", exception.getMessage());
    }



    @Test
    void goalProgress_0Percent_whenNoContributions() {
        // Arrange
        Goal goal = new Goal("Emergency Fund", 10000.0, null, "user123");
        goal.setCurrentAmount(0.0);

        // Act
        double percentage = (goal.getCurrentAmount() / goal.getTargetAmount()) * 100;

        // Assert
        assertEquals(0.0, percentage);
    }

    @Test
    void goalProgress_50Percent_whenHalfwayToTarget() {
        // Arrange
        Goal goal = new Goal("Vacation Fund", 5000.0, null, "user123");
        goal.setCurrentAmount(2500.0);

        // Act
        double percentage = (goal.getCurrentAmount() / goal.getTargetAmount()) * 100;

        // Assert
        assertEquals(50.0, percentage);
    }

    @Test
    void goalProgress_100Percent_whenTargetReached() {
        // Arrange
        Goal goal = new Goal("New Laptop", 1500.0, null, "user123");
        goal.setCurrentAmount(1500.0);

        // Act
        double percentage = (goal.getCurrentAmount() / goal.getTargetAmount()) * 100;

        // Assert
        assertEquals(100.0, percentage);
    }

    @Test
    void goalProgress_over100Percent_whenExceedsTarget() {
        // Arrange
        Goal goal = new Goal("Home Down Payment", 50000.0, null, "user123");
        goal.setCurrentAmount(55000.0);

        // Act
        double percentage = (goal.getCurrentAmount() / goal.getTargetAmount()) * 100;

        // Assert
        assertEquals(110.0, percentage, 0.01);
    }

    @Test
    void goalProgress_remainingAmount_calculatesCorrectly() {
        // Arrange
        Goal goal = new Goal("Wedding Fund", 15000.0, null, "user123");
        goal.setCurrentAmount(12000.0);

        // Act
        double remaining = goal.getTargetAmount() - goal.getCurrentAmount();

        // Assert
        assertEquals(3000.0, remaining);
    }



    @Test
    void getTotalSaved_multipleGoals_sumsCorrectly() {
        // Arrange
        Goal goal1 = new Goal("Goal 1", 10000.0, null, "user123");
        goal1.setCurrentAmount(7500.0);

        Goal goal2 = new Goal("Goal 2", 5000.0, null, "user123");
        goal2.setCurrentAmount(3200.0);

        Goal goal3 = new Goal("Goal 3", 25000.0, null, "user123");
        goal3.setCurrentAmount(8200.0);

        List<Goal> goals = Arrays.asList(goal1, goal2, goal3);
        when(goalRepository.findByUserId("user123")).thenReturn(goals);

        // Act
        Double totalSaved = goalService.getTotalSaved("user123");

        // Assert
        assertEquals(18900.0, totalSaved); // 7500 + 3200 + 8200
    }

    @Test
    void getTotalTarget_multipleGoals_sumsCorrectly() {
        // Arrange
        Goal goal1 = new Goal("Goal 1", 10000.0, null, "user123");
        Goal goal2 = new Goal("Goal 2", 5000.0, null, "user123");
        Goal goal3 = new Goal("Goal 3", 25000.0, null, "user123");

        List<Goal> goals = Arrays.asList(goal1, goal2, goal3);
        when(goalRepository.findByUserId("user123")).thenReturn(goals);

        // Act
        Double totalTarget = goalService.getTotalTarget("user123");

        // Assert
        assertEquals(40000.0, totalTarget); // 10000 + 5000 + 25000
    }

    @Test
    void getActiveGoalsCount_returnsCorrectCount() {
        // Arrange
        List<Goal> goals = Arrays.asList(
                new Goal("Goal 1", 10000.0, null, "user123"),
                new Goal("Goal 2", 5000.0, null, "user123"),
                new Goal("Goal 3", 25000.0, null, "user123"),
                new Goal("Goal 4", 50000.0, null, "user123")
        );
        when(goalRepository.findByUserId("user123")).thenReturn(goals);

        // Act
        Integer count = goalService.getActiveGoalsCount("user123");

        // Assert
        assertEquals(4, count);
    }

    @Test
    void getTotalSaved_noGoals_returnsZero() {
        // Arrange
        when(goalRepository.findByUserId("user123")).thenReturn(Arrays.asList());

        // Act
        Double totalSaved = goalService.getTotalSaved("user123");

        // Assert
        assertEquals(0.0, totalSaved);
    }



    @Test
    void updateGoal_validChanges_updatesSuccessfully() {
        // Arrange
        Goal existingGoal = new Goal("Old Name", 10000.0, LocalDate.of(2026, 12, 31), "user123");
        existingGoal.setId(1L);

        Goal updatedGoal = new Goal();
        updatedGoal.setName("New Name");
        updatedGoal.setTargetAmount(15000.0);
        updatedGoal.setDeadline(LocalDate.of(2027, 6, 30));

        when(goalRepository.findById(1L)).thenReturn(Optional.of(existingGoal));
        when(goalRepository.save(any(Goal.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        Goal result = goalService.updateGoal(1L, updatedGoal);

        // Assert
        assertEquals("New Name", result.getName());
        assertEquals(15000.0, result.getTargetAmount());
        assertEquals(LocalDate.of(2027, 6, 30), result.getDeadline());
    }

    @Test
    void updateGoal_goalNotFound_throwsException() {
        // Arrange
        when(goalRepository.findById(999L)).thenReturn(Optional.empty());

        // Act & Assert
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> goalService.updateGoal(999L, new Goal())
        );
        assertEquals("Goal not found", exception.getMessage());
    }



    @Test
    void deleteGoal_existingGoal_deletesSuccessfully() {
        // Arrange
        doNothing().when(goalRepository).deleteById(1L);

        // Act
        goalService.deleteGoal(1L);

        // Assert
        verify(goalRepository, times(1)).deleteById(1L);
    }



    @Test
    void goalProgress_verySmallContribution_calculatesCorrectly() {
        // Arrange
        Goal goal = new Goal("Long-term Savings", 100000.0, null, "user123");
        goal.setCurrentAmount(0.01);

        // Act
        double percentage = (goal.getCurrentAmount() / goal.getTargetAmount()) * 100;

        // Assert
        // 0.01 / 100000 * 100 = 0.00001 (or 1E-5)
        assertEquals(0.00001, percentage, 0.000001);
    }

    @Test
    void goalProgress_largeNumbers_handlesCorrectly() {
        // Arrange
        Goal goal = new Goal("Retirement Fund", 1000000.0, null, "user123");
        goal.setCurrentAmount(750000.0);

        // Act
        double percentage = (goal.getCurrentAmount() / goal.getTargetAmount()) * 100;

        // Assert
        assertEquals(75.0, percentage);
    }

    @Test
    void addContribution_withNote_savesCorrectly() {
        // Arrange
        Goal goal = new Goal("Vacation", 5000.0, null, "user123");
        goal.setId(1L);
        goal.setCurrentAmount(1000.0);

        when(goalRepository.findById(1L)).thenReturn(Optional.of(goal));
        when(contributionRepository.save(any(Contribution.class))).thenReturn(null);
        when(goalRepository.save(any(Goal.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        Goal updated = goalService.addContribution(1L, 500.0, "Birthday money");

        // Assert
        assertEquals(1500.0, updated.getCurrentAmount());
        verify(contributionRepository, times(1)).save(any(Contribution.class));
    }
}

