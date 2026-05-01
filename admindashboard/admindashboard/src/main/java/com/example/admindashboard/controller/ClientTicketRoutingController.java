package com.example.admindashboard.controller;

import com.example.admindashboard.model.Ticket;
import com.example.admindashboard.service.ClientTicketRoutingService;
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
    private ClientTicketRoutingService routingService;

    // =========================================================================
    // 1. DYNAMIC INBOX: Fetches client tickets based on role
    // =========================================================================
    @GetMapping("/inbox")
    public ResponseEntity<List<Ticket>> getMyInbox(Principal principal) {
        String username = principal.getName();
        List<Ticket> inbox = routingService.getInboxForUser(username);
        return ResponseEntity.ok(inbox);
    }

    // =========================================================================
    // 2. TICKET HAND-OFF: Moves the client ticket down the hierarchy
    // =========================================================================
    @PostMapping("/assign")
    @PreAuthorize("hasAnyAuthority('ROLE_SUPER_ADMIN', 'ROLE_DELIVERY_MANAGER', 'ROLE_PROJECT_MANAGER', 'ROLE_TEAM_LEAD')")
    public ResponseEntity<?> assignTicket(@RequestBody Map<String, Object> payload) {
        try {
            Long ticketId = Long.valueOf(payload.get("ticketId").toString());
            String targetUsername = payload.get("targetUsername").toString();
            String targetRole = payload.get("targetRole").toString();

            Ticket updatedTicket = routingService.routeTicket(ticketId, targetUsername, targetRole);

            return ResponseEntity.ok(updatedTicket);

        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error routing ticket: " + e.getMessage());
        }
    }
}