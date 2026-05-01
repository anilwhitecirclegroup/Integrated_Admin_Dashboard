package com.example.admindashboard.repository;

import com.example.admindashboard.model.Ticket;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TicketRepository extends JpaRepository<Ticket, Long> {
    List<Ticket> findByProjectId(Long projectId);
    List<Ticket> findByStatus(String status);

    // Fetches all tickets assigned to a specific developer
    List<Ticket> findByAssignedTo_Id(Long developerId);

    // Add this inside your TicketRepository interface
    List<Ticket> findByWorkflowStateIsNullOrWorkflowStateOrderByIdDesc(String workflowState);

    // Finds tickets assigned to a specific manager that are in a specific state
    List<Ticket> findByCurrentAssigneeIdAndWorkflowStateOrderByIdDesc(String currentAssigneeId, String workflowState);
}