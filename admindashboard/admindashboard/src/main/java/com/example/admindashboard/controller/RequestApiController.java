package com.example.admindashboard.controller;

// I fixed your imports to match your project structure!
import com.example.admindashboard.model.ServiceRequest;
import com.example.admindashboard.repository.ServiceRequestRepository;
import com.example.admindashboard.service.EmailService;
import com.example.admindashboard.model.User;
import com.example.admindashboard.repository.UserRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/requests")
public class RequestApiController {

    @Autowired
    private ServiceRequestRepository repository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private EmailService emailService;

    @PostMapping("/submit")
    public ResponseEntity<?> submitRequest(@RequestBody ServiceRequest request) {
        // 1. Set the date and save the ticket to the database
        request.setSubmissionDate(LocalDate.now());
        ServiceRequest savedRequest = repository.save(request);

        // 2. NEW: ASYNC EMAIL TRIGGER FOR IT TICKETS
        try {
            // Find out who submitted the ticket to grab their official email
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String employeeEmail = "no-reply@whitecircle.com"; // Fallback email
            String employeeName = savedRequest.getEmployeeName();

            if (authentication != null && authentication.isAuthenticated() && !"anonymousUser".equals(authentication.getPrincipal())) {
                Optional<User> currentUserOpt = userRepository.findByUsername(authentication.getName());
                if (currentUserOpt.isPresent()) {
                    employeeEmail = currentUserOpt.get().getEmail();
                    // Fallback to logged-in user's name if not provided in request
                    if (employeeName == null || employeeName.isEmpty()) {
                        employeeName = currentUserOpt.get().getFullName();
                    }
                }
            }

            // Package the data exactly as our HTML template expects
            Map<String, Object> emailData = new HashMap<>();
            emailData.put("empName", employeeName);
            emailData.put("ticketId", savedRequest.getTicketId() != null ? savedRequest.getTicketId() : "TKT-NEW");
            emailData.put("type", savedRequest.getType());
            emailData.put("priority", savedRequest.getPriority());
            emailData.put("category", savedRequest.getCategory());
            emailData.put("detailItem", savedRequest.getDetailItem());
            emailData.put("justification", savedRequest.getJustification());

            // TODO: Replace with your actual admin testing email!
            String adminEmail = "arcthunder07@gmail.com";

            // Fire the background email!
            emailService.sendTicketSubmissionToAdmin(
                    adminEmail,
                    employeeName,
                    employeeEmail,
                    savedRequest.getType(),
                    emailData
            );
        } catch (Exception e) {
            // Catch errors silently so the frontend still shows "Ticket Submitted Successfully!"
            System.err.println("Non-fatal error: Failed to trigger IT ticket email - " + e.getMessage());
        }

        return ResponseEntity.ok(savedRequest);
    }

    @GetMapping("/active/{empId}")
    public List<ServiceRequest> getEmployeeRequests(@PathVariable String empId) {
        return repository.findByEmployeeIdOrderBySubmissionDateDesc(empId);
    }

    @PostMapping("/update-status")
    public ResponseEntity<?> updateStatus(@RequestParam Long id, @RequestParam String status) {
        ServiceRequest request = repository.findById(id).orElseThrow();
        request.setStatus(status);
        repository.save(request);

        // --- EMAIL TRIGGER START ---
        try {
            // 1. Look up the employee's email using the UserRepository since ServiceRequest isn't directly linked
            // We assume employeeId matches the username (e.g., "EMP001").
            // If your lookup method is different (like findByEmployeeId), update this line!
            Optional<User> userOpt = userRepository.findByUsername(request.getEmployeeId());
            String employeeEmail = "no-reply@whitecircle.com"; // Fallback if lookup fails

            if (userOpt.isPresent() && userOpt.get().getEmail() != null) {
                employeeEmail = userOpt.get().getEmail();
            }

            Map<String, Object> emailData = new HashMap<>();

            emailData.put("empName", request.getEmployeeName());

            // Format specific type cleanly (e.g., "HW-1042 - RAM Upgrade")
            String specificType = (request.getTicketId() != null ? request.getTicketId() : "Ticket") +
                    (request.getCategory() != null ? " - " + request.getCategory() : "");
            emailData.put("specificType", specificType);

            if (request.getSubmissionDate() != null) {
                emailData.put("submittedOn", request.getSubmissionDate());
            }

            // Create a dynamic title like "HARDWARE Ticket" or "SOFTWARE Ticket"
            String prettyRequestType = (request.getType() != null ? request.getType().toUpperCase() : "IT") + " Ticket";

            emailService.sendRequestStatusUpdateToEmployee(
                    employeeEmail,
                    request.getEmployeeName(),
                    prettyRequestType,
                    status,
                    emailData
            );
        } catch (Exception e) {
            System.err.println("⚠️ Warning: Could not trigger IT Ticket status email: " + e.getMessage());
        }
        // --- EMAIL TRIGGER END ---

        return ResponseEntity.ok("Status updated to " + status);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ServiceRequest> getRequestById(@PathVariable Long id) {
        return repository.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
}