package com.example.admindashboard.controller;

import com.example.admindashboard.model.WeeklyTimesheet;
import com.example.admindashboard.repository.WeeklyTimesheetRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/admin/timesheet")
public class AdminTimesheetController {

    @Autowired
    private WeeklyTimesheetRepository timesheetRepository;

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
            if (comments != null) {
                ts.setOverallComments(comments);
            }
            timesheetRepository.save(ts);
            return ResponseEntity.ok("Timesheet " + status);
        }
        return ResponseEntity.notFound().build();
    }
}