package com.example.admindashboard.model;

import jakarta.persistence.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@Entity
@Table(name = "weekly_timesheet")
public class WeeklyTimesheet {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Re-linking the User entity so Thymeleaf and Search can access the employee's name!
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "employee_id", nullable = false)
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
    private User user;

    @Column(name = "week_start_date", nullable = false)
    private LocalDate weekStartDate;

    @Column(name = "week_end_date", nullable = false)
    private LocalDate weekEndDate;

    @Column(nullable = false)
    private String status;

    @Column(columnDefinition = "TEXT")
    private String overallComments;

    @Column(name = "total_week_hours")
    private Double totalWeekHours;

    private LocalDateTime submittedAt;
    private LocalDate submissionDate;

    // Links to the rows
    @OneToMany(mappedBy = "weeklyTimesheet", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<WeeklyTimesheetEntry> entries = new ArrayList<>();

    // GETTERS AND SETTERS

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }

    public LocalDate getWeekStartDate() { return weekStartDate; }
    public void setWeekStartDate(LocalDate weekStartDate) { this.weekStartDate = weekStartDate; }

    public LocalDate getWeekEndDate() { return weekEndDate; }
    public void setWeekEndDate(LocalDate weekEndDate) { this.weekEndDate = weekEndDate; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getOverallComments() { return overallComments; }
    public void setOverallComments(String overallComments) { this.overallComments = overallComments; }

    public Double getTotalWeekHours() { return totalWeekHours; }
    public void setTotalWeekHours(Double totalWeekHours) { this.totalWeekHours = totalWeekHours; }

    public LocalDateTime getSubmittedAt() { return submittedAt; }
    public void setSubmittedAt(LocalDateTime submittedAt) { this.submittedAt = submittedAt; }

    public LocalDate getSubmissionDate() {return submissionDate;}
    public void setSubmissionDate(LocalDate submissionDate) {this.submissionDate = submissionDate;}

    public List<WeeklyTimesheetEntry> getEntries() { return entries; }
    public void setEntries(List<WeeklyTimesheetEntry> entries) { this.entries = entries; }

}