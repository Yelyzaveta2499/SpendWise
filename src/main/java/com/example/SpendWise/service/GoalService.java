package com.example.SpendWise.service;

import com.example.SpendWise.model.entity.Contribution;
import com.example.SpendWise.model.entity.Goal;
import com.example.SpendWise.model.repository.ContributionRepository;
import com.example.SpendWise.model.repository.GoalRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class GoalService {

    @Autowired
    public GoalRepository goalRepository;

    @Autowired
    public ContributionRepository contributionRepository;

    // Create a new goal
    public Goal createGoal(Goal goal) {
        // Validate target amount
        if (goal.getTargetAmount() == null || goal.getTargetAmount() <= 0) {
            throw new IllegalArgumentException("Target amount must be greater than zero");
        }

        // Validate name
        if (goal.getName() == null || goal.getName().trim().isEmpty()) {
            throw new IllegalArgumentException("Goal name is required");
        }

        return goalRepository.save(goal);
    }

    // Get all goals for a user
    public List<Goal> getAllGoalsByUser(String userId) {
        return goalRepository.findByUserIdOrderByCreatedDateDesc(userId);
    }

    // Get a single goal by ID
    public Optional<Goal> getGoalById(Long id) {
        return goalRepository.findById(id);
    }

    // Update a goal
    public Goal updateGoal(Long id, Goal updatedGoal) {
        Optional<Goal> existingGoal = goalRepository.findById(id);

        if (existingGoal.isEmpty()) {
            throw new IllegalArgumentException("Goal not found");
        }

        Goal goal = existingGoal.get();

        if (updatedGoal.getName() != null) {
            goal.setName(updatedGoal.getName());
        }

        if (updatedGoal.getTargetAmount() != null && updatedGoal.getTargetAmount() > 0) {
            goal.setTargetAmount(updatedGoal.getTargetAmount());
        }

        if (updatedGoal.getDeadline() != null) {
            goal.setDeadline(updatedGoal.getDeadline());
        }

        if (updatedGoal.getIcon() != null) {
            goal.setIcon(updatedGoal.getIcon());
        }

        if (updatedGoal.getColor() != null) {
            goal.setColor(updatedGoal.getColor());
        }

        return goalRepository.save(goal);
    }

    // Delete a goal
    public void deleteGoal(Long id) {
        goalRepository.deleteById(id);
    }

    // Add contribution to a goal
    public Goal addContribution(Long goalId, Double amount, String note) {
        // Validate amount
        if (amount == null || amount <= 0) {
            throw new IllegalArgumentException("Contribution amount must be greater than zero");
        }

        Optional<Goal> optionalGoal = goalRepository.findById(goalId);

        if (optionalGoal.isEmpty()) {
            throw new IllegalArgumentException("Goal not found");
        }

        Goal goal = optionalGoal.get();

        // Create and save contribution
        Contribution contribution = new Contribution(goal, amount, note);
        contributionRepository.save(contribution);

        // Update goal's current amount
        goal.setCurrentAmount(goal.getCurrentAmount() + amount);

        return goalRepository.save(goal);
    }

    // Get all contributions for a goal
    public List<Contribution> getContributionsByGoal(Long goalId) {
        return contributionRepository.findByGoalIdOrderByContributionDateDesc(goalId);
    }

    // Calculate total saved across all goals for a user
    public Double getTotalSaved(String userId) {
        List<Goal> goals = goalRepository.findByUserId(userId);
        return goals.stream()
                .mapToDouble(Goal::getCurrentAmount)
                .sum();
    }

    // Calculate total target across all goals for a user
    public Double getTotalTarget(String userId) {
        List<Goal> goals = goalRepository.findByUserId(userId);
        return goals.stream()
                .mapToDouble(Goal::getTargetAmount)
                .sum();
    }

    // Get count of active goals for a user
    public Integer getActiveGoalsCount(String userId) {
        List<Goal> goals = goalRepository.findByUserId(userId);
        return goals.size();
    }
}

