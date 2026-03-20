package com.example.SpendWise.model.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import java.time.LocalDate;

@Entity
@Table(name = "contributions")
public class Contribution {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @JsonIgnore
    @ManyToOne
    @JoinColumn(name = "goal_id", nullable = false)
    private Goal goal;

    @Column(nullable = false)
    private Double amount;

    @Column(nullable = false)
    private LocalDate contributionDate;

    private String note;


    public Contribution() {
        this.contributionDate = LocalDate.now();
    }

    public Contribution(Goal goal, Double amount, String note) {
        this.goal = goal;
        this.amount = amount;
        this.note = note;
        this.contributionDate = LocalDate.now();
    }


    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Goal getGoal() {
        return goal;
    }

    public void setGoal(Goal goal) {
        this.goal = goal;
    }

    public Double getAmount() {
        return amount;
    }

    public void setAmount(Double amount) {
        this.amount = amount;
    }

    public LocalDate getContributionDate() {
        return contributionDate;
    }

    public void setContributionDate(LocalDate contributionDate) {
        this.contributionDate = contributionDate;
    }

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
    }
}

