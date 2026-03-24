package com.example.admindashboard.controller;

import com.example.admindashboard.model.Attendance;
import com.example.admindashboard.repository.AttendanceRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/admin/regularization")
public class AdminRegularizationController {

    @Autowired
    private AttendanceRepository attendanceRepository;

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
            return ResponseEntity.ok("Request updated successfully");
        } else {
            return ResponseEntity.notFound().build();
        }
    }
}