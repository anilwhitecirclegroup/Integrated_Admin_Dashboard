package com.example.admindashboard.controller;

import com.example.admindashboard.model.Attendance;
import com.example.admindashboard.model.User;
import com.example.admindashboard.repository.AttendanceRepository;
import com.example.admindashboard.repository.UserRepository;
import com.example.admindashboard.service.EmailService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/admin/regularization")
public class AdminRegularizationController {

    @Autowired
    private AttendanceRepository attendanceRepository;

    // NEW: Inject UserRepository to check the current logged-in user's role and hierarchy
    @Autowired
    private UserRepository userRepository;

    @Autowired
    private EmailService emailService;

    // 1. Get List of Requests
    // LOCK: Restrict access to authorized leadership roles
    @PreAuthorize("hasAnyAuthority('ROLE_SUPER_ADMIN', 'ROLE_HR_ADMIN', 'ROLE_HR_EXECUTIVE', 'ROLE_MANAGER')")
    @GetMapping("/list")
    public ResponseEntity<List<Attendance>> getRequests(@RequestParam String status, Principal principal) {

        User currentUser = userRepository.findByUsername(principal.getName()).orElse(null);
        boolean isManager = currentUser != null && currentUser.getRole() != null && "MANAGER".equalsIgnoreCase(currentUser.getRole().getRoleName());

        List<Attendance> allRequests = attendanceRepository.findByApprovalStatusIgnoreCase(status);

        // DATA VISIBILITY FILTER: Managers only see requests from their own team members
        if (isManager && currentUser != null) {
            allRequests = allRequests.stream()
                    .filter(req -> req.getUser() != null
                            && req.getUser().getManager() != null
                            && req.getUser().getManager().getId().equals(currentUser.getId()))
                    .collect(Collectors.toList());
        }

        return ResponseEntity.ok(allRequests);
    }

    // 2. Approve or Reject Request
    // LOCK: Restrict access to authorized leadership roles
    @PreAuthorize("hasAnyAuthority('ROLE_SUPER_ADMIN', 'ROLE_HR_ADMIN', 'ROLE_HR_EXECUTIVE', 'ROLE_MANAGER')")
    @PostMapping("/{id}/{status}")
    public ResponseEntity<?> updateStatus(
            @PathVariable Long id,
            @PathVariable String status,
            Principal principal) {

        User currentUser = userRepository.findByUsername(principal.getName()).orElseThrow();
        boolean isManager = currentUser.getRole() != null && "MANAGER".equalsIgnoreCase(currentUser.getRole().getRoleName());

        Optional<Attendance> requestOpt = attendanceRepository.findById(id);

        if (requestOpt.isPresent()) {
            Attendance req = requestOpt.get();

            // CRITICAL SECURITY BLOCK: Prevent Manager from modifying out-of-team attendance via API bypass
            if (isManager && (req.getUser().getManager() == null || !req.getUser().getManager().getId().equals(currentUser.getId()))) {
                return ResponseEntity.status(403).body("Error: 403 Forbidden. You are not authorized to modify attendance for employees outside your reporting hierarchy.");
            }

            // Format cleanly to "Approved" or "Denied"
            String formattedStatus = status.substring(0, 1).toUpperCase() + status.substring(1).toLowerCase();

            // Save it back to the correct ApprovalStatus column
            req.setApprovalStatus(formattedStatus);

            attendanceRepository.save(req);

            // --- EMAIL TRIGGER START ---
            try {
                Map<String, Object> emailData = new HashMap<>();
                emailData.put("empName", req.getUser().getFullName());

                if (req.getPresentDays() != null && req.getAbsentDays() != null) {
                    emailData.put("specificType", req.getPresentDays() + " Present | " + req.getAbsentDays() + " Absent");
                }
                if (req.getSubmittedOn() != null) {
                    emailData.put("submittedOn", req.getSubmittedOn());
                }
                if (req.getWeekStartDate() != null && req.getWeekEndDate() != null) {
                    emailData.put("duration", req.getWeekStartDate() + " to " + req.getWeekEndDate() + " (" + req.getTotalHours() + " hrs)");
                }

                emailService.sendRequestStatusUpdateToEmployee(
                        req.getUser().getEmail(),
                        req.getUser().getFullName(),
                        "Attendance Regularization",
                        formattedStatus,
                        emailData
                );
            } catch (Exception e) {
                System.err.println("⚠️ Warning: Could not trigger Attendance Regularization email: " + e.getMessage());
            }
            // --- EMAIL TRIGGER END ---

            return ResponseEntity.ok("Request updated successfully");
        } else {
            return ResponseEntity.notFound().build();
        }
    }
}