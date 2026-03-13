package com.example.SpendWise.model.repository;

import com.example.SpendWise.model.entity.TagEntity;
import com.example.SpendWise.model.entity.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface TagRepository extends JpaRepository<TagEntity, Long> {

    /**
     * Find all tags for a specific user
     */
    List<TagEntity> findByUser(UserEntity user);

    /**
     * Find all tags for a specific user, ordered by name
     */
    List<TagEntity> findByUserOrderByNameAsc(UserEntity user);

    /**
     * Find a tag by name for a specific user
     */
    Optional<TagEntity> findByUserAndName(UserEntity user, String name);

    /**
     * Check if a tag with the given name exists for a user
     */
    boolean existsByUserAndName(UserEntity user, String name);

    /**
     * Count tags for a given user
     */
    long countByUser(UserEntity user);

    /**
     * Find tags by user and name containing (case-insensitive search)
     */
    List<TagEntity> findByUserAndNameContainingIgnoreCase(UserEntity user, String namePart);

    /**
     * Find all tags used by a specific expense (through the join table)
     */
    @Query("SELECT t FROM TagEntity t JOIN t.expenseTags et WHERE et.expense.id = :expenseId")
    List<TagEntity> findTagsByExpenseId(@Param("expenseId") Long expenseId);

    /**
     * Find most used tags by user (tags with most expense associations)
     */
    @Query("SELECT t FROM TagEntity t " +
           "LEFT JOIN t.expenseTags et " +
           "WHERE t.user = :user " +
           "GROUP BY t.id " +
           "ORDER BY COUNT(et.id) DESC")
    List<TagEntity> findMostUsedTagsByUser(@Param("user") UserEntity user);
}

