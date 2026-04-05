package com.example.admindashboard.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "audit_logs")
public class AuditLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // The user who performed the action (storing username as a string is safer for audits
    // in case the actual User account is ever deleted in the future)
    private String username;

    private String action; // e.g., "DELETE", "UPDATE", "APPROVE"
    private String module; // e.g., "EMPLOYEE_DATA", "PAYROLL", "LEAVE"

    @Column(columnDefinition = "TEXT")
    private String oldValue; // What the data looked like before

    @Column(columnDefinition = "TEXT")
    private String newValue; // What the data was changed to

    private String ipAddress;

    @Column(nullable = false)
    private LocalDateTime timestamp;

    // GETTERS AND SETTERS
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getAction() { return action; }
    public void setAction(String action) { this.action = action; }

    public String getModule() { return module; }
    public void setModule(String module) { this.module = module; }

    public String getOldValue() { return oldValue; }
    public void setOldValue(String oldValue) { this.oldValue = oldValue; }

    public String getNewValue() { return newValue; }
    public void setNewValue(String newValue) { this.newValue = newValue; }

    public String getIpAddress() { return ipAddress; }
    public void setIpAddress(String ipAddress) { this.ipAddress = ipAddress; }

    public LocalDateTime getTimestamp() { return timestamp; }
    public void setTimestamp(LocalDateTime timestamp) { this.timestamp = timestamp; }
}