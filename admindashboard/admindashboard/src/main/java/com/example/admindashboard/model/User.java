package com.example.admindashboard.model;

import com.example.admindashboard.model.EmployeeProfile;
import com.fasterxml.jackson.annotation.JsonIgnore;
import java.time.LocalDate;
import jakarta.persistence.*;

@Entity
@Table(name = "users")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(mappedBy = "user", cascade = CascadeType.ALL)
    @JsonIgnore
    private EmployeeProfile employeeProfile;

    public EmployeeProfile getEmployeeProfile() {
        return employeeProfile;
    }

    public void setEmployeeProfile(EmployeeProfile employeeProfile) {
        this.employeeProfile = employeeProfile;
    }

    // LOGIN CREDENTIALS
    @Column(unique = true, nullable = false)
    private String username;
    private String password;
    private String role;

    // SECTION 1: IDENTITY & JOB
    private String fullName;
    private String email;
    private String designation;
    private String experience;
    @Column(name = "joining_date") // Maps to SQL column
    private LocalDate joiningDate;

    // SECTION 2: PROJECT & ALLOCATION
    private String businessUnit;
    private String accountName;
    private String projectName;
    private String projectCode;
    private String teamGroup;
    private String customerName;
    private String verticalName;
    private String domainIndustry;

    // SECTION 3: CONTACT DETAILS
    private String mobileNumber;
    private String workLocation;
    private String city;
    private String country;

    // SECTION 4: REPORTING LINES
    private String reportingManager;
    private String projectManager;
    private String buHrContact;

    // GETTERS AND SETTERS

    public LocalDate getJoiningDate() { return joiningDate; }
    public void setJoiningDate(LocalDate joiningDate) { this.joiningDate = joiningDate; }

    public String getDesignation() {
        return designation;
    }
    public void setDesignation(String designation) {
        this.designation = designation;
    }

    public String getExperience() { return experience; }
    public void setExperience(String experience) {
        this.experience = experience;
    }

    public String getBusinessUnit() { return businessUnit; }
    public void setBusinessUnit(String businessUnit) {
        this.businessUnit = businessUnit;
    }

    public String getProjectName() {
        return projectName;
    }
    public void setProjectName(String projectName) {
        this.projectName = projectName;
    }

    public String getAccountName() {
        return accountName;
    }
    public void setAccountName(String accountName) {
        this.accountName = accountName;
    }

    public String getProjectCode() {
        return projectCode;
    }
    public void setProjectCode(String projectCode) {
        this.projectCode = projectCode;
    }

    public String getTeamGroup() { return teamGroup; }
    public void setTeamGroup(String teamGroup) {
        this.teamGroup = teamGroup;
    }

    public String getVerticalName() {
        return verticalName;
    }
    public void setVerticalName(String verticalName) {
        this.verticalName = verticalName;
    }

    public String getCustomerName() {
        return customerName;
    }
    public void setCustomerName(String customerName) {
        this.customerName = customerName;
    }

    public String getDomainIndustry() {
        return domainIndustry;
    }
    public void setDomainIndustry(String domainIndustry) {
        this.domainIndustry = domainIndustry;
    }

    public String getMobileNumber() {
        return mobileNumber;
    }
    public void setMobileNumber(String mobileNumber) {
        this.mobileNumber = mobileNumber;
    }

    public String getCity() { return city; }
    public void setCity(String city) {
        this.city = city;
    }

    public String getWorkLocation() {
        return workLocation;
    }
    public void setWorkLocation(String workLocation) {
        this.workLocation = workLocation;
    }

    public String getCountry() {
        return country;
    }
    public void setCountry(String country) {
        this.country = country;
    }

    public String getReportingManager() {
        return reportingManager;
    }
    public void setReportingManager(String reportingManager) {
        this.reportingManager = reportingManager;
    }

    public String getProjectManager() {
        return projectManager;
    }
    public void setProjectManager(String projectManager) {
        this.projectManager = projectManager;
    }

    public String getBuHrContact() {
        return buHrContact;
    }
    public void setBuHrContact(String buHrContact) {
        this.buHrContact = buHrContact;
    }


    // GETTERS/SETTERS FOR KEY FIELDS
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }

    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

}