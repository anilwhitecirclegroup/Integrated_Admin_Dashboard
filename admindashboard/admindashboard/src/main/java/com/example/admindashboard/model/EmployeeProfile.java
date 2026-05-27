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

    // SECTION 1: IDENTITY & JOB (Moved from User)
    private String designation;
    private String experience;
    @Column(name = "joining_date")
    private LocalDate joiningDate;

    // SECTION 2: PROJECT & ALLOCATION (Moved from User)
    private String businessUnit;
    private String accountName;
    private String projectName;
    private String projectCode;
    private String teamGroup;
    private String customerName;
    private String verticalName;
    private String domainIndustry;

    // SECTION 3: CONTACT DETAILS (Merged from User & Profile)
    private String mobileNumber;
    private String altMobile;
    private String personalEmail;
    private String workLocation;
    private String city;
    private String country;
    private String permanentAddress;
    private String workingAddress;

    // SECTION 4: REPORTING LINES (Moved from User)
    // Note: The actual security hierarchy is User.manager, but these are kept for display/HR records
    private String reportingManager;
    private String projectManager;
    private String buHrContact;

    // SECTION 5: PERSONAL & LEGAL (Original Profile Data)
    private LocalDate dob;
    private String gender;
    private String aadharNo;
    private String panNo;

    private String emergencyContactName;
    private String relationWithEmployee;
    private String emergencyPhone;

    // SECTION 6: EDUCATION (Original Profile Data)
    private String qual1Title;
    private String qual1Inst;
    private String qual1Year;
    private String qual2Title;
    private String qual2Inst;
    private String qual2Year;

    // Bank Details
    private String bankAccountHolder;
    private String bankAccountNumber;
    private String bankIfscCode;
    private String bankName;
    private String bankBranch;
    private String bankAccountType;

    // GETTERS AND SETTERS
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }

    public String getDesignation() { return designation; }
    public void setDesignation(String designation) { this.designation = designation; }

    public String getExperience() { return experience; }
    public void setExperience(String experience) { this.experience = experience; }

    public LocalDate getJoiningDate() { return joiningDate; }
    public void setJoiningDate(LocalDate joiningDate) { this.joiningDate = joiningDate; }

    public String getBusinessUnit() { return businessUnit; }
    public void setBusinessUnit(String businessUnit) { this.businessUnit = businessUnit; }

    public String getAccountName() { return accountName; }
    public void setAccountName(String accountName) { this.accountName = accountName; }

    public String getProjectName() { return projectName; }
    public void setProjectName(String projectName) { this.projectName = projectName; }

    public String getProjectCode() { return projectCode; }
    public void setProjectCode(String projectCode) { this.projectCode = projectCode; }

    public String getTeamGroup() { return teamGroup; }
    public void setTeamGroup(String teamGroup) { this.teamGroup = teamGroup; }

    public String getCustomerName() { return customerName; }
    public void setCustomerName(String customerName) { this.customerName = customerName; }

    public String getVerticalName() { return verticalName; }
    public void setVerticalName(String verticalName) { this.verticalName = verticalName; }

    public String getDomainIndustry() { return domainIndustry; }
    public void setDomainIndustry(String domainIndustry) { this.domainIndustry = domainIndustry; }

    public String getMobileNumber() { return mobileNumber; }
    public void setMobileNumber(String mobileNumber) { this.mobileNumber = mobileNumber; }

    public String getAltMobile() { return altMobile; }
    public void setAltMobile(String altMobile) { this.altMobile = altMobile; }

    public String getPersonalEmail() { return personalEmail; }
    public void setPersonalEmail(String personalEmail) { this.personalEmail = personalEmail; }

    public String getWorkLocation() { return workLocation; }
    public void setWorkLocation(String workLocation) { this.workLocation = workLocation; }

    public String getCity() { return city; }
    public void setCity(String city) { this.city = city; }

    public String getCountry() { return country; }
    public void setCountry(String country) { this.country = country; }

    public String getPermanentAddress() { return permanentAddress; }
    public void setPermanentAddress(String permanentAddress) { this.permanentAddress = permanentAddress; }

    public String getWorkingAddress() { return workingAddress; }
    public void setWorkingAddress(String workingAddress) { this.workingAddress = workingAddress; }

    public String getReportingManager() { return reportingManager; }
    public void setReportingManager(String reportingManager) { this.reportingManager = reportingManager; }

    public String getProjectManager() { return projectManager; }
    public void setProjectManager(String projectManager) { this.projectManager = projectManager; }

    public String getBuHrContact() { return buHrContact; }
    public void setBuHrContact(String buHrContact) { this.buHrContact = buHrContact; }

    public LocalDate getDob() { return dob; }
    public void setDob(LocalDate dob) { this.dob = dob; }

    public String getGender() { return gender; }
    public void setGender(String gender) { this.gender = gender; }

    public String getAadharNo() { return aadharNo; }
    public void setAadharNo(String aadharNo) { this.aadharNo = aadharNo; }

    public String getPanNo() { return panNo; }
    public void setPanNo(String panNo) { this.panNo = panNo; }

    public String getEmergencyContactName() { return emergencyContactName; }
    public void setEmergencyContactName(String emergencyContactName) { this.emergencyContactName = emergencyContactName; }

    public String getRelationWithEmployee() { return relationWithEmployee; }
    public void setRelationWithEmployee(String relationWithEmployee) { this.relationWithEmployee = relationWithEmployee; }

    public String getEmergencyPhone() { return emergencyPhone; }
    public void setEmergencyPhone(String emergencyPhone) { this.emergencyPhone = emergencyPhone; }

    public String getQual1Title() { return qual1Title; }
    public void setQual1Title(String qual1Title) { this.qual1Title = qual1Title; }

    public String getQual1Inst() { return qual1Inst; }
    public void setQual1Inst(String qual1Inst) { this.qual1Inst = qual1Inst; }

    public String getQual1Year() { return qual1Year; }
    public void setQual1Year(String qual1Year) { this.qual1Year = qual1Year; }

    public String getQual2Title() { return qual2Title; }
    public void setQual2Title(String qual2Title) { this.qual2Title = qual2Title; }

    public String getQual2Inst() { return qual2Inst; }
    public void setQual2Inst(String qual2Inst) { this.qual2Inst = qual2Inst; }

    public String getQual2Year() { return qual2Year; }
    public void setQual2Year(String qual2Year) { this.qual2Year = qual2Year; }

    public String getBankAccountHolder() { return bankAccountHolder; }
    public void setBankAccountHolder(String bankAccountHolder) { this.bankAccountHolder = bankAccountHolder; }

    public String getBankAccountNumber() { return bankAccountNumber; }
    public void setBankAccountNumber(String bankAccountNumber) { this.bankAccountNumber = bankAccountNumber; }

    public String getBankIfscCode() { return bankIfscCode; }
    public void setBankIfscCode(String bankIfscCode) { this.bankIfscCode = bankIfscCode; }

    public String getBankName() { return bankName; }
    public void setBankName(String bankName) { this.bankName = bankName; }

    public String getBankBranch() { return bankBranch; }
    public void setBankBranch(String bankBranch) { this.bankBranch = bankBranch; }

    public String getBankAccountType() { return bankAccountType; }
    public void setBankAccountType(String bankAccountType) { this.bankAccountType = bankAccountType; }
}