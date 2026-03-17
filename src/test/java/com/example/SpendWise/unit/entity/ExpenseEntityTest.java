package com.example.SpendWise.unit.entity;

import com.example.SpendWise.model.entity.ExpenseEntity;
import com.example.SpendWise.model.entity.ExpenseTagEntity;
import com.example.SpendWise.model.entity.TagEntity;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

class ExpenseEntityTest {

    @Test
    void addAndRemoveTag_maintainsBidirectionalRelationship() {
        ExpenseEntity expense = new ExpenseEntity();
        expense.setName("Test");
        expense.setCategory("Category");
        expense.setAmount(new BigDecimal("10.00"));
        expense.setExpenseDate(LocalDate.now());

        TagEntity tag = new TagEntity();
        tag.setName("Food");

        expense.addTag(tag);

        assertEquals(1, expense.getExpenseTags().size());
        assertEquals(1, tag.getExpenseTags().size());

        ExpenseTagEntity link = expense.getExpenseTags().iterator().next();
        assertSame(expense, link.getExpense());
        assertSame(tag, link.getTag());

        expense.removeTag(tag);

        assertTrue(expense.getExpenseTags().isEmpty());
        assertTrue(tag.getExpenseTags().isEmpty());
        assertNull(link.getExpense());
        assertNull(link.getTag());
    }

    @Test
    void clearTags_removesAllLinksSafely() {
        ExpenseEntity expense = new ExpenseEntity();
        expense.setName("Test");
        expense.setCategory("Category");
        expense.setAmount(new BigDecimal("10.00"));
        expense.setExpenseDate(LocalDate.now());

        TagEntity tag1 = new TagEntity();
        tag1.setName("Food");
        TagEntity tag2 = new TagEntity();
        tag2.setName("Health");

        expense.addTag(tag1);
        expense.addTag(tag2);

        assertEquals(2, expense.getExpenseTags().size());

        expense.clearTags();

        assertTrue(expense.getExpenseTags().isEmpty());
        assertTrue(tag1.getExpenseTags().isEmpty());
        assertTrue(tag2.getExpenseTags().isEmpty());
    }
}

