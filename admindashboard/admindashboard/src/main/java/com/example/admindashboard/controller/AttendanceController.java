package com.example.admindashboard.controller;

import com.example.admindashboard.service.AttendanceService;
import com.example.admindashboard.service.EmailService;
import com.example.admindashboard.model.User;
import com.example.admindashboard.repository.UserRepository;

// IMPORTANT: Update these two imports to match your exact Attendance model name if it differs!
import com.example.admindashboard.model.Attendance;
import com.example.admindashboard.repository.AttendanceRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/attendance")
public class AttendanceController {

    @Autowired
    private AttendanceService attendanceService;

    @Autowired
    private EmailService emailService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private AttendanceRepository attendanceRepository;

    @PostMapping("/save")
    public ResponseEntity<?> saveAttendanceDraft(@RequestBody java.util.Map<String, Object> attendanceData, Principal principal) {
        try {
            // We pass the raw data and the logged-in username to the service layer
            Object savedDraft = attendanceService.saveWeeklyDraft(attendanceData, principal.getName());
            return ResponseEntity.ok(savedDraft);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error saving draft: " + e.getMessage());
        }
    }

    // Submit for Approval
    @PostMapping("/submit/{id}")
    public ResponseEntity<?> submitAttendance(@PathVariable Long id, Principal principal) {
        try {
            // 1. Tells the service to change the status from "Draft" to "Pending"
            attendanceService.submitWeeklyAttendance(id, principal.getName());

            // 2. NEW: ASYNC EMAIL TRIGGER FOR ATTENDANCE
            try {
                String currentUsername = principal.getName();
                Optional<User> currentUserOpt = userRepository.findByUsername(currentUsername);

                // Fetch the submitted attendance record to grab the stats for the email
                Optional<Attendance> attendanceOpt = attendanceRepository.findById(id);

                if (currentUserOpt.isPresent() && attendanceOpt.isPresent()) {
                    User currentUser = currentUserOpt.get();
                    Attendance attendance = attendanceOpt.get();

                    // Package the data for our Thymeleaf HTML template
                    Map<String, Object> emailData = new HashMap<>();
                    emailData.put("empName", currentUser.getFullName());
                    emailData.put("empId", currentUser.getUsername());

                    // Note: Ensure these getter names match your actual Attendance model!
                    emailData.put("weekStartDate", attendance.getWeekStartDate());
                    emailData.put("weekEndDate", attendance.getWeekEndDate());
                    emailData.put("presentDays", attendance.getPresentDays());
                    emailData.put("absentDays", attendance.getAbsentDays());
                    emailData.put("totalHours", attendance.getTotalHours());
                    emailData.put("reason", attendance.getReason());

                    // TODO: Replace with your actual testing email address!
                    String adminEmail = "arcthunder07@gmail.com";

                    // Fire the background email!
                    emailService.sendAttendanceSubmissionToAdmin(
                            adminEmail,
                            currentUser.getFullName(),
                            currentUser.getEmail(),
                            emailData
                    );
                }
            } catch (Exception e) {
                // Catch errors silently so the frontend still shows "Successfully submitted"
                System.err.println("Non-fatal error: Failed to trigger attendance email - " + e.getMessage());
            }

            return ResponseEntity.ok("Successfully submitted for approval.");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error submitting record: " + e.getMessage());
        }
    }

    //  Get My Attendance History
    @GetMapping("/my-history")
    public ResponseEntity<?> getMyHistory(Principal principal) {
        try {
            return ResponseEntity.ok(attendanceService.getMyAttendanceHistory(principal.getName()));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error fetching history: " + e.getMessage());
        }
    }
}