package com.example.admindashboard.service;

import com.example.admindashboard.model.Subtask;
import com.example.admindashboard.model.Ticket;
import com.example.admindashboard.repository.SubtaskRepository;
import com.example.admindashboard.repository.TicketRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class TicketProgressService {

    @Autowired
    private TicketRepository ticketRepository;

    @Autowired
    private SubtaskRepository subtaskRepository;

    // TASK 2.1: Auto-Progress Engine
    @Transactional
    public void recalculateTicketProgress(Long ticketId) {
        Ticket ticket = ticketRepository.findById(ticketId)
                .orElseThrow(() -> new IllegalArgumentException("Ticket not found"));

        // Fetch all subtasks for this ticket
        List<Subtask> subtasks = subtaskRepository.findByTicketId(ticketId); // You'll need this in your Repo

        if (subtasks.isEmpty()) {
            return; // No subtasks, progress remains manual
        }

        // Sum the weightage of only the "Completed" subtasks
        int totalProgress = subtasks.stream()
                .filter(st -> "Completed".equalsIgnoreCase(st.getStatus()))
                .mapToInt(Subtask::getWeightage)
                .sum();

        // Cap at 100 just in case of manager data-entry error
        ticket.setProgressPercentage(Math.min(totalProgress, 100));

        // Auto-update status if it hits 100%
        if (ticket.getProgressPercentage() == 100) {
            ticket.setStatus("Completed");
        } else if (ticket.getProgressPercentage() > 0 && "Open".equalsIgnoreCase(ticket.getStatus())) {
            ticket.setStatus("In Progress");
        }

        ticketRepository.save(ticket);
    }

    // TASK 2.2: Ticket Closure Validation
    @Transactional
    public void manuallyCloseTicket(Long ticketId) {
        Ticket ticket = ticketRepository.findById(ticketId)
                .orElseThrow(() -> new IllegalArgumentException("Ticket not found"));

        // Strict validation constraint from requirement doc
        if (ticket.getProgressPercentage() < 100) {
            throw new IllegalStateException("Cannot close ticket. Progress is only at " + ticket.getProgressPercentage() + "%. Must be 100%.");
        }

        ticket.setStatus("Completed");
        ticketRepository.save(ticket);
    }
}