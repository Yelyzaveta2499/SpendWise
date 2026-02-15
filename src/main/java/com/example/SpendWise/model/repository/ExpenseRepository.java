package com.example.SpendWise.model.repository;

import com.example.SpendWise.model.entity.ExpenseEntity;
import com.example.SpendWise.model.entity.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;

public interface ExpenseRepository extends JpaRepository<ExpenseEntity, Long> {

    List<ExpenseEntity> findByUser(UserEntity user);

    List<ExpenseEntity> findByUserAndExpenseDateBetween(
        UserEntity user,
        LocalDate start,
        LocalDate end
    );

    List<ExpenseEntity> findTop20ByUserOrderByExpenseDateDesc(UserEntity user);
}
