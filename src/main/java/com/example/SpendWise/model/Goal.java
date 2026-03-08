package com.example.SpendWise.model;

import jakarta.persistence.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "goals")
public class Goal {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private Double targetAmount;

    @Column(nullable = false)
    private Double currentAmount = 0.0;

    private LocalDate deadline;

    private String icon;

    private String color;

    @Column(nullable = false)
    private String userId;

    @Column(nullable = false)
    private LocalDate createdDate;

    @OneToMany(mappedBy = "goal", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Contribution> contributions = new ArrayList<>();


    public Goal() {
        this.createdDate = LocalDate.now();
        this.currentAmount = 0.0;
    }

    public Goal(String name, Double targetAmount, LocalDate deadline, String userId) {
        this.name = name;
        this.targetAmount = targetAmount;
        this.deadline = deadline;
        this.userId = userId;
        this.createdDate = LocalDate.now();
        this.currentAmount = 0.0;
    }


    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Double getTargetAmount() {
        return targetAmount;
    }

    public void setTargetAmount(Double targetAmount) {
        this.targetAmount = targetAmount;
    }

    public Double getCurrentAmount() {
        return currentAmount;
    }

    public void setCurrentAmount(Double currentAmount) {
        this.currentAmount = currentAmount;
    }

    public LocalDate getDeadline() {
        return deadline;
    }

    public void setDeadline(LocalDate deadline) {
        this.deadline = deadline;
    }

    public String getIcon() {
        return icon;
    }

    public void setIcon(String icon) {
        this.icon = icon;
    }

    public String getColor() {
        return color;
    }

    public void setColor(String color) {
        this.color = color;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public LocalDate getCreatedDate() {
        return createdDate;
    }

    public void setCreatedDate(LocalDate createdDate) {
        this.createdDate = createdDate;
    }

    public List<Contribution> getContributions() {
        return contributions;
    }

    public void setContributions(List<Contribution> contributions) {
        this.contributions = contributions;
    }

    // Helper methods to calculate remaining amount and percentage
    public Double getRemainingAmount() {
        return targetAmount - currentAmount;
    }

    public Integer getProgressPercentage() {
        if (targetAmount == 0) {
            return 0;
        }
        return (int) Math.round((currentAmount / targetAmount) * 100);
    }

    public boolean isDeadlinePassed() {
        if (deadline == null) {
            return false;
        }
        return LocalDate.now().isAfter(deadline);
    }
}

