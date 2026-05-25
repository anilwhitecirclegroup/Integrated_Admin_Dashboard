package com.example.admindashboard.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(
    name = "employee_leave_wallet",
    uniqueConstraints = {
        @UniqueConstraint(columnNames = {"user_id", "leave_type_id"})
    }
)
public class EmployeeLeaveWallet {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne
    @JoinColumn(name = "leave_type_id", nullable = false)
    private LeaveTypeMaster leaveType;

    private Double openingBalance = 0.0;

    private Double earnedCredit = 0.0;

    private Double usedBalance = 0.0;

    private Double encashedBalance = 0.0;

    private Double availableBalance = 0.0;

    private LocalDateTime updatedAt;

    @PrePersist
    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public User getUser() {
		return user;
	}

	public void setUser(User user) {
		this.user = user;
	}

	public LeaveTypeMaster getLeaveType() {
		return leaveType;
	}

	public void setLeaveType(LeaveTypeMaster leaveType) {
		this.leaveType = leaveType;
	}

	public Double getOpeningBalance() {
		return openingBalance;
	}

	public void setOpeningBalance(Double openingBalance) {
		this.openingBalance = openingBalance;
	}

	public Double getEarnedCredit() {
		return earnedCredit;
	}

	public void setEarnedCredit(Double earnedCredit) {
		this.earnedCredit = earnedCredit;
	}

	public Double getUsedBalance() {
		return usedBalance;
	}

	public void setUsedBalance(Double usedBalance) {
		this.usedBalance = usedBalance;
	}

	public Double getEncashedBalance() {
		return encashedBalance;
	}

	public void setEncashedBalance(Double encashedBalance) {
		this.encashedBalance = encashedBalance;
	}

	public Double getAvailableBalance() {
		return availableBalance;
	}

	public void setAvailableBalance(Double availableBalance) {
		this.availableBalance = availableBalance;
	}

	public LocalDateTime getUpdatedAt() {
		return updatedAt;
	}

	public void setUpdatedAt(LocalDateTime updatedAt) {
		this.updatedAt = updatedAt;
	}

}