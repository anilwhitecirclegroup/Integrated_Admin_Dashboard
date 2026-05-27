package com.example.admindashboard.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "leave_ledger")
public class LeaveLedger {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

	@Column(name = "days")
	private Double days;

    @ManyToOne
    @JoinColumn(name = "leave_type_id", nullable = false)
    private LeaveTypeMaster leaveType;

    private String transactionType;

    public Double getDays() {
		return days;
	}

	public void setDays(Double days) {
		this.days = days;
	}

	private Double credit = 0.0;

    private Double debit = 0.0;

    private Double balanceAfter = 0.0;

    private String referenceType;

    private Long referenceId;

    @Column(length = 1000)
    private String remarks;

    @ManyToOne
    @JoinColumn(name = "approved_by")
    private User approvedBy;

    private String status = "APPROVED";

    private LocalDateTime transactionDate;

    @PrePersist
    protected void onCreate() {
        transactionDate = LocalDateTime.now();
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

	public String getTransactionType() {
		return transactionType;
	}

	public void setTransactionType(String transactionType) {
		this.transactionType = transactionType;
	}

	public Double getCredit() {
		return credit;
	}

	public void setCredit(Double credit) {
		this.credit = credit;
	}

	public Double getDebit() {
		return debit;
	}

	public void setDebit(Double debit) {
		this.debit = debit;
	}

	public Double getBalanceAfter() {
		return balanceAfter;
	}

	public void setBalanceAfter(Double balanceAfter) {
		this.balanceAfter = balanceAfter;
	}

	public String getReferenceType() {
		return referenceType;
	}

	public void setReferenceType(String referenceType) {
		this.referenceType = referenceType;
	}

	public Long getReferenceId() {
		return referenceId;
	}

	public void setReferenceId(Long referenceId) {
		this.referenceId = referenceId;
	}

	public String getRemarks() {
		return remarks;
	}

	public void setRemarks(String remarks) {
		this.remarks = remarks;
	}

	public User getApprovedBy() {
		return approvedBy;
	}

	public void setApprovedBy(User approvedBy) {
		this.approvedBy = approvedBy;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public LocalDateTime getTransactionDate() {
		return transactionDate;
	}

	public void setTransactionDate(LocalDateTime transactionDate) {
		this.transactionDate = transactionDate;
	}
}