package com.example.admindashboard.controller;

import com.example.admindashboard.model.ServiceRequest;
import com.example.admindashboard.repository.ServiceRequestRepository;
import com.example.admindashboard.service.EmailService;
import com.example.admindashboard.model.User;
import com.example.admindashboard.repository.UserRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.security.Principal;

@RestController
@RequestMapping("/api/requests")
public class RequestApiController {

    @Autowired
    private ServiceRequestRepository repository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private EmailService emailService;

    // SELF-SERVICE: Any authenticated user can submit a ticket. No strict RBAC lock needed.
    @PostMapping("/submit")
    public ResponseEntity<?> submitRequest(@RequestBody ServiceRequest request) {
        // 1. Set the date and save the ticket to the database
        request.setSubmissionDate(LocalDate.now());
        ServiceRequest savedRequest = repository.save(request);

        // 2. NEW: ASYNC EMAIL TRIGGER FOR IT TICKETS
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String employeeEmail = "no-reply@whitecircle.com";
            String employeeName = savedRequest.getEmployeeName();

            if (authentication != null && authentication.isAuthenticated() && !"anonymousUser".equals(authentication.getPrincipal())) {
                Optional<User> currentUserOpt = userRepository.findByUsername(authentication.getName());
                if (currentUserOpt.isPresent()) {
                    employeeEmail = currentUserOpt.get().getEmail();
                    if (employeeName == null || employeeName.isEmpty()) {
                        employeeName = currentUserOpt.get().getFullName();
                    }
                }
            }

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

            emailService.sendTicketSubmissionToAdmin(
                    adminEmail,
                    employeeName,
                    employeeEmail,
                    savedRequest.getType(),
                    emailData
            );
        } catch (Exception e) {
            System.err.println("Non-fatal error: Failed to trigger IT ticket email - " + e.getMessage());
        }

        return ResponseEntity.ok(savedRequest);
    }

    // LOCK: An employee can only fetch their OWN tickets lists, unless they are an Admin.
    @PreAuthorize("#empId == authentication.name or hasAnyAuthority('ROLE_SUPER_ADMIN', 'ROLE_HR_ADMIN', 'ROLE_IT_ADMIN')")
    @GetMapping("/active/{empId}")
    public List<ServiceRequest> getEmployeeRequests(@PathVariable String empId) {
        return repository.findByEmployeeIdOrderBySubmissionDateDesc(empId);
    }

    // LOCK: Only Admins can update the status of a ticket.
    @PreAuthorize("hasAnyAuthority('ROLE_SUPER_ADMIN', 'ROLE_HR_ADMIN', 'ROLE_IT_ADMIN')")
    @PostMapping("/update-status")
    public ResponseEntity<?> updateStatus(@RequestParam Long id, @RequestParam String status) {
        ServiceRequest request = repository.findById(id).orElseThrow();
        request.setStatus(status);
        repository.save(request);

        // --- EMAIL TRIGGER START ---
        try {
            Optional<User> userOpt = userRepository.findByUsername(request.getEmployeeId());
            String employeeEmail = "no-reply@whitecircle.com";

            if (userOpt.isPresent() && userOpt.get().getEmail() != null) {
                employeeEmail = userOpt.get().getEmail();
            }

            Map<String, Object> emailData = new HashMap<>();
            emailData.put("empName", request.getEmployeeName());

            String specificType = (request.getTicketId() != null ? request.getTicketId() : "Ticket") +
                    (request.getCategory() != null ? " - " + request.getCategory() : "");
            emailData.put("specificType", specificType);

            if (request.getSubmissionDate() != null) {
                emailData.put("submittedOn", request.getSubmissionDate());
            }

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

    // FIXED LOCK: Horizontal Security (Ownership Check)
    @GetMapping("/{id}")
    public ResponseEntity<ServiceRequest> getRequestById(@PathVariable Long id, Principal principal) {
        Optional<ServiceRequest> requestOpt = repository.findById(id);

        if (requestOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        ServiceRequest request = requestOpt.get();
        String currentUsername = principal.getName();

        // Find the currently logged-in user to check their roles
        User currentUser = userRepository.findByUsername(currentUsername).orElse(null);
        boolean isAdmin = false;

        if (currentUser != null && currentUser.getRole() != null) {
            String roleName = currentUser.getRole().getRoleName();
            isAdmin = roleName.equals("SUPER_ADMIN") ||
                    roleName.equals("HR_ADMIN") ||
                    roleName.equals("IT_ADMIN");
        }

        // OWNERSHIP CHECK: Are you an admin? OR Are you the creator of this ticket?
        if (isAdmin || request.getEmployeeId().equals(currentUsername)) {
            return ResponseEntity.ok(request);
        } else {
            // If they are neither, explicitly block them.
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
    }
}