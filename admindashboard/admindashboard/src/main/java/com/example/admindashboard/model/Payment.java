package com.example.admindashboard.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "payments")
public class Payment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Link to the Project this payment ledger belongs to
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "project_id", nullable = false)
    @JsonIgnore
    private Project project;

    // Using BigDecimal for financial data to prevent floating-point precision errors
    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal totalProjectCost = BigDecimal.ZERO;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal paidAmount = BigDecimal.ZERO;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal pendingAmount = BigDecimal.ZERO;

    // e.g., "Pending", "Partially Paid", "Fully Paid"
    @Column(nullable = false)
    private String paymentStatus = "Pending";

    // Used by the Cron Job to trigger automated emails before the cycle ends
    private LocalDate nextBillingDate;

    @Column(updatable = false)
    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
        calculatePendingAmount();
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
        calculatePendingAmount();
    }

    // Auto-calculates the pending amount to ensure data integrity
    private void calculatePendingAmount() {
        if (this.totalProjectCost != null && this.paidAmount != null) {
            this.pendingAmount = this.totalProjectCost.subtract(this.paidAmount);

            if (this.pendingAmount.compareTo(BigDecimal.ZERO) <= 0) {
                this.paymentStatus = "Fully Paid";
            } else if (this.paidAmount.compareTo(BigDecimal.ZERO) > 0) {
                this.paymentStatus = "Partially Paid";
            } else {
                this.paymentStatus = "Pending";
            }
        }
    }

    // --- GETTERS AND SETTERS ---

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Project getProject() { return project; }
    public void setProject(Project project) { this.project = project; }

    public BigDecimal getTotalProjectCost() { return totalProjectCost; }
    public void setTotalProjectCost(BigDecimal totalProjectCost) { this.totalProjectCost = totalProjectCost; }

    public BigDecimal getPaidAmount() { return paidAmount; }
    public void setPaidAmount(BigDecimal paidAmount) { this.paidAmount = paidAmount; }

    public BigDecimal getPendingAmount() { return pendingAmount; }
    // Pending amount is auto-calculated, but providing a setter for framework compatibility
    public void setPendingAmount(BigDecimal pendingAmount) { this.pendingAmount = pendingAmount; }

    public String getPaymentStatus() { return paymentStatus; }
    public void setPaymentStatus(String paymentStatus) { this.paymentStatus = paymentStatus; }

    public LocalDate getNextBillingDate() { return nextBillingDate; }
    public void setNextBillingDate(LocalDate nextBillingDate) { this.nextBillingDate = nextBillingDate; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}