package com.example.admindashboard.controller;

import com.example.admindashboard.model.Meeting;
import com.example.admindashboard.model.User;
import com.example.admindashboard.repository.MeetingRepository;
import com.example.admindashboard.repository.UserRepository;
import com.example.admindashboard.service.EmailService; // Added Email Service
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.HashMap;
import java.util.Map;
import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/api/meetings")
public class MeetingController {

    @Autowired
    private MeetingRepository meetingRepository;

    @Autowired
    private UserRepository userRepository;

    // INJECT THE EMAIL SERVICE
    @Autowired
    private EmailService emailService;

    @PostMapping("/book")
    public ResponseEntity<?> bookMeeting(@RequestBody Meeting meeting, Principal principal) {
        try {
            // 1. Identify who is logged in and booking the room
            String username = principal.getName();
            User organizer = userRepository.findByUsername(username)
                    .orElseThrow(() -> new RuntimeException("User not found"));

            // 2. Attach the organizer to the meeting
            meeting.setOrganizer(organizer);

         // 3. Validate specific employee IDs before saving meeting
            if (meeting.getSpecificEmployeeIds() != null && !meeting.getSpecificEmployeeIds().trim().isEmpty()) {

                String[] invitedIds = meeting.getSpecificEmployeeIds().split(",");
                List<String> invalidEmployeeIds = new ArrayList<>();

                for (String empId : invitedIds) {
                    String cleanedEmpId = empId.trim();

                    if (cleanedEmpId.isEmpty()) {
                        continue;
                    }

                    boolean employeeExists = userRepository.findByUsername(cleanedEmpId).isPresent();

                    if (!employeeExists) {
                    	invalidEmployeeIds.add(cleanedEmpId);
                    }
                }

                if (!invalidEmployeeIds.isEmpty()) {
                    return ResponseEntity.badRequest().body(
                            "Invalid Employee ID(s): " + String.join(", ", invalidEmployeeIds)
                    );
                }
            }

            // 4. Save to the PostgreSQL database only after validation passes
            Meeting savedMeeting = meetingRepository.save(meeting);

            // --- ASYNC EMAIL TRIGGER START ---
            try {
                // Package the meeting details for the HTML template
                Map<String, Object> emailData = new HashMap<>();
                emailData.put("meetingTitle", savedMeeting.getMeetingTitle());
                emailData.put("meetingDate", savedMeeting.getMeetingDate());
                emailData.put("startTime", savedMeeting.getStartTime());
                emailData.put("endTime", savedMeeting.getEndTime());
                emailData.put("meetingMode", savedMeeting.getMeetingMode());
                emailData.put("platform", savedMeeting.getPlatform() != null ? savedMeeting.getPlatform() : "TBD");
                emailData.put("organizerName", organizer.getFullName());
                emailData.put("meetingLink", savedMeeting.getMeetingLink());

                // Check if specific employees were invited
                if (savedMeeting.getSpecificEmployeeIds() != null && !savedMeeting.getSpecificEmployeeIds().trim().isEmpty()) {

                    // Split the comma-separated string into an array (e.g., ["EMP001", "EMP002"])
                    String[] invitedIds = savedMeeting.getSpecificEmployeeIds().split(",");

                    // Loop through each ID, find them in the DB, and send the invite
                    for (String empId : invitedIds) {
                        userRepository.findByUsername(empId.trim()).ifPresent(invitee -> {
                            // Only send if they have a valid email setup
                            if (invitee.getEmail() != null && !invitee.getEmail().isEmpty()) {
                                emailService.sendMeetingInvite(
                                        invitee.getEmail(),
                                        invitee.getFullName(),
                                        savedMeeting.getMeetingTitle(),
                                        emailData
                                );
                            }
                        });
                    }
                }

                // Note: If you want to automatically email everyone in a BU when ParticipantType is "TEAM",
                // you can easily add an 'else if' block here later to fetch all users by BU and loop through them!

            } catch (Exception e) {
                System.err.println("⚠️ Warning: Could not send meeting invites: " + e.getMessage());
            }
            // --- ASYNC EMAIL TRIGGER END ---

            return ResponseEntity.ok("Meeting booked successfully!");

        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error booking meeting: " + e.getMessage());
        }
    }
}