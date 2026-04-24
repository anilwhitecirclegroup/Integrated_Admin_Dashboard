package com.example.admindashboard.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "task_update_logs")
public class TaskUpdateLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Links this specific log to a specific ticket
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ticket_id", nullable = false)
    @JsonIgnore
    private Ticket ticket;

    @Column(columnDefinition = "TEXT", nullable = false)
    private String updateNote;

    private Integer recordedPercentage;

    @Column(updatable = false)
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        this.updatedAt = LocalDateTime.now();
    }

    // --- GETTERS AND SETTERS ---
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Ticket getTicket() { return ticket; }
    public void setTicket(Ticket ticket) { this.ticket = ticket; }

    public String getUpdateNote() { return updateNote; }
    public void setUpdateNote(String updateNote) { this.updateNote = updateNote; }

    public Integer getRecordedPercentage() { return recordedPercentage; }
    public void setRecordedPercentage(Integer recordedPercentage) { this.recordedPercentage = recordedPercentage; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
}