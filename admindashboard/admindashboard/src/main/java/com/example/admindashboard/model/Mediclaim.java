package com.example.admindashboard.model;

import jakarta.persistence.*;
import java.time.LocalDate;

@Entity
@Table(name = "mediclaim_requests")
public class Mediclaim {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    // Step 1: Hospital Info
    private String hospitalName;
    private String city;
    private String claimType; // Reimbursement or Cashless

    // Step 2: Admission Details
    private String diagnosis;
    private LocalDate dateOfAdmission;
    private LocalDate dateOfDischarge;

    // Step 3: Financials
    private Double totalBill;
    private Double claimAmount;
    private String remarks;

    // Step 4: Uploads
    private String documentFilename;

    // System Tracking
    private String status = "Pending"; // Pending, Approved, Rejected
    private LocalDate submissionDate = LocalDate.now();

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }
    public String getHospitalName() { return hospitalName; }
    public void setHospitalName(String hospitalName) { this.hospitalName = hospitalName; }
    public String getCity() { return city; }
    public void setCity(String city) { this.city = city; }
    public String getClaimType() { return claimType; }
    public void setClaimType(String claimType) { this.claimType = claimType; }
    public String getDiagnosis() { return diagnosis; }
    public void setDiagnosis(String diagnosis) { this.diagnosis = diagnosis; }
    public LocalDate getDateOfAdmission() { return dateOfAdmission; }
    public void setDateOfAdmission(LocalDate dateOfAdmission) { this.dateOfAdmission = dateOfAdmission; }
    public LocalDate getDateOfDischarge() { return dateOfDischarge; }
    public void setDateOfDischarge(LocalDate dateOfDischarge) { this.dateOfDischarge = dateOfDischarge; }
    public Double getTotalBill() { return totalBill; }
    public void setTotalBill(Double totalBill) { this.totalBill = totalBill; }
    public Double getClaimAmount() { return claimAmount; }
    public void setClaimAmount(Double claimAmount) { this.claimAmount = claimAmount; }
    public String getRemarks() { return remarks; }
    public void setRemarks(String remarks) { this.remarks = remarks; }
    public String getDocumentFilename() { return documentFilename; }
    public void setDocumentFilename(String documentFilename) { this.documentFilename = documentFilename; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public LocalDate getSubmissionDate() { return submissionDate; }
    public void setSubmissionDate(LocalDate submissionDate) { this.submissionDate = submissionDate; }
}