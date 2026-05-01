package com.example.admindashboard.service;

import com.example.admindashboard.model.Ticket;
import com.example.admindashboard.model.User;
import com.example.admindashboard.repository.TicketRepository;
import com.example.admindashboard.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
public class ClientTicketRoutingService {

    @Autowired
    private TicketRepository ticketRepository;

    @Autowired
    private UserRepository userRepository;

    // =========================================================================
    // 1. DASHBOARD ROUTER: Determines what tickets a user sees based on their role
    // =========================================================================
    public List<Ticket> getInboxForUser(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));

        String role = user.getRole().getRoleName();

        switch (role) {
            case "ROLE_SUPER_ADMIN":
                return ticketRepository.findByWorkflowStateIsNullOrWorkflowStateOrderByIdDesc("UNASSIGNED");

            case "ROLE_DELIVERY_MANAGER":
                return ticketRepository.findByCurrentAssigneeIdAndWorkflowStateOrderByIdDesc(username, "PENDING_DM");

            case "ROLE_PROJECT_MANAGER":
                return ticketRepository.findByCurrentAssigneeIdAndWorkflowStateOrderByIdDesc(username, "PENDING_PM");

            case "ROLE_TEAM_LEAD":
                List<Ticket> tlInbox = new ArrayList<>();
                tlInbox.addAll(ticketRepository.findByWorkflowStateIsNullOrWorkflowStateOrderByIdDesc("UNASSIGNED"));
                tlInbox.addAll(ticketRepository.findByCurrentAssigneeIdAndWorkflowStateOrderByIdDesc(username, "PENDING_TL"));
                return tlInbox;

            case "ROLE_EMPLOYEE":
            default:
                return ticketRepository.findByCurrentAssigneeIdAndWorkflowStateOrderByIdDesc(username, "IN_PROGRESS");
        }
    }

    // =========================================================================
    // 2. ASSIGNMENT ENGINE: Moves the ticket down the hierarchy safely
    // =========================================================================
    @Transactional
    public Ticket routeTicket(Long ticketId, String targetAssigneeUsername, String targetRole) {

        Ticket ticket = ticketRepository.findById(ticketId)
                .orElseThrow(() -> new RuntimeException("Ticket not found"));

        User targetUser = userRepository.findByUsername(targetAssigneeUsername)
                .orElseThrow(() -> new RuntimeException("Target assignee not found"));

        ticket.setCurrentAssigneeId(targetUser.getUsername());

        switch (targetRole) {
            case "ROLE_DELIVERY_MANAGER":
                ticket.setWorkflowState("PENDING_DM");
                break;
            case "ROLE_PROJECT_MANAGER":
                ticket.setWorkflowState("PENDING_PM");
                break;
            case "ROLE_TEAM_LEAD":
                ticket.setWorkflowState("PENDING_TL");
                break;
            case "ROLE_EMPLOYEE":
                ticket.setWorkflowState("IN_PROGRESS");
                ticket.setStatus("In Progress");
                break;
            default:
                throw new IllegalArgumentException("Invalid routing target role.");
        }

        return ticketRepository.save(ticket);
    }
}