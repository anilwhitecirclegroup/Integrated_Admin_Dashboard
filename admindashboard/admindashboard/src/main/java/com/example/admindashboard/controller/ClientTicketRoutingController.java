package com.example.admindashboard.controller;

import com.example.admindashboard.model.ServiceRequest;
import com.example.admindashboard.service.ServiceRequestRoutingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/routing")
public class ClientTicketRoutingController {

    @Autowired
    private ServiceRequestRoutingService routingService;

    // =========================================================================
    // 1. DYNAMIC INBOX: Fetches tickets based on the logged-in user's role
    // =========================================================================
    @GetMapping("/inbox")
    public ResponseEntity<List<ServiceRequest>> getMyInbox(Principal principal) {
        // The principal automatically holds the currently logged-in user's ID/Username
        String username = principal.getName();

        // Let our smart service figure out what this specific user is allowed to see
        List<ServiceRequest> inbox = routingService.getInboxForUser(username);

        return ResponseEntity.ok(inbox);
    }

    // =========================================================================
    // 2. TICKET HAND-OFF: Moves the ticket down the hierarchy
    // =========================================================================
    @PostMapping("/assign")
    @PreAuthorize("hasAnyAuthority('ROLE_SUPER_ADMIN', 'ROLE_DELIVERY_MANAGER', 'ROLE_PROJECT_MANAGER', 'ROLE_TEAM_LEAD')")
    public ResponseEntity<?> assignTicket(@RequestBody Map<String, Object> payload) {
        try {
            // Extract the assignment details from the frontend JSON payload
            Long ticketId = Long.valueOf(payload.get("ticketId").toString());
            String targetUsername = payload.get("targetUsername").toString();
            String targetRole = payload.get("targetRole").toString();

            // Execute the hand-off!
            ServiceRequest updatedTicket = routingService.routeTicket(ticketId, targetUsername, targetRole);

            return ResponseEntity.ok(updatedTicket);

        } catch (Exception e) {
            // If the target user doesn't exist, or they pass a bad role, catch it safely
            return ResponseEntity.badRequest().body("Error routing ticket: " + e.getMessage());
        }
    }
}