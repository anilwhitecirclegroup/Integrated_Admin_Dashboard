package com.example.admindashboard.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "timesheets")
public class Timesheet {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    @JsonIgnoreProperties({"password", "timesheets", "roles", "attendance", "hibernateLazyInitializer", "handler"})
    private User user;

    private String weekRange;
    private String status;

    @Column(name = "submission_date")
    private LocalDate submissionDate;

    private String rejectionReason;
    private String comments;
    private LocalDate weekStartDate;
    private LocalDate weekEndDate;

    @Column(name = "approved_by")
    private String approvedBy;
    @Column(name = "total_hours")
    private Double totalHours;

    // Daily Hour tracking
    private Double mondayHours;
    private String mondayTask;
    private Double tuesdayHours;
    private String tuesdayTask;
    private Double wednesdayHours;
    private String wednesdayTask;
    private Double thursdayHours;
    private String thursdayTask;
    private Double fridayHours;
    private String fridayTask;

    private Integer presentDays;
    private Integer absentDays;

    public Integer getPresentDays() {
        return presentDays;
    }
    public void setPresentDays(Integer presentDays) {
        this.presentDays = presentDays;
    }

    public Integer getAbsentDays() {
        return absentDays;
    }
    public void setAbsentDays(Integer absentDays) {
        this.absentDays = absentDays;
    }

    @JsonManagedReference
    @OneToMany(mappedBy = "timesheet", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<TimesheetEntry> entries = new ArrayList<>();

    // GETTERS AND SETTERS
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }

    public String getWeekRange() { return weekRange; }
    public void setWeekRange(String weekRange) { this.weekRange = weekRange; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public LocalDate getSubmissionDate() { return submissionDate; }
    public void setSubmissionDate(LocalDate submissionDate) { this.submissionDate = submissionDate; }

    public String getRejectionReason() { return rejectionReason; }
    public void setRejectionReason(String rejectionReason) { this.rejectionReason = rejectionReason; }

    public String getComments() { return comments; }
    public void setComments(String comments) { this.comments = comments; }

    public LocalDate getWeekStartDate() { return weekStartDate; }
    public void setWeekStartDate(LocalDate weekStartDate) { this.weekStartDate = weekStartDate; }

    public LocalDate getWeekEndDate() { return weekEndDate; }
    public void setWeekEndDate(LocalDate weekEndDate) { this.weekEndDate = weekEndDate; }

    public String getApprovedBy() { return approvedBy; }
    public void setApprovedBy(String approvedBy) { this.approvedBy = approvedBy; }

    public Double getTotalHours() { return totalHours; }
    public void setTotalHours(Double totalHours) { this.totalHours = totalHours; }

    public Double getMondayHours() { return mondayHours; }
    public void setMondayHours(Double mondayHours) { this.mondayHours = mondayHours; }

    public String getMondayTask() { return mondayTask; }
    public void setMondayTask(String mondayTask) { this.mondayTask = mondayTask; }

    public Double getTuesdayHours() { return tuesdayHours; }
    public void setTuesdayHours(Double tuesdayHours) { this.tuesdayHours = tuesdayHours; }

    public String getTuesdayTask() { return tuesdayTask; }
    public void setTuesdayTask(String tuesdayTask) { this.tuesdayTask = tuesdayTask; }

    public Double getWednesdayHours() { return wednesdayHours; }
    public void setWednesdayHours(Double wednesdayHours) { this.wednesdayHours = wednesdayHours; }

    public String getWednesdayTask() { return wednesdayTask; }
    public void setWednesdayTask(String wednesdayTask) { this.wednesdayTask = wednesdayTask; }

    public Double getThursdayHours() { return thursdayHours; }
    public void setThursdayHours(Double thursdayHours) { this.thursdayHours = thursdayHours; }

    public String getThursdayTask() { return thursdayTask; }
    public void setThursdayTask(String thursdayTask) { this.thursdayTask = thursdayTask; }

    public Double getFridayHours() { return fridayHours; }
    public void setFridayHours(Double fridayHours) { this.fridayHours = fridayHours; }

    public String getFridayTask() { return fridayTask; }
    public void setFridayTask(String fridayTask) { this.fridayTask = fridayTask; }

    public List<TimesheetEntry> getEntries() { return entries; }
    public void setEntries(List<TimesheetEntry> entries) { this.entries = entries; }
}