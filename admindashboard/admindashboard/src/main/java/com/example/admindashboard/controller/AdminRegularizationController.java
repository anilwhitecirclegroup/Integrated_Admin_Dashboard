package com.example.admindashboard.controller;

import com.example.admindashboard.model.Attendance;
import com.example.admindashboard.repository.AttendanceRepository;
import com.example.admindashboard.service.EmailService; // Import the Email Service
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/admin/regularization")
public class AdminRegularizationController {

    @Autowired
    private AttendanceRepository attendanceRepository;

    // 1. INJECT THE EMAIL SERVICE
    @Autowired
    private EmailService emailService;

    // 1. Get List of Requests (Now searching the correct ApprovalStatus column!)
    @GetMapping("/list")
    public ResponseEntity<List<Attendance>> getRequests(@RequestParam String status) {
        return ResponseEntity.ok(attendanceRepository.findByApprovalStatusIgnoreCase(status));
    }

    // 2. Approve or Reject Request
    @PostMapping("/{id}/{status}")
    public ResponseEntity<?> updateStatus(
            @PathVariable Long id,
            @PathVariable String status) {

        Optional<Attendance> requestOpt = attendanceRepository.findById(id);

        if (requestOpt.isPresent()) {
            Attendance req = requestOpt.get();

            // Format cleanly to "Approved" or "Denied"
            String formattedStatus = status.substring(0, 1).toUpperCase() + status.substring(1).toLowerCase();

            // Save it back to the correct ApprovalStatus column
            req.setApprovalStatus(formattedStatus);

            attendanceRepository.save(req);

            // --- EMAIL TRIGGER START ---
            try {
                Map<String, Object> emailData = new HashMap<>();

                // 1. Employee Name
                emailData.put("empName", req.getUser().getFullName());

                // 2. Clever mapping for Attendance details
                if (req.getPresentDays() != null && req.getAbsentDays() != null) {
                    emailData.put("specificType", req.getPresentDays() + " Present | " + req.getAbsentDays() + " Absent");
                }

                // 3. Submitted Date
                if (req.getSubmittedOn() != null) {
                    emailData.put("submittedOn", req.getSubmittedOn());
                }

                // 4. Duration and Total Hours
                if (req.getWeekStartDate() != null && req.getWeekEndDate() != null) {
                    emailData.put("duration", req.getWeekStartDate() + " to " + req.getWeekEndDate() + " (" + req.getTotalHours() + " hrs)");
                }

                // Note: The regularization controller doesn't currently accept an 'admin comments' parameter
                // from the frontend, but if you ever add one, you can easily put it in this map!

                emailService.sendRequestStatusUpdateToEmployee(
                        req.getUser().getEmail(),
                        req.getUser().getFullName(),
                        "Attendance Regularization", // The request type
                        formattedStatus,             // Dynamically passes "Approved" or "Denied"
                        emailData
                );
            } catch (Exception e) {
                // Failsafe: Ensures that if an employee is missing an email in the DB,
                // the system still successfully processes the approval without crashing.
                System.err.println("⚠️ Warning: Could not trigger Attendance Regularization email: " + e.getMessage());
            }
            // --- EMAIL TRIGGER END ---

            return ResponseEntity.ok("Request updated successfully");
        } else {
            return ResponseEntity.notFound().build();
        }
    }
}