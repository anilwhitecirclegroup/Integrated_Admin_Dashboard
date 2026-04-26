package com.example.admindashboard.model;

import jakarta.persistence.*;
import java.time.LocalDate;

@Entity
@Table(name = "service_requests")
public class ServiceRequest {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Standard fields for all requests
    private String ticketId;
    private String employeeId;
    private String employeeName;

    // NEW FIELDS: Contact Information
    private String employeeEmail;
    private String employeePhone;

    // NEW FIELDS: For Admin Clarity
    private String department;
    private String managerName;

    private String type; // SOFTWARE, HARDWARE, INCIDENT, ACCESS, PERMISSION
    private String category; // e.g., Software License, RAM Upgrade, VPN Issue
    private String priority;
    private LocalDate submissionDate;

    // UPDATED: Default status is now strictly "Open"
    private String status = "Open";

    // Dynamic fields from modals
    private String detailItem;
    private String durationOrLevel;
    private String assetTag;
    private String operatingSystem;
    private String location;

    @Column(columnDefinition = "TEXT")
    private String justification;

    @PrePersist
    public void generateTicketId() {
        String prefix;
        if ("HARDWARE".equalsIgnoreCase(type)) {
            prefix = "HW-";
        } else if ("INCIDENT".equalsIgnoreCase(type)) {
            prefix = "INC-";
        } else {
            prefix = "SR-";
        }
        this.ticketId = prefix + (int)(Math.random() * 9000 + 1000);
    }

    // --- GETTERS AND SETTERS ---

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getTicketId() { return ticketId; }
    public void setTicketId(String ticketId) { this.ticketId = ticketId; }

    public String getEmployeeId() { return employeeId; }
    public void setEmployeeId(String employeeId) { this.employeeId = employeeId; }

    public String getEmployeeName() { return employeeName; }
    public void setEmployeeName(String employeeName) { this.employeeName = employeeName; }

    public String getEmployeeEmail() { return employeeEmail; }
    public void setEmployeeEmail(String employeeEmail) { this.employeeEmail = employeeEmail; }

    public String getEmployeePhone() { return employeePhone; }
    public void setEmployeePhone(String employeePhone) { this.employeePhone = employeePhone; }

    public String getDepartment() { return department; }
    public void setDepartment(String department) { this.department = department; }

    public String getManagerName() { return managerName; }
    public void setManagerName(String managerName) { this.managerName = managerName; }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public String getPriority() { return priority; }
    public void setPriority(String priority) { this.priority = priority; }

    public LocalDate getSubmissionDate() { return submissionDate; }
    public void setSubmissionDate(LocalDate submissionDate) { this.submissionDate = submissionDate; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getDetailItem() { return detailItem; }
    public void setDetailItem(String detailItem) { this.detailItem = detailItem; }

    public String getDurationOrLevel() { return durationOrLevel; }
    public void setDurationOrLevel(String durationOrLevel) { this.durationOrLevel = durationOrLevel; }

    public String getAssetTag() { return assetTag; }
    public void setAssetTag(String assetTag) { this.assetTag = assetTag; }

    public String getOperatingSystem() { return operatingSystem; }
    public void setOperatingSystem(String operatingSystem) { this.operatingSystem = operatingSystem; }

    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = location; }

    public String getJustification() { return justification; }
    public void setJustification(String justification) { this.justification = justification; }
}