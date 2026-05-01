package com.example.admindashboard.controller;

import com.example.admindashboard.model.Ticket;
import com.example.admindashboard.model.User;
import com.example.admindashboard.repository.TicketRepository;
import com.example.admindashboard.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
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

    // SWITCHED BACK TO TICKET REPOSITORY
    @Autowired
    private TicketRepository ticketRepository;

    @Autowired
    private UserRepository userRepository;

    @GetMapping("/tickets")
    @PreAuthorize("hasAuthority('ROLE_SUPER_ADMIN')")
    public String showManagerTicketDashboard(Model model, Principal principal) {

        // Fetching ACTUAL Client Tickets now
        List<Ticket> pendingRequests = ticketRepository.findByWorkflowStateIsNullOrWorkflowStateOrderByIdDesc("UNASSIGNED");

        List<User> deliveryManagers = userRepository.findAll().stream()
                .filter(u -> u.getRole() != null && "ROLE_DELIVERY_MANAGER".equals(u.getRole().getRoleName()))
                .collect(Collectors.toList());

        model.addAttribute("tickets", pendingRequests);
        model.addAttribute("unassignedCount", pendingRequests.size());
        model.addAttribute("deliveryManagers", deliveryManagers);

        return "admin-manage-client-tickets";
    }
}