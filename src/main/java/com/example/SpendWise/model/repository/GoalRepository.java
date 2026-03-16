package com.example.SpendWise.model.repository;

import com.example.SpendWise.model.entity.Goal;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface GoalRepository extends JpaRepository<Goal, Long> {

    // Find all goals for a specific user
    List<Goal> findByUserId(String userId);

    // Find goals by user and check if deadline has passed
    List<Goal> findByUserIdOrderByCreatedDateDesc(String userId);
}

