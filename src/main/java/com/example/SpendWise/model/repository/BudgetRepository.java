package com.example.SpendWise.model.repository;

import com.example.SpendWise.model.entity.BudgetEntity;
import com.example.SpendWise.model.entity.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface BudgetRepository extends JpaRepository<BudgetEntity, Long> {

    List<BudgetEntity> findByUserOrderByYearDescMonthDescCategoryAsc(UserEntity user);

    Optional<BudgetEntity> findByUserAndCategoryAndYearAndMonth(UserEntity user, String category, Integer year, Integer month);
}

