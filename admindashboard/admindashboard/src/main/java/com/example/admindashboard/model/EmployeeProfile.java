package com.example.admindashboard.model;

import jakarta.persistence.*;
import java.time.LocalDate;

@Entity
@Table(name = "employee_profiles")
public class EmployeeProfile {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne
    @JoinColumn(name = "user_id")
    private User user;

    private LocalDate dob;
    private String gender;
    private String personalEmail;
    private String aadharNo;

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

    public LocalDate getDob() {
        return dob;
    }
    public void setDob(LocalDate dob) {
        this.dob = dob;
    }

    public String getPersonalEmail() {
        return personalEmail;
    }
    public void setPersonalEmail(String personalEmail) {
        this.personalEmail = personalEmail;
    }

    public String getGender() {
        return gender;
    }
    public void setGender(String gender) {
        this.gender = gender;
    }

    public String getAadharNo() {
        return aadharNo;
    }
    public void setAadharNo(String aadharNo) {
        this.aadharNo = aadharNo;
    }

    public String getPanNo() {
        return panNo;
    }
    public void setPanNo(String panNo) {
        this.panNo = panNo;
    }

    public String getPermanentAddress() {
        return permanentAddress;
    }
    public void setPermanentAddress(String permanentAddress) {
        this.permanentAddress = permanentAddress;
    }

    public String getWorkingAddress() {
        return workingAddress;
    }
    public void setWorkingAddress(String workingAddress) {
        this.workingAddress = workingAddress;
    }

    public String getQual1Title() {
        return qual1Title;
    }
    public void setQual1Title(String qual1Title) {
        this.qual1Title = qual1Title;
    }

    public String getQual1Inst() {
        return qual1Inst;
    }
    public void setQual1Inst(String qual1Inst) {
        this.qual1Inst = qual1Inst;
    }

    public String getQual1Year() {
        return qual1Year;
    }
    public void setQual1Year(String qual1Year) {
        this.qual1Year = qual1Year;
    }

    public String getQual2Title() {
        return qual2Title;
    }
    public void setQual2Title(String qual2Title) {
        this.qual2Title = qual2Title;
    }

    public String getQual2Inst() {
        return qual2Inst;
    }
    public void setQual2Inst(String qual2Inst) {
        this.qual2Inst = qual2Inst;
    }

    public String getQual2Year() {
        return qual2Year;
    }
    public void setQual2Year(String qual2Year) {
        this.qual2Year = qual2Year;
    }

    public String getAltMobile() {
        return altMobile;
    }
    public void setAltMobile(String altMobile) {
        this.altMobile = altMobile;
    }

    public String getEmergencyContactName() {
        return emergencyContactName;
    }
    public void setEmergencyContactName(String emergencyContactName) {
        this.emergencyContactName = emergencyContactName;
    }

    public String getRelationWithEmployee() {
        return relationWithEmployee;
    }
    public void setRelationWithEmployee(String relationWithEmployee) {
        this.relationWithEmployee = relationWithEmployee;
    }

    public String getEmergencyPhone() {
        return emergencyPhone;
    }
    public void setEmergencyPhone(String emergencyPhone) {
        this.emergencyPhone = emergencyPhone;
    }

    private String panNo;

    private String permanentAddress;
    private String workingAddress;

    private String qual1Title;
    private String qual1Inst;
    private String qual1Year;
    private String qual2Title;
    private String qual2Inst;
    private String qual2Year;

    private String altMobile;
    private String emergencyContactName;
    private String relationWithEmployee;
    private String emergencyPhone;

    // Standard Getters and Setters for all fields...
}