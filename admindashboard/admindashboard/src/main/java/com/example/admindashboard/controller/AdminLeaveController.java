package com.example.admindashboard.controller;

import com.example.admindashboard.model.LeaveRequest;
import com.example.admindashboard.repository.LeaveRequestRepository;
import com.example.admindashboard.service.EmailService; // Import the Email Service
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import java.security.Principal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
public class AdminLeaveController {

    @Autowired
    private LeaveRequestRepository leaveRequestRepository;

    // 1. INJECT THE EMAIL SERVICE
    @Autowired
    private EmailService emailService;

    // 2. LOAD THE ADMIN DASHBOARD PAGE
    @GetMapping("/admin/leave-approvals")
    public String showLeaveApprovals(Model model) {
        List<LeaveRequest> pending = leaveRequestRepository.findByStatus("Pending");
        List<LeaveRequest> approved = leaveRequestRepository.findByStatus("Approved");
        List<LeaveRequest> rejected = leaveRequestRepository.findByStatus("Rejected");

        model.addAttribute("pendingLeaves", pending);
        model.addAttribute("approvedLeaves", approved);
        model.addAttribute("rejectedLeaves", rejected);

        return "admin-leave-approvals";
    }

    // 3. HANDLE APPROVAL (Called by JavaScript)
    @PostMapping("/api/admin/leave/approve/{id}")
    @ResponseBody
    public ResponseEntity<String> approveLeave(@PathVariable Long id, Principal principal) {
        try {
            LeaveRequest leave = leaveRequestRepository.findById(id)
                    .orElseThrow(() -> new IllegalArgumentException("Invalid leave Id:" + id));

            leave.setStatus("Approved");
            leaveRequestRepository.save(leave);

            // --- EMAIL TRIGGER START ---
            Map<String, Object> emailData = new HashMap<>();
            // No admin comments needed for standard approval, but the map must be passed

            // 1. Fix the missing name
            emailData.put("empName", leave.getUser().getFullName());

            // 2. Add the new specific details
            emailData.put("specificType", leave.getLeaveType());
            emailData.put("submittedOn", leave.getCreatedAt());
            emailData.put("duration", leave.getFromDate() + " to " + leave.getToDate() + " (" + leave.getTotalDays() + " Days)");

            emailService.sendRequestStatusUpdateToEmployee(
                    leave.getUser().getEmail(),
                    leave.getUser().getFullName(),
                    "Leave",     // The type of request
                    "Approved",  // The status
                    emailData
            );
            // --- EMAIL TRIGGER END ---

            return ResponseEntity.ok("Leave Approved successfully");

        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error: " + e.getMessage());
        }
    }

    // 4. HANDLE REJECTION & NOTES (Called by JS)
    @PostMapping("/api/admin/leave/reject/{id}")
    @ResponseBody
    public ResponseEntity<String> rejectLeave(
            @PathVariable Long id,
            @RequestParam(value = "note", required = false) String note,
            Principal principal) {

        try {
            LeaveRequest leave = leaveRequestRepository.findById(id)
                    .orElseThrow(() -> new IllegalArgumentException("Invalid leave Id:" + id));

            leave.setStatus("Rejected");
            leave.setAdminComments(note); // Save note to database

            leaveRequestRepository.save(leave);

            // --- EMAIL TRIGGER START ---
            Map<String, Object> emailData = new HashMap<>();

            emailData.put("empName", leave.getUser().getFullName());
            emailData.put("specificType", leave.getLeaveType());
            emailData.put("submittedOn", leave.getCreatedAt());
            emailData.put("duration", leave.getFromDate() + " to " + leave.getToDate() + " (" + leave.getTotalDays() + " Days)");
            emailData.put("adminComments", note); // The rejection reason

            emailService.sendRequestStatusUpdateToEmployee(
                    leave.getUser().getEmail(),
                    leave.getUser().getFullName(),
                    "Leave",     // The type of request
                    "Rejected",  // The status
                    emailData
            );
            // --- EMAIL TRIGGER END ---

            return ResponseEntity.ok("Leave Rejected successfully");

        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error: " + e.getMessage());
        }
    }
}