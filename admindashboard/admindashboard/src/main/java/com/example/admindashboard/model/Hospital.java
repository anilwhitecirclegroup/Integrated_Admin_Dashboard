package com.example.admindashboard.model;

import jakarta.persistence.*;

@Entity
@Table(name = "network_hospitals")
public class Hospital {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;
    private String location;
    private String contactNumber;

    // Filter flags
    private boolean isCashless;
    private boolean isEmergency24x7;

    // Default Constructor
    public Hospital() {}

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = location; }
    public String getContactNumber() { return contactNumber; }
    public void setContactNumber(String contactNumber) { this.contactNumber = contactNumber; }
    public boolean isCashless() { return isCashless; }
    public void setCashless(boolean cashless) { isCashless = cashless; }
    public boolean isEmergency24x7() { return isEmergency24x7; }
    public void setEmergency24x7(boolean emergency24x7) { isEmergency24x7 = emergency24x7; }
}