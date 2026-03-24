package com.example.admindashboard.controller;

import com.example.admindashboard.model.LeaveRequest;
import com.example.admindashboard.repository.LeaveRequestRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import java.security.Principal;
import java.util.List;

@Controller
public class AdminLeaveController {

    @Autowired
    private LeaveRequestRepository leaveRequestRepository;

    // 1. LOAD THE ADMIN DASHBOARD PAGE
    @GetMapping("/admin/leave-approvals")
    public String showLeaveApprovals(Model model) {

        // Fetch leaves grouped by status for the 3 tabs
        List<LeaveRequest> pending = leaveRequestRepository.findByStatus("Pending");
        List<LeaveRequest> approved = leaveRequestRepository.findByStatus("Approved");
        List<LeaveRequest> rejected = leaveRequestRepository.findByStatus("Rejected");

        model.addAttribute("pendingLeaves", pending);
        model.addAttribute("approvedLeaves", approved);
        model.addAttribute("rejectedLeaves", rejected);

        return "admin-leave-approvals";
    }

    // 2. HANDLE APPROVAL (Called by JavaScript)
    @PostMapping("/api/admin/leave/approve/{id}")
    @ResponseBody
    public ResponseEntity<String> approveLeave(@PathVariable Long id, Principal principal) {
        try {
            LeaveRequest leave = leaveRequestRepository.findById(id)
                    .orElseThrow(() -> new IllegalArgumentException("Invalid leave Id:" + id));

            leave.setStatus("Approved");

            // Optional: Track who approved it (if you added an approvedBy field to LeaveRequest)
            // if (principal != null) {
            //     leave.setApprovedBy(principal.getName());
            // }

            leaveRequestRepository.save(leave);
            return ResponseEntity.ok("Leave Approved successfully");

        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error: " + e.getMessage());
        }
    }

    // 3. HANDLE REJECTION & NOTES (Called by JS)
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
            leave.setAdminComments(note);

            leaveRequestRepository.save(leave);
            return ResponseEntity.ok("Leave Rejected successfully");

        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error: " + e.getMessage());
        }
    }
}