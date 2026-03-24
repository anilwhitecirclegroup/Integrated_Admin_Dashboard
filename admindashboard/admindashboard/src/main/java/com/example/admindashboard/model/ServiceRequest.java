package com.whitecircle.hrms.model;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDate;

@Entity
@Data
@Table(name = "service_requests")
public class ServiceRequest {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Standard fields for all requests
    private String ticketId;
    private String employeeId;
    private String employeeName;

    // NEW FIELDS: For Admin Clarity
    private String department;
    private String managerName;

    private String type; // SOFTWARE, HARDWARE, INCIDENT, ACCESS, PERMISSION
    private String category; // e.g., Software License, RAM Upgrade, VPN Issue
    private String priority;
    private LocalDate submissionDate;
    private String status = "Pending Approval";

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

        // Logic to assign prefixes based on the request category
        if ("HARDWARE".equalsIgnoreCase(type)) {
            prefix = "HW-";
        } else if ("INCIDENT".equalsIgnoreCase(type)) {
            prefix = "INC-";
        } else {
            // Software, System Access, and Permissions all get SR-
            prefix = "SR-";
        }

        this.ticketId = prefix + (int)(Math.random() * 9000 + 1000);
    }
}