package com.example.admindashboard.model;

import jakarta.persistence.*;
import java.time.LocalDate;
import java.time.LocalTime;

@Entity
@Table(name = "attendance")
public class Attendance {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user; // Links to our existing User table

    private LocalDate date;
    private LocalTime checkInTime;
    private LocalTime checkOutTime;
    private String status; // Present, Absent, Half-Day
    private String totalHours;

    //  WEEKLY ATTENDANCE REGULATION FIELDS
    private String weekStartDate;
    private String weekEndDate;
    private Integer presentDays;
    private Integer absentDays;
    private String reason;
    private String approvalStatus;
    private LocalDate submittedOn;

    // DAILY BREAKDOWN FIELDS
    private Double mondayHours;
    private String mondayStatus;
    private String mondayMode;
    private String mondayReason;

    private Double tuesdayHours;
    private String tuesdayStatus;
    private String tuesdayMode;
    private String tuesdayReason;

    private Double wednesdayHours;
    private String wednesdayStatus;
    private String wednesdayMode;
    private String wednesdayReason;

    private Double thursdayHours;
    private String thursdayStatus;
    private String thursdayMode;
    private String thursdayReason;

    private Double fridayHours;
    private String fridayStatus;
    private String fridayMode;
    private String fridayReason;

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }
    public LocalDate getDate() { return date; }
    public void setDate(LocalDate date) { this.date = date; }
    public LocalTime getCheckInTime() { return checkInTime; }
    public void setCheckInTime(LocalTime checkInTime) { this.checkInTime = checkInTime; }
    public LocalTime getCheckOutTime() { return checkOutTime; }
    public void setCheckOutTime(LocalTime checkOutTime) { this.checkOutTime = checkOutTime; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public String getTotalHours() { return totalHours; }
    public void setTotalHours(String totalHours) { this.totalHours = totalHours; }
    public String getReason() { return reason; }
    public void setReason(String reason) { this.reason = reason; }


    public String getWeekStartDate() { return weekStartDate; }
    public void setWeekStartDate(String weekStartDate) { this.weekStartDate = weekStartDate; }

    public String getWeekEndDate() { return weekEndDate; }
    public void setWeekEndDate(String weekEndDate) { this.weekEndDate = weekEndDate; }

    public Integer getPresentDays() { return presentDays; }
    public void setPresentDays(Integer presentDays) { this.presentDays = presentDays; }

    public Integer getAbsentDays() { return absentDays; }
    public void setAbsentDays(Integer absentDays) { this.absentDays = absentDays; }

    public String getApprovalStatus() { return approvalStatus; }
    public void setApprovalStatus(String approvalStatus) { this.approvalStatus = approvalStatus; }

    public LocalDate getSubmittedOn() { return submittedOn; }
    public void setSubmittedOn(LocalDate submittedOn) { this.submittedOn = submittedOn; }

    public Double getMondayHours() { return mondayHours; }
    public void setMondayHours(Double mondayHours) { this.mondayHours = mondayHours; }
    public String getMondayStatus() { return mondayStatus; }
    public void setMondayStatus(String mondayStatus) { this.mondayStatus = mondayStatus; }
    public String getMondayMode() { return mondayMode; }
    public void setMondayMode(String mondayMode) { this.mondayMode = mondayMode; }
    public String getMondayReason() { return mondayReason; }
    public void setMondayReason(String mondayReason) { this.mondayReason = mondayReason; }

    public Double getTuesdayHours() { return tuesdayHours; }
    public void setTuesdayHours(Double tuesdayHours) { this.tuesdayHours = tuesdayHours; }
    public String getTuesdayStatus() { return tuesdayStatus; }
    public void setTuesdayStatus(String tuesdayStatus) { this.tuesdayStatus = tuesdayStatus; }
    public String getTuesdayMode() { return tuesdayMode; }
    public void setTuesdayMode(String tuesdayMode) { this.tuesdayMode = tuesdayMode; }
    public String getTuesdayReason() { return tuesdayReason; }
    public void setTuesdayReason(String tuesdayReason) { this.tuesdayReason = tuesdayReason; }

    public Double getWednesdayHours() { return wednesdayHours; }
    public void setWednesdayHours(Double wednesdayHours) { this.wednesdayHours = wednesdayHours; }
    public String getWednesdayStatus() { return wednesdayStatus; }
    public void setWednesdayStatus(String wednesdayStatus) { this.wednesdayStatus = wednesdayStatus; }
    public String getWednesdayMode() { return wednesdayMode; }
    public void setWednesdayMode(String wednesdayMode) { this.wednesdayMode = wednesdayMode; }
    public String getWednesdayReason() { return wednesdayReason; }
    public void setWednesdayReason(String wednesdayReason) { this.wednesdayReason = wednesdayReason; }

    public Double getThursdayHours() { return thursdayHours; }
    public void setThursdayHours(Double thursdayHours) { this.thursdayHours = thursdayHours; }
    public String getThursdayStatus() { return thursdayStatus; }
    public void setThursdayStatus(String thursdayStatus) { this.thursdayStatus = thursdayStatus; }
    public String getThursdayMode() { return thursdayMode; }
    public void setThursdayMode(String thursdayMode) { this.thursdayMode = thursdayMode; }
    public String getThursdayReason() { return thursdayReason; }
    public void setThursdayReason(String thursdayReason) { this.thursdayReason = thursdayReason; }

    public Double getFridayHours() { return fridayHours; }
    public void setFridayHours(Double fridayHours) { this.fridayHours = fridayHours; }
    public String getFridayStatus() { return fridayStatus; }
    public void setFridayStatus(String fridayStatus) { this.fridayStatus = fridayStatus; }
    public String getFridayMode() { return fridayMode; }
    public void setFridayMode(String fridayMode) { this.fridayMode = fridayMode; }
    public String getFridayReason() { return fridayReason; }
    public void setFridayReason(String fridayReason) { this.fridayReason = fridayReason; }


}