package com.example.SpendWise.model.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "expense_tags", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"expense_id", "tag_id"})
})
public class ExpenseTagEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "expense_id", nullable = false)
    private ExpenseEntity expense;

    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tag_id", nullable = false)
    private TagEntity tag;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @PrePersist
    public void prePersist() {
        this.createdAt = LocalDateTime.now();
    }

    public ExpenseTagEntity() {
    }

    public ExpenseTagEntity(ExpenseEntity expense, TagEntity tag) {
        this.expense = expense;
        this.tag = tag;
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public ExpenseEntity getExpense() {
        return expense;
    }

    public void setExpense(ExpenseEntity expense) {
        this.expense = expense;
    }

    public TagEntity getTag() {
        return tag;
    }

    public void setTag(TagEntity tag) {
        this.tag = tag;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ExpenseTagEntity)) return false;
        ExpenseTagEntity that = (ExpenseTagEntity) o;
        return expense != null && expense.equals(that.expense) &&
                tag != null && tag.equals(that.tag);
    }

    @Override
    public int hashCode() {
        return 31;
    }
}

