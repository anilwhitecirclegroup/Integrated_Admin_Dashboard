package com.example.admindashboard.controller;

import com.example.admindashboard.model.Timesheet;
import com.example.admindashboard.service.TimesheetService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.security.Principal;
import java.util.List;

@RestController
@RequestMapping("/api/timesheet")
public class TimesheetController {

    @Autowired
    private TimesheetService timesheetService;

    // API: Save Draft
    @PostMapping("/save")
    public ResponseEntity<?> saveDraft(@RequestBody Timesheet timesheet, Principal principal) {
        try {
            Timesheet saved = timesheetService.saveDraft(principal.getName(), timesheet);
            return ResponseEntity.ok(saved);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    // API: Submit Final
    @PostMapping("/submit/{id}")
    public ResponseEntity<String> submitTimesheet(@PathVariable Long id) {
        String result = timesheetService.submitTimesheet(id);
        if (result.startsWith("Error")) {
            return ResponseEntity.badRequest().body(result);
        }
        return ResponseEntity.ok(result);
    }

    // API: Get My Timesheets
    @GetMapping("/my-history")
    public ResponseEntity<List<Timesheet>> getMyTimesheets(Principal principal) {
        return ResponseEntity.ok(timesheetService.getEmployeeTimesheets(principal.getName()));
    }
}