package com.example.admindashboard.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;

@Entity
@Table(name = "weekly_timesheet_entry")
public class WeeklyTimesheetEntry {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "weekly_timesheet_id", nullable = false)
    @JsonIgnore
    private WeeklyTimesheet weeklyTimesheet;

    @Column(name = "project_id")
    private Long projectId;

    @Column(name = "task_id")
    private Long taskId;

    @Column(name = "entry_type")
    private String type;

    // The 7 days of hours
    private Double sunHours = 0.0;
    private Double monHours = 0.0;
    private Double tueHours = 0.0;
    private Double wedHours = 0.0;
    private Double thuHours = 0.0;
    private Double friHours = 0.0;
    private Double satHours = 0.0;

    @Column(name = "row_total_hours")
    private Double rowTotalHours;

    @Column(columnDefinition = "TEXT")
    private String comments;

    // GETTERS AND SETTERS

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public WeeklyTimesheet getWeeklyTimesheet() { return weeklyTimesheet; }
    public void setWeeklyTimesheet(WeeklyTimesheet weeklyTimesheet) { this.weeklyTimesheet = weeklyTimesheet; }

    public Long getProjectId() { return projectId; }
    public void setProjectId(Long projectId) { this.projectId = projectId; }

    public Long getTaskId() { return taskId; }
    public void setTaskId(Long taskId) { this.taskId = taskId; }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    public Double getSunHours() { return sunHours; }
    public void setSunHours(Double sunHours) { this.sunHours = sunHours; }

    public Double getMonHours() { return monHours; }
    public void setMonHours(Double monHours) { this.monHours = monHours; }

    public Double getTueHours() { return tueHours; }
    public void setTueHours(Double tueHours) { this.tueHours = tueHours; }

    public Double getWedHours() { return wedHours; }
    public void setWedHours(Double wedHours) { this.wedHours = wedHours; }

    public Double getThuHours() { return thuHours; }
    public void setThuHours(Double thuHours) { this.thuHours = thuHours; }

    public Double getFriHours() { return friHours; }
    public void setFriHours(Double friHours) { this.friHours = friHours; }

    public Double getSatHours() { return satHours; }
    public void setSatHours(Double satHours) { this.satHours = satHours; }

    public Double getRowTotalHours() { return rowTotalHours; }
    public void setRowTotalHours(Double rowTotalHours) { this.rowTotalHours = rowTotalHours; }

    public String getComments() { return comments; }
    public void setComments(String comments) { this.comments = comments; }

}