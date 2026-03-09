package com.example.SpendWise.model.repository;

import com.example.SpendWise.model.entity.ExpenseEntity;
import com.example.SpendWise.model.entity.ExpenseTagEntity;
import com.example.SpendWise.model.entity.TagEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

public interface ExpenseTagRepository extends JpaRepository<ExpenseTagEntity, Long> {

    /**
     * Find all expense-tag associations for a specific expense
     */
    List<ExpenseTagEntity> findByExpense(ExpenseEntity expense);

    /**
     * Find all expense-tag associations for a specific tag
     */
    List<ExpenseTagEntity> findByTag(TagEntity tag);

    /**
     * Find a specific expense-tag association
     */
    Optional<ExpenseTagEntity> findByExpenseAndTag(ExpenseEntity expense, TagEntity tag);

    /**
     * Check if an expense-tag association exists
     */
    boolean existsByExpenseAndTag(ExpenseEntity expense, TagEntity tag);

    /**
     * Delete all associations for a specific expense
     */
    @Modifying
    @Transactional
    void deleteByExpense(ExpenseEntity expense);

    /**
     * Delete all associations for a specific tag
     */
    @Modifying
    @Transactional
    void deleteByTag(TagEntity tag);

    /**
     * Count how many expenses are tagged with a specific tag
     */
    long countByTag(TagEntity tag);

    /**
     * Find all expenses for a specific tag
     */
    @Query("SELECT et.expense FROM ExpenseTagEntity et WHERE et.tag = :tag")
    List<ExpenseEntity> findExpensesByTag(@Param("tag") TagEntity tag);

    /**
     * Find all tags for a specific expense
     */
    @Query("SELECT et.tag FROM ExpenseTagEntity et WHERE et.expense = :expense")
    List<TagEntity> findTagsByExpense(@Param("expense") ExpenseEntity expense);
}

