package com.example.admindashboard.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Entity
@Table(name = "tickets")
public class Ticket {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String description;

    // Priority: Low, Medium, High
    @Column(nullable = false)
    private String priority = "Medium";

    // Status: Open, In Progress, Completed
    @Column(nullable = false)
    private String status = "Open";

    private LocalDate deadline;

    // Overall progress (0 to 100). Will be auto-calculated later via subtasks.
    private Integer progressPercentage = 0;

    // Link to the parent Project
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "project_id", nullable = false)
    @JsonIgnore
    private Project project;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "assigned_to_id")
    private User assignedTo;

    // --- NEW: Link to child Subtasks ---
    @OneToMany(mappedBy = "ticket", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JsonIgnore
    private List<Subtask> subtasks = new ArrayList<>();

    // Assigning multiple team members to a single ticket
    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
            name = "ticket_assignments",
            joinColumns = @JoinColumn(name = "ticket_id"),
            inverseJoinColumns = @JoinColumn(name = "user_id")
    )
    @JsonIgnore
    private Set<User> assignedMembers = new HashSet<>();

    @Column(updatable = false)
    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    @OneToMany(mappedBy = "ticket", cascade = CascadeType.ALL)
    @OrderBy("createdAt DESC")
    private java.util.List<TimeLog> timeLogs;

    // NEW: Bidirectional mapping to fetch the audit trail automatically
    @OneToMany(mappedBy = "ticket", cascade = CascadeType.ALL)
    @OrderBy("updatedAt DESC") // Ensures the newest updates appear at the top
    private List<TaskUpdateLog> updateLogs;

    public List<TaskUpdateLog> getUpdateLogs() { return updateLogs; }
    public void setUpdateLogs(List<TaskUpdateLog> updateLogs) { this.updateLogs = updateLogs; }

    // NEW: Time Tracking Field
    @Column(name = "total_time_tracked", columnDefinition = "BIGINT DEFAULT 0")
    private Long totalTimeTracked = 0L; // Stores total time in seconds

    // --- GETTERS AND SETTERS ---

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getPriority() { return priority; }
    public void setPriority(String priority) { this.priority = priority; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public LocalDate getDeadline() { return deadline; }
    public void setDeadline(LocalDate deadline) { this.deadline = deadline; }

    public Integer getProgressPercentage() { return progressPercentage; }
    public void setProgressPercentage(Integer progressPercentage) { this.progressPercentage = progressPercentage; }

    public Project getProject() { return project; }
    public void setProject(Project project) { this.project = project; }

    // --- NEW: Subtask Getters and Setters ---
    public List<Subtask> getSubtasks() { return subtasks; }
    public void setSubtasks(List<Subtask> subtasks) { this.subtasks = subtasks; }

    public Set<User> getAssignedMembers() { return assignedMembers; }
    public void setAssignedMembers(Set<User> assignedMembers) { this.assignedMembers = assignedMembers; }

    // Helper methods for managing assignments easily
    public void addAssignedMember(User user) { this.assignedMembers.add(user); }
    public void removeAssignedMember(User user) { this.assignedMembers.remove(user); }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }

    public User getAssignedTo() { return assignedTo; }
    public void setAssignedTo(User assignedTo) { this.assignedTo = assignedTo; }

    public Long getTotalTimeTracked() {return totalTimeTracked == null ? 0L : totalTimeTracked;}
    public void setTotalTimeTracked(Long totalTimeTracked) {this.totalTimeTracked = totalTimeTracked;}

}