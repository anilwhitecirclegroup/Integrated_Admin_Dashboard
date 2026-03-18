package com.example.admindashboard.dto;

import java.time.LocalDate;

public class LeaveReportDTO {
    private String userFullName;
    private String username;
    private String leaveType;
    private LocalDate fromDate;
    private LocalDate toDate;
    private Double totalDays;
    private String status;
    private String reason;

    public LeaveReportDTO(String userFullName, String username, String leaveType, LocalDate fromDate, LocalDate toDate, Double totalDays, String status, String reason) {
        this.userFullName = userFullName;
        this.username = username;
        this.leaveType = leaveType;
        this.fromDate = fromDate;
        this.toDate = toDate;
        this.totalDays = totalDays;
        this.status = status;
        this.reason = reason;
    }

    // Getters
    public String getUserFullName() { return userFullName; }
    public String getUsername() { return username; }
    public String getLeaveType() { return leaveType; }
    public LocalDate getFromDate() { return fromDate; }
    public LocalDate getToDate() { return toDate; }
    public Double getTotalDays() { return totalDays; }
    public String getStatus() { return status; }
    public String getReason() { return reason; }
}