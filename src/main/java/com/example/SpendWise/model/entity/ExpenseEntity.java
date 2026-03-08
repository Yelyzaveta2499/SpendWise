package com.example.SpendWise.model.entity;

import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "expenses")
public class ExpenseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private UserEntity user;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(nullable = false, length = 100)
    private String category;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal amount;

    @Column(name = "expense_date", nullable = false)
    private LocalDate expenseDate;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @OneToMany(mappedBy = "expense", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<ExpenseTagEntity> expenseTags = new HashSet<>();

    @PrePersist
    public void prePersist() {
        LocalDateTime now = LocalDateTime.now();
        this.createdAt = now;
        this.updatedAt = now;
    }

    @PreUpdate
    public void preUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    public ExpenseEntity() {
    }


    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public UserEntity getUser() { return user; }
    public void setUser(UserEntity user) { this.user = user; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public BigDecimal getAmount() { return amount; }
    public void setAmount(BigDecimal amount) { this.amount = amount; }

    public LocalDate getExpenseDate() { return expenseDate; }
    public void setExpenseDate(LocalDate expenseDate) { this.expenseDate = expenseDate; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }

    public Set<ExpenseTagEntity> getExpenseTags() { return expenseTags; }
    public void setExpenseTags(Set<ExpenseTagEntity> expenseTags) { this.expenseTags = expenseTags; }

    // Helper methods for managing bidirectional relationship
    public void addTag(TagEntity tag) {
        ExpenseTagEntity expenseTag = new ExpenseTagEntity(this, tag);
        expenseTags.add(expenseTag);
        tag.getExpenseTags().add(expenseTag);
    }

    public void removeTag(TagEntity tag) {
        for (ExpenseTagEntity expenseTag : expenseTags) {
            if (expenseTag.getExpense().equals(this) && expenseTag.getTag().equals(tag)) {
                expenseTags.remove(expenseTag);
                tag.getExpenseTags().remove(expenseTag);
                expenseTag.setExpense(null);
                expenseTag.setTag(null);
                break;
            }
        }
    }

    public void clearTags() {
        for (ExpenseTagEntity expenseTag : new HashSet<>(expenseTags)) {
            removeTag(expenseTag.getTag());
        }
    }
}
