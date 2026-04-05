package com.example.admindashboard.controller;

import com.example.admindashboard.dto.TimesheetSubmissionDTO;
import com.example.admindashboard.model.WeeklyTimesheet;
import com.example.admindashboard.model.User;
import com.example.admindashboard.repository.UserRepository;
import com.example.admindashboard.service.WeeklyTimesheetService;
import com.example.admindashboard.service.EmailService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import com.example.admindashboard.repository.WeeklyTimesheetRepository;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import java.security.Principal;

@RestController
@RequestMapping("/api/weekly-timesheet")
public class WeeklyTimesheetRestController {

    @Autowired
    private WeeklyTimesheetService timesheetService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private WeeklyTimesheetRepository timesheetRepository;

    @Autowired
    private EmailService emailService;

    @PostMapping("/submit")
    public ResponseEntity<Map<String, String>> submitTimesheet(@RequestBody TimesheetSubmissionDTO payload) {
        Map<String, String> response = new HashMap<>();
        try {
            // 1. Save the timesheet to the database
            timesheetService.saveWeeklyTimesheet(payload);

            // 2. NEW: ASYNC EMAIL TRIGGER FOR TIMESHEET
            try {
                // Find out who is currently logged in
                Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
                if (authentication != null && authentication.isAuthenticated() && !"anonymousUser".equals(authentication.getPrincipal())) {

                    String currentUsername = authentication.getName();
                    Optional<User> currentUserOpt = userRepository.findByUsername(currentUsername);

                    if (currentUserOpt.isPresent()) {
                        User currentUser = currentUserOpt.get();

                        // Package the data for our Thymeleaf HTML template
                        Map<String, Object> emailData = new HashMap<>();
                        emailData.put("empName", currentUser.getFullName());
                        emailData.put("empId", currentUser.getUsername());
                        emailData.put("weekStartDate", payload.getWeekStartDate());
                        emailData.put("weekEndDate", payload.getWeekEndDate());
                        emailData.put("totalHours", payload.getTotalWeekHours());
                        emailData.put("comments", payload.getOverallComments());

                        // TODO: Change to your actual testing email address!
                        String adminEmail = "arcthunder07@gmail.com";

                        emailService.sendTimesheetSubmissionToAdmin(adminEmail, currentUser.getFullName(), currentUser.getEmail(), emailData);
                    }
                }
            } catch (Exception e) {
                // Catch any email errors silently so the user still sees "Timesheet saved successfully!"
                System.err.println("Non-fatal error: Failed to trigger timesheet email - " + e.getMessage());
            }

            response.put("status", "success");
            response.put("message", "Timesheet saved successfully!");
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            response.put("status", "error");
            response.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    @GetMapping("/current-user")
    public ResponseEntity<Map<String, Object>> getCurrentUserDetails() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        // Safety check to ensure someone is actually logged in
        if (authentication == null || !authentication.isAuthenticated() || "anonymousUser".equals(authentication.getPrincipal())) {
            return ResponseEntity.status(401).build(); // 401 Unauthorized shortcut
        }

        String currentUsername = authentication.getName();

        // UPDATED: Look them up in the User database
        Optional<User> currentUserOpt = userRepository.findByUsername(currentUsername);

        if (currentUserOpt.isEmpty()) {
            return ResponseEntity.notFound().build(); // 404 Not Found shortcut
        }

        User currentUser = currentUserOpt.get();

        Map<String, Object> response = new HashMap<>();

        // Send "EMP001" to be displayed on the screen
        response.put("employeeId", currentUser.getUsername());

        // Send their full name
        response.put("employeeName", currentUser.getFullName());

        return ResponseEntity.ok(response);
    }

    @GetMapping("/my-timesheets")
    public ResponseEntity<List<WeeklyTimesheet>> getMyTimesheets() {
        List<WeeklyTimesheet> timesheets = timesheetService.getAllMyTimesheets();
        return ResponseEntity.ok(timesheets);
    }

    @GetMapping("/week")
    public ResponseEntity<WeeklyTimesheet> getTimesheetForWeek(
            @RequestParam("startDate") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate) {

        Optional<WeeklyTimesheet> timesheet = timesheetService.getTimesheetByWeekStartDate(startDate);

        return timesheet.map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.noContent().build());
    }


    // EXPORT ENDPOINTS (NOW SECURED AGAINST IDOR)
    @GetMapping("/export/pdf/{id}")
    public ResponseEntity<byte[]> exportPdf(@PathVariable Long id, Principal principal) {
        WeeklyTimesheet timesheet = timesheetRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Timesheet not found"));

        // CRITICAL SECURITY BLOCK: Ensure the logged-in user actually owns this timesheet!
        if (timesheet.getUser() == null || !timesheet.getUser().getUsername().equals(principal.getName())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        byte[] pdfBytes = timesheetService.generatePdf(timesheet);

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=Timesheet_" + timesheet.getWeekStartDate() + ".pdf")
                .contentType(MediaType.APPLICATION_PDF)
                .body(pdfBytes);
    }

    @GetMapping("/export/excel/{id}")
    public ResponseEntity<byte[]> exportExcel(@PathVariable Long id, Principal principal) {
        WeeklyTimesheet timesheet = timesheetRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Timesheet not found"));

        // CRITICAL SECURITY BLOCK: Ensure the logged-in user actually owns this timesheet!
        if (timesheet.getUser() == null || !timesheet.getUser().getUsername().equals(principal.getName())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        byte[] excelBytes = timesheetService.generateExcel(timesheet);

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=Timesheet_" + timesheet.getWeekStartDate() + ".xlsx")
                .contentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                .body(excelBytes);
    }
}