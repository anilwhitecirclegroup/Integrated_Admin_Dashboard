package com.example.admindashboard.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "leave_type_master")
public class LeaveTypeMaster {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String leaveCode;

    @Column(nullable = false)
    private String leaveName;

    private Double monthlyCredit = 0.0;

    private Double maxBalance = 0.0;

    private Boolean carryForward = false;

    private Boolean isPaid = true;

    private Boolean requiresApproval = true;

    private Boolean active = true;

    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getLeaveCode() {
		return leaveCode;
	}

	public void setLeaveCode(String leaveCode) {
		this.leaveCode = leaveCode;
	}

	public String getLeaveName() {
		return leaveName;
	}

	public void setLeaveName(String leaveName) {
		this.leaveName = leaveName;
	}

	public Double getMonthlyCredit() {
		return monthlyCredit;
	}

	public void setMonthlyCredit(Double monthlyCredit) {
		this.monthlyCredit = monthlyCredit;
	}

	public Double getMaxBalance() {
		return maxBalance;
	}

	public void setMaxBalance(Double maxBalance) {
		this.maxBalance = maxBalance;
	}

	public Boolean getCarryForward() {
		return carryForward;
	}

	public void setCarryForward(Boolean carryForward) {
		this.carryForward = carryForward;
	}

	public Boolean getIsPaid() {
		return isPaid;
	}

	public void setIsPaid(Boolean isPaid) {
		this.isPaid = isPaid;
	}

	public Boolean getRequiresApproval() {
		return requiresApproval;
	}

	public void setRequiresApproval(Boolean requiresApproval) {
		this.requiresApproval = requiresApproval;
	}

	public Boolean getActive() {
		return active;
	}

	public void setActive(Boolean active) {
		this.active = active;
	}

	public LocalDateTime getCreatedAt() {
		return createdAt;
	}

	public void setCreatedAt(LocalDateTime createdAt) {
		this.createdAt = createdAt;
	}

}