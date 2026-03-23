package com.example.admindashboard.controller;

import java.time.LocalDate;
import java.util.List;
import java.util.HashMap;
import java.util.Map;
import com.example.admindashboard.model.LeaveRequest;
import com.example.admindashboard.model.User;
import com.example.admindashboard.repository.LeaveRequestRepository;
import com.example.admindashboard.repository.UserRepository;
import com.example.admindashboard.service.LeaveRequestService;
import com.example.admindashboard.service.EmailService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;

@RestController
@RequestMapping("/api/leave")
public class LeaveController {

    // --- ALL DEPENDENCIES AT THE TOP ---
    @Autowired
    private LeaveRequestRepository leaveRequestRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private LeaveRequestService leaveRequestService;

    @Autowired
    private EmailService emailService;

    // 1. SUBMIT LEAVE REQUEST
    @PostMapping("/submit")
    public ResponseEntity<?> submitLeaveRequest(@RequestBody LeaveRequest leaveRequest, Principal principal) {
        try {
            // 1. Find the currently logged-in employee
            String username = principal.getName();
            User currentUser = userRepository.findByUsername(username)
                    .orElseThrow(() -> new RuntimeException("User not found"));

            // 2. Attach the employee to the request and set status to "Pending"
            leaveRequest.setUser(currentUser);
            leaveRequest.setStatus("Pending");

            // 3. Automatically stamp today's date on the request before saving
            leaveRequest.setCreatedAt(LocalDate.now());

            // 4. Save it to the PostgreSQL database
            LeaveRequest savedRequest = leaveRequestRepository.save(leaveRequest);

            // 5. NEW: ASYNC EMAIL TRIGGER
            try {
                // Map the data exactly as it appears in your HTML template variables
                Map<String, Object> emailData = new HashMap<>();
                emailData.put("empName", currentUser.getFullName());
                emailData.put("empId", currentUser.getUsername());
                emailData.put("leaveType", savedRequest.getLeaveType());
                emailData.put("fromDate", savedRequest.getFromDate());
                emailData.put("toDate", savedRequest.getToDate());
                emailData.put("totalDays", savedRequest.getTotalDays());
                emailData.put("reason", savedRequest.getReason());

                // TODO: Replace with your actual Admin/HR email receiving the requests
                String adminEmail = "arcthunder07@gmail.com";

                // Fire and forget! The @Async service handles this in the background
                emailService.sendLeaveRequestToAdmin(adminEmail, currentUser.getFullName(), currentUser.getEmail(), emailData);
            } catch (Exception e) {
                // We catch exceptions here so an email failure doesn't break the actual leave submission!
                System.err.println("Non-fatal error: Failed to trigger admin email - " + e.getMessage());
            }


            return ResponseEntity.ok(savedRequest);

        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error submitting leave: " + e.getMessage());
        }
    }

    // 2. FETCH RECENT LEAVES (For the UI Card)
    @GetMapping("/my-leaves")
    public ResponseEntity<?> getMyRecentLeaves(Principal principal) {
        try {
            List<LeaveRequest> myLeaves = leaveRequestService.getMyLeaves(principal.getName());
            return ResponseEntity.ok(myLeaves);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error fetching leave history: " + e.getMessage());
        }
    }

    // 3. CALCULATE LEAVE BALANCE DYNAMICALLY
    @GetMapping("/balance")
    public ResponseEntity<?> getLeaveBalance(Principal principal) {
        try {
            String username = principal.getName();
            User currentUser = userRepository.findByUsername(username)
                    .orElseThrow(() -> new RuntimeException("User not found"));

            List<LeaveRequest> allLeaves = leaveRequestRepository.findByUserOrderByIdDesc(currentUser);

            int totalCL = 12;
            int totalSL = 10;
            int totalEL = 20;

            int usedCL = 0, usedSL = 0, usedEL = 0;

            for (LeaveRequest leave : allLeaves) {
                if ("APPROVED".equalsIgnoreCase(leave.getStatus())) {
                    if ("Casual".equalsIgnoreCase(leave.getLeaveType())) {
                        usedCL += leave.getTotalDays();
                    } else if ("Sick".equalsIgnoreCase(leave.getLeaveType())) {
                        usedSL += leave.getTotalDays();
                    } else if ("Earned".equalsIgnoreCase(leave.getLeaveType())) {
                        usedEL += leave.getTotalDays();
                    }
                }
            }

            Map<String, Map<String, Integer>> balances = new HashMap<>();
            balances.put("Casual", Map.of("used", usedCL, "total", totalCL, "left", totalCL - usedCL));
            balances.put("Sick", Map.of("used", usedSL, "total", totalSL, "left", totalSL - usedSL));
            balances.put("Earned", Map.of("used", usedEL, "total", totalEL, "left", totalEL - usedEL));

            return ResponseEntity.ok(balances);

        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error calculating balance: " + e.getMessage());
        }
    }
}