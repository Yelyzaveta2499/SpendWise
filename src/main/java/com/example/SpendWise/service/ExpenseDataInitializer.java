package com.example.SpendWise.service;

import com.example.SpendWise.model.entity.ExpenseEntity;
import com.example.SpendWise.model.entity.UserEntity;
import com.example.SpendWise.model.repository.ExpenseRepository;
import com.example.SpendWise.model.repository.UserRepository;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Seeds a simple set of zero-amount placeholder expenses for user "indiv"
 * the first time the application starts with an empty expenses table
 * for that user. If the user already has any expenses, nothing is added.
 */
@Component
public class ExpenseDataInitializer {

    private static final Logger logger = LoggerFactory.getLogger(ExpenseDataInitializer.class);

    private final UserRepository userRepository;
    private final ExpenseRepository expenseRepository;

    public ExpenseDataInitializer(UserRepository userRepository, ExpenseRepository expenseRepository) {
        this.userRepository = userRepository;
        this.expenseRepository = expenseRepository;
    }

    @PostConstruct
    public void seedInitialExpenses() {
        // Only seed for the indiv user for now
        UserEntity indivUser = userRepository.findByUsername("indiv").orElse(null);
        if (indivUser == null) {
            logger.info("User 'indiv' not found - skipping expense seeding");
            return; // no user -> nothing to seed
        }

        // Seed ONLY when the user has no real expenses (amount > 0)
        long realCount = expenseRepository.findTop20ByUserOrderByExpenseDateDesc(indivUser)
                .stream()
                .filter(e -> e.getAmount() != null && e.getAmount().compareTo(BigDecimal.ZERO) > 0)
                .count();

        if (realCount > 0) {
            logger.info("User 'indiv' already has {} real expenses - skipping seeding", realCount);
            return;
        }

        // Also avoid adding duplicates if placeholders already exist
        long existingCount = expenseRepository.countByUser(indivUser);
        if (existingCount > 0) {
            logger.info("User 'indiv' already has {} expenses - skipping seeding to prevent duplicates", existingCount);
            return;
        }

        logger.info("Seeding initial placeholder expenses for user 'indiv'...");
        LocalDate today = LocalDate.now();

        savePlaceholder(indivUser, "Grocery Store", "Food & Dining", today);
        savePlaceholder(indivUser, "Monthly Salary", "Income", today);
        savePlaceholder(indivUser, "Coffee Shop", "Coffee", today);
        savePlaceholder(indivUser, "Rent Payment", "Housing", today);
        savePlaceholder(indivUser, "Gas Station", "Transportation", today);
        savePlaceholder(indivUser, "Phone Bill", "Utilities", today);
        savePlaceholder(indivUser, "Amazon Purchase", "Shopping", today);
        logger.info("Successfully seeded 7 placeholder expenses for user 'indiv'");
    }

    private void savePlaceholder(UserEntity user, String name, String category, LocalDate date) {
        ExpenseEntity e = new ExpenseEntity();
        e.setUser(user);
        e.setName(name);
        e.setCategory(category);
        e.setAmount(BigDecimal.ZERO);
        e.setExpenseDate(date);
        // createdAt / updatedAt handled by @PrePersist
        expenseRepository.save(e);
    }
}
