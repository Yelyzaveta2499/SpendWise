package com.example.SpendWise.model;

import com.example.SpendWise.model.entity.Contribution;
import com.example.SpendWise.model.entity.Goal;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

class ContributionTest {

    @Test
    void constructorsSetFieldsCorrectly() {
        Goal goal = new Goal("Trip", 1000.0, LocalDate.of(2030, 1, 1), "user1");

        Contribution c = new Contribution(goal, 50.0, "First");

        assertEquals(goal, c.getGoal());
        assertEquals(50.0, c.getAmount());
        assertEquals("First", c.getNote());
        assertNotNull(c.getContributionDate());
    }

    @Test
    void settersAndGettersWork() {
        Contribution c = new Contribution();
        Goal goal = new Goal();
        goal.setName("Trip");

        LocalDate date = LocalDate.of(2025, 1, 1);

        c.setId(1L);
        c.setGoal(goal);
        c.setAmount(25.0);
        c.setContributionDate(date);
        c.setNote("Test");

        assertEquals(1L, c.getId());
        assertEquals(goal, c.getGoal());
        assertEquals(25.0, c.getAmount());
        assertEquals(date, c.getContributionDate());
        assertEquals("Test", c.getNote());
    }
}

