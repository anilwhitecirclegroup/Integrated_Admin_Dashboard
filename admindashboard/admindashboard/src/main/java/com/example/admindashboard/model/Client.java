package com.example.admindashboard.model;

import jakarta.persistence.*;

@Entity
@Table(name = "clients")
public class Client {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Company Profile
    @Column(nullable = false, unique = true)
    private String clientId;

    @Column(nullable = false)
    private String companyName;

    private String domain;
    private String accountStatus;

    // Contact Details
    @Column(nullable = false)
    private String contactPerson;

    @Column(nullable = false)
    private String officialEmail;

    @Column(nullable = false)
    private String phoneNumber;

    // Internal Allocation
    private String assignedTeam;
    private String projectManager;
    private String assignedEmployee;

    // Billing & Location
    @Column(columnDefinition = "TEXT")
    private String billingAddress;

    private String city;
    private String country;

    // ==========================================
    //          GETTERS AND SETTERS
    // ==========================================

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getClientId() { return clientId; }
    public void setClientId(String clientId) { this.clientId = clientId; }

    public String getCompanyName() { return companyName; }
    public void setCompanyName(String companyName) { this.companyName = companyName; }

    public String getDomain() { return domain; }
    public void setDomain(String domain) { this.domain = domain; }

    public String getAccountStatus() { return accountStatus; }
    public void setAccountStatus(String accountStatus) { this.accountStatus = accountStatus; }

    public String getContactPerson() { return contactPerson; }
    public void setContactPerson(String contactPerson) { this.contactPerson = contactPerson; }

    public String getOfficialEmail() { return officialEmail; }
    public void setOfficialEmail(String officialEmail) { this.officialEmail = officialEmail; }

    public String getPhoneNumber() { return phoneNumber; }
    public void setPhoneNumber(String phoneNumber) { this.phoneNumber = phoneNumber; }

    public String getAssignedTeam() { return assignedTeam; }
    public void setAssignedTeam(String assignedTeam) { this.assignedTeam = assignedTeam; }

    public String getProjectManager() { return projectManager; }
    public void setProjectManager(String projectManager) { this.projectManager = projectManager; }

    public String getAssignedEmployee() { return assignedEmployee; }
    public void setAssignedEmployee(String assignedEmployee) { this.assignedEmployee = assignedEmployee; }

    public String getBillingAddress() { return billingAddress; }
    public void setBillingAddress(String billingAddress) { this.billingAddress = billingAddress; }

    public String getCity() { return city; }
    public void setCity(String city) { this.city = city; }

    public String getCountry() { return country; }
    public void setCountry(String country) { this.country = country; }
}