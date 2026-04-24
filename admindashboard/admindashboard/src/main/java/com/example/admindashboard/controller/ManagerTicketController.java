package com.example.admindashboard.controller;

import com.example.admindashboard.model.Ticket;
import com.example.admindashboard.model.User;
import com.example.admindashboard.repository.ProjectRepository;
import com.example.admindashboard.repository.TicketRepository;
import com.example.admindashboard.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.security.Principal;
import java.util.List;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/manager")
public class ManagerTicketController {

    @Autowired
    private TicketRepository ticketRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ProjectRepository projectRepository;

    @GetMapping("/tickets")
    public String showManagerTicketDashboard(Model model, Principal principal) {

        List<Ticket> allTickets = ticketRepository.findAll();

        List<User> developers = userRepository.findAll().stream()
                .filter(u -> u.getRole() != null && "EMPLOYEE".equals(u.getRole().getRoleName()))
                .collect(Collectors.toList());

        model.addAttribute("tickets", allTickets);
        model.addAttribute("developers", developers);

        User manager = userRepository.findByUsername(principal.getName()).orElse(null);
        if (manager != null) {
            model.addAttribute("managerName", manager.getFullName());
        }

        // Calculate Dynamic Metrics for the UI Cards
        long unassignedCount = allTickets.stream()
                .filter(t -> "Open".equals(t.getStatus()) || t.getStatus() == null)
                .count();

        long activeCount = allTickets.stream()
                .filter(t -> "Development".equals(t.getStatus()))
                .count();

        long completedCount = allTickets.stream()
                .filter(t -> "Completed".equals(t.getStatus()))
                .count();

        // Add them to the Model
        model.addAttribute("unassignedCount", unassignedCount);
        model.addAttribute("activeCount", activeCount);
        model.addAttribute("completedCount", completedCount);

        // Updated to match your new file name
        return "admin-manage-client-tickets";
    }

    @org.springframework.web.bind.annotation.PostMapping("/tickets/assign")
    public String assignTicket(@org.springframework.web.bind.annotation.RequestParam Long ticketId,
                               @org.springframework.web.bind.annotation.RequestParam(required = false) Long assigneeId) {

        Ticket ticket = ticketRepository.findById(ticketId).orElse(null);

        if (ticket != null) {
            if (assigneeId != null) {
                User assignee = userRepository.findById(assigneeId).orElse(null);
                // Note: Ensure your Ticket.java model has a 'private User assignedTo;' field with getters/setters!
                ticket.setAssignedTo(assignee);
                ticket.setStatus("Development"); // Auto-update status when assigned
            } else {
                ticket.setAssignedTo(null);
                ticket.setStatus("Open"); // Revert status if unassigned
            }
            ticketRepository.save(ticket);
        }

        return "redirect:/manager/tickets"; // Refresh the page to show the update
    }

}