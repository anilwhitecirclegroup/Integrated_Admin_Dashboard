package com.example.admindashboard.controller;

import com.example.admindashboard.model.WeeklyTimesheet;
import com.example.admindashboard.repository.WeeklyTimesheetRepository;
import com.example.admindashboard.service.EmailService; // Import the Email Service
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/admin/timesheet")
public class AdminTimesheetController {

    @Autowired
    private WeeklyTimesheetRepository timesheetRepository;

    // 1. INJECT THE EMAIL SERVICE
    @Autowired
    private EmailService emailService;

    // 1. Fetch timesheets by status (SUBMITTED, APPROVED, REJECTED)
    @GetMapping("/list")
    public ResponseEntity<List<WeeklyTimesheet>> getTimesheets(@RequestParam String status) {
        // Note: Using "SUBMITTED" to match your model's status for pending items
        return ResponseEntity.ok(timesheetRepository.findByStatus(status));
    }

    // 2. Approve or Reject
    @PostMapping("/{id}/{status}")
    public ResponseEntity<?> updateStatus(
            @PathVariable Long id,
            @PathVariable String status,
            @RequestParam(required = false) String comments) {

        Optional<WeeklyTimesheet> tsOpt = timesheetRepository.findById(id);

        if (tsOpt.isPresent()) {
            WeeklyTimesheet ts = tsOpt.get();
            ts.setStatus(status);

            if (comments != null && !comments.isEmpty()) {
                ts.setOverallComments(comments);
            }
            timesheetRepository.save(ts);

            // --- EMAIL TRIGGER START ---
            try {
                Map<String, Object> emailData = new HashMap<>();

                // 1. Employee Name
                emailData.put("empName", ts.getUser().getFullName());

                // 2. Cleverly use the "Specific Type" row to show Total Hours
                emailData.put("specificType", "Total Hours Logged: " + ts.getTotalWeekHours() + " hrs");

                // 3. Fallback logic for submitted date (handles if one field is empty)
                if (ts.getSubmissionDate() != null) {
                    emailData.put("submittedOn", ts.getSubmissionDate());
                } else if (ts.getSubmittedAt() != null) {
                    emailData.put("submittedOn", ts.getSubmittedAt().toLocalDate());
                }

                // 4. Duration
                emailData.put("duration", ts.getWeekStartDate() + " to " + ts.getWeekEndDate());

                // 5. Admin Comments (Only added if provided during a rejection/approval)
                if (comments != null && !comments.isEmpty()) {
                    emailData.put("adminComments", comments);
                }

                emailService.sendRequestStatusUpdateToEmployee(
                        ts.getUser().getEmail(),
                        ts.getUser().getFullName(),
                        "Timesheet", // The request type
                        status,      // Dynamically passes "APPROVED" or "REJECTED" from the URL
                        emailData
                );
            } catch (Exception e) {
                // Failsafe: Ensures that if an employee is missing an email in the DB,
                // the system still successfully approves the timesheet without crashing.
                System.err.println("⚠️ Warning: Could not trigger Timesheet email: " + e.getMessage());
            }
            // --- EMAIL TRIGGER END ---

            return ResponseEntity.ok("Timesheet " + status);
        }
        return ResponseEntity.notFound().build();
    }
}