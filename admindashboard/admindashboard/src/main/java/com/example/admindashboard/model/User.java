package com.example.admindashboard.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;

@Entity
@Table(name = "users")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // LOGIN CREDENTIALS
    @Column(unique = true, nullable = false)
    private String username;
    private String password;

    // BASIC IDENTIFICATION
    private String fullName;
    private String email;

    // RBAC & HIERARCHY (From your Requirement Doc)
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "role_id")
    private Role role;

    // This allows you to link a user to their specific manager for data filtering
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "manager_id")
    @JsonIgnore
    private User manager;

    private Long departmentId; // Optional: For department-level filtering later

    private String status = "ACTIVE"; // ACTIVE, INACTIVE, SUSPENDED

    // LINK TO HR DATA
    @OneToOne(mappedBy = "user", cascade = CascadeType.ALL)
    @JsonIgnore
    private EmployeeProfile employeeProfile;

    // GETTERS AND SETTERS
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public Role getRole() { return role; }
    public void setRole(Role role) { this.role = role; }

    public User getManager() { return manager; }
    public void setManager(User manager) { this.manager = manager; }

    public Long getDepartmentId() { return departmentId; }
    public void setDepartmentId(Long departmentId) { this.departmentId = departmentId; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public EmployeeProfile getEmployeeProfile() { return employeeProfile; }
    public void setEmployeeProfile(EmployeeProfile employeeProfile) { this.employeeProfile = employeeProfile; }
}