package com.example.admindashboard.controller;

import com.example.admindashboard.model.Timesheet;
import com.example.admindashboard.repository.TimesheetRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/admin/regularization")
public class AdminRegularizationController {

    @Autowired
    private TimesheetRepository timesheetRepository;

    // 1. Get List of Requests (PENDING, APPROVED, DENIED)
    @GetMapping("/list")
    public ResponseEntity<List<Timesheet>> getRequests(@RequestParam String status) {
        return ResponseEntity.ok(timesheetRepository.findByStatus(status));
    }

    // 2. Approve or Reject Request
    @PostMapping("/{id}/{status}")
    public ResponseEntity<?> updateStatus(
            @PathVariable Long id,
            @PathVariable String status,
            @RequestParam(required = false) String comments) {

        Optional<Timesheet> requestOpt = timesheetRepository.findById(id);

        if (requestOpt.isPresent()) {
            Timesheet req = requestOpt.get();
            req.setStatus(status);

            // Using the 'comments' field from your Timesheet model
            if (comments != null && !comments.isEmpty()) {
                req.setComments(comments);
            }

            timesheetRepository.save(req);
            return ResponseEntity.ok("Request updated successfully");
        } else {
            return ResponseEntity.notFound().build();
        }
    }
}