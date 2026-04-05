package com.example.admindashboard.controller;

import com.example.admindashboard.model.User;
import com.example.admindashboard.model.WeeklyTimesheet;
import com.example.admindashboard.repository.UserRepository;
import com.example.admindashboard.repository.WeeklyTimesheetRepository;
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
@RequestMapping("/api/admin/timesheet")
public class AdminTimesheetController {

    @Autowired
    private WeeklyTimesheetRepository timesheetRepository;

    // NEW: Inject UserRepository for role and hierarchy validation
    @Autowired
    private UserRepository userRepository;

    @Autowired
    private EmailService emailService;

    // 1. Fetch timesheets by status
    // LOCK: Restrict access to authorized leadership roles
    @PreAuthorize("hasAnyAuthority('ROLE_SUPER_ADMIN', 'ROLE_HR_ADMIN', 'ROLE_MANAGER')")
    @GetMapping("/list")
    public ResponseEntity<List<WeeklyTimesheet>> getTimesheets(@RequestParam String status, Principal principal) {

        User currentUser = userRepository.findByUsername(principal.getName()).orElse(null);
        boolean isManager = currentUser != null && currentUser.getRole() != null && "MANAGER".equalsIgnoreCase(currentUser.getRole().getRoleName());

        List<WeeklyTimesheet> allTimesheets = timesheetRepository.findByStatus(status);

        // DATA VISIBILITY FILTER: Managers only see timesheets submitted by their team
        if (isManager && currentUser != null) {
            allTimesheets = allTimesheets.stream()
                    .filter(ts -> ts.getUser() != null
                            && ts.getUser().getManager() != null
                            && ts.getUser().getManager().getId().equals(currentUser.getId()))
                    .collect(Collectors.toList());
        }

        return ResponseEntity.ok(allTimesheets);
    }

    // 2. Approve or Reject
    // LOCK: Restrict access to authorized leadership roles
    @PreAuthorize("hasAnyAuthority('ROLE_SUPER_ADMIN', 'ROLE_HR_ADMIN', 'ROLE_MANAGER')")
    @PostMapping("/{id}/{status}")
    public ResponseEntity<?> updateStatus(
            @PathVariable Long id,
            @PathVariable String status,
            @RequestParam(required = false) String comments,
            Principal principal) {

        User currentUser = userRepository.findByUsername(principal.getName()).orElseThrow();
        boolean isManager = currentUser.getRole() != null && "MANAGER".equalsIgnoreCase(currentUser.getRole().getRoleName());

        Optional<WeeklyTimesheet> tsOpt = timesheetRepository.findById(id);

        if (tsOpt.isPresent()) {
            WeeklyTimesheet ts = tsOpt.get();

            // CRITICAL SECURITY BLOCK: Prevent Manager from modifying out-of-team timesheets via API bypass
            if (isManager && (ts.getUser().getManager() == null || !ts.getUser().getManager().getId().equals(currentUser.getId()))) {
                return ResponseEntity.status(403).body("Error: 403 Forbidden. You are not authorized to evaluate timesheets for employees outside your reporting hierarchy.");
            }

            ts.setStatus(status);

            if (comments != null && !comments.isEmpty()) {
                ts.setOverallComments(comments);
            }
            timesheetRepository.save(ts);

            // --- EMAIL TRIGGER START ---
            try {
                Map<String, Object> emailData = new HashMap<>();
                emailData.put("empName", ts.getUser().getFullName());
                emailData.put("specificType", "Total Hours Logged: " + ts.getTotalWeekHours() + " hrs");

                if (ts.getSubmissionDate() != null) {
                    emailData.put("submittedOn", ts.getSubmissionDate());
                } else if (ts.getSubmittedAt() != null) {
                    emailData.put("submittedOn", ts.getSubmittedAt().toLocalDate());
                }

                emailData.put("duration", ts.getWeekStartDate() + " to " + ts.getWeekEndDate());

                if (comments != null && !comments.isEmpty()) {
                    emailData.put("adminComments", comments);
                }

                emailService.sendRequestStatusUpdateToEmployee(
                        ts.getUser().getEmail(),
                        ts.getUser().getFullName(),
                        "Timesheet",
                        status,
                        emailData
                );
            } catch (Exception e) {
                System.err.println("⚠️ Warning: Could not trigger Timesheet email: " + e.getMessage());
            }
            // --- EMAIL TRIGGER END ---

            return ResponseEntity.ok("Timesheet " + status);
        }
        return ResponseEntity.notFound().build();
    }
}