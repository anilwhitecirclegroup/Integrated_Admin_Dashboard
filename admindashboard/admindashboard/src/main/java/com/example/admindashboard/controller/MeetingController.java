package com.example.admindashboard.controller;

import com.example.admindashboard.model.Meeting;
import com.example.admindashboard.model.User;
import com.example.admindashboard.repository.MeetingRepository;
import com.example.admindashboard.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.security.Principal;

@RestController
@RequestMapping("/api/meetings")
public class MeetingController {

    @Autowired
    private MeetingRepository meetingRepository;

    @Autowired
    private UserRepository userRepository;

    @PostMapping("/book")
    public ResponseEntity<?> bookMeeting(@RequestBody Meeting meeting, Principal principal) {
        try {
            // 1. Identify who is logged in and booking the room
            String username = principal.getName();
            User organizer = userRepository.findByUsername(username)
                    .orElseThrow(() -> new RuntimeException("User not found"));

            // 2. Attach the organizer to the meeting
            meeting.setOrganizer(organizer);

            // 3. Save to the PostgreSQL database
            Meeting savedMeeting = meetingRepository.save(meeting);

            return ResponseEntity.ok("Meeting booked successfully!");

        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error booking meeting: " + e.getMessage());
        }
    }
}