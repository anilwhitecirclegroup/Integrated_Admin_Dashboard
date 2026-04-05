package com.example.admindashboard.controller;

import com.example.admindashboard.dto.LeaveReportDTO;
import com.example.admindashboard.model.LeaveRequest;
import com.example.admindashboard.model.User;
import com.example.admindashboard.repository.LeaveRequestRepository;
import com.example.admindashboard.repository.UserRepository;
import com.example.admindashboard.service.ReportExportService;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
// NEW: Imports for Security and Principal
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.security.Principal;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/admin/leave")
public class LeaveReportRestController {

    @Autowired
    private LeaveRequestRepository leaveRepository;

    @Autowired
    private ReportExportService exportService;

    // NEW: Inject UserRepository to determine user roles and hierarchy
    @Autowired
    private UserRepository userRepository;

    // 1. Fetch JSON data for the frontend table
    // LOCK: Restrict access to authorized reporting roles
    @PreAuthorize("hasAnyAuthority('ROLE_SUPER_ADMIN', 'ROLE_HR_ADMIN', 'ROLE_HR_EXECUTIVE', 'ROLE_MANAGER')")
    @GetMapping("/report-data")
    public ResponseEntity<List<LeaveReportDTO>> getMonthlyLeaveData(@RequestParam int year, @RequestParam int month, Principal principal) {

        User currentUser = userRepository.findByUsername(principal.getName()).orElse(null);
        boolean isManager = currentUser != null && currentUser.getRole() != null && "MANAGER".equalsIgnoreCase(currentUser.getRole().getRoleName());

        YearMonth yearMonth = YearMonth.of(year, month);
        LocalDate startDate = yearMonth.atDay(1);
        LocalDate endDate = yearMonth.atEndOfMonth();

        List<LeaveRequest> leaves = leaveRepository.findByFromDateBetweenOrderByFromDateDesc(startDate, endDate);

        // VISIBILITY: Filter leaves for Managers so they only see their team's data
        leaves = filterLeavesByManager(leaves, isManager, currentUser);

        // Map to our safe DTO
        List<LeaveReportDTO> dtoList = leaves.stream().map(leave -> new LeaveReportDTO(
                leave.getUser() != null ? leave.getUser().getFullName() : "Unknown",
                leave.getUser() != null ? leave.getUser().getUsername() : "N/A",
                leave.getLeaveType(),
                leave.getFromDate(),
                leave.getToDate(),
                leave.getTotalDays(),
                leave.getStatus(),
                leave.getReason()
        )).collect(Collectors.toList());

        return ResponseEntity.ok(dtoList);
    }

    // 2. Export to Excel
    // LOCK: Restrict access to authorized reporting roles
    @PreAuthorize("hasAnyAuthority('ROLE_SUPER_ADMIN', 'ROLE_HR_ADMIN', 'ROLE_HR_EXECUTIVE', 'ROLE_MANAGER')")
    @GetMapping("/export/excel")
    public void exportExcel(@RequestParam int year, @RequestParam int month, Principal principal, HttpServletResponse response) throws IOException {

        User currentUser = userRepository.findByUsername(principal.getName()).orElse(null);
        boolean isManager = currentUser != null && currentUser.getRole() != null && "MANAGER".equalsIgnoreCase(currentUser.getRole().getRoleName());

        YearMonth yearMonth = YearMonth.of(year, month);
        List<LeaveRequest> leaves = leaveRepository.findByFromDateBetweenOrderByFromDateDesc(yearMonth.atDay(1), yearMonth.atEndOfMonth());

        // VISIBILITY: Prevent Managers from downloading Excel data of other teams
        leaves = filterLeavesByManager(leaves, isManager, currentUser);

        response.setContentType("application/octet-stream");
        String headerValue = String.format("attachment; filename=Leave_Report_%d_%02d.xlsx", year, month);
        response.setHeader("Content-Disposition", headerValue);

        exportService.exportLeaveReportToExcel(response, leaves);
    }

    // 3. Export to PDF
    // LOCK: Restrict access to authorized reporting roles
    @PreAuthorize("hasAnyAuthority('ROLE_SUPER_ADMIN', 'ROLE_HR_ADMIN', 'ROLE_HR_EXECUTIVE', 'ROLE_MANAGER')")
    @GetMapping("/export/pdf")
    public void exportPdf(@RequestParam int year, @RequestParam int month, Principal principal, HttpServletResponse response) throws IOException {

        User currentUser = userRepository.findByUsername(principal.getName()).orElse(null);
        boolean isManager = currentUser != null && currentUser.getRole() != null && "MANAGER".equalsIgnoreCase(currentUser.getRole().getRoleName());

        YearMonth yearMonth = YearMonth.of(year, month);
        List<LeaveRequest> leaves = leaveRepository.findByFromDateBetweenOrderByFromDateDesc(yearMonth.atDay(1), yearMonth.atEndOfMonth());

        // VISIBILITY: Prevent Managers from downloading PDF data of other teams
        leaves = filterLeavesByManager(leaves, isManager, currentUser);

        response.setContentType("application/pdf");
        String headerValue = String.format("attachment; filename=Leave_Report_%d_%02d.pdf", year, month);
        response.setHeader("Content-Disposition", headerValue);

        exportService.exportLeaveReportToPdf(response, leaves);
    }


    // HELPER METHOD: Centralized Data Visibility Filter

    private List<LeaveRequest> filterLeavesByManager(List<LeaveRequest> leaves, boolean isManager, User currentUser) {
        if (!isManager || currentUser == null) {
            return leaves;
        }
        return leaves.stream()
                .filter(leave -> leave.getUser() != null
                        && leave.getUser().getManager() != null
                        && leave.getUser().getManager().getId().equals(currentUser.getId()))
                .collect(Collectors.toList());
    }
}