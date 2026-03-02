package com.example.SpendWise.repository;

import com.example.SpendWise.model.Contribution;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ContributionRepository extends JpaRepository<Contribution, Long> {

    // Find all contributions for a specific goal
    List<Contribution> findByGoalIdOrderByContributionDateDesc(Long goalId);

    // Find contributions by goal
    List<Contribution> findByGoalId(Long goalId);
}

