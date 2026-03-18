package com.example.admindashboard.controller;

import com.example.admindashboard.dto.LeaveReportDTO;
import com.example.admindashboard.model.LeaveRequest;
import com.example.admindashboard.repository.LeaveRequestRepository;
import com.example.admindashboard.service.ReportExportService;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
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

    // 1. Fetch JSON data for the frontend table
    @GetMapping("/report-data")
    public ResponseEntity<List<LeaveReportDTO>> getMonthlyLeaveData(@RequestParam int year, @RequestParam int month) {

        // Calculate the first and last day of the selected month
        YearMonth yearMonth = YearMonth.of(year, month);
        LocalDate startDate = yearMonth.atDay(1);
        LocalDate endDate = yearMonth.atEndOfMonth();

        // Fetch using the custom query we added to the repository last time
        List<LeaveRequest> leaves = leaveRepository.findByFromDateBetweenOrderByFromDateDesc(startDate, endDate);

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
    @GetMapping("/export/excel")
    public void exportExcel(@RequestParam int year, @RequestParam int month, HttpServletResponse response) throws IOException {
        YearMonth yearMonth = YearMonth.of(year, month);
        List<LeaveRequest> leaves = leaveRepository.findByFromDateBetweenOrderByFromDateDesc(yearMonth.atDay(1), yearMonth.atEndOfMonth());

        response.setContentType("application/octet-stream");
        String headerValue = String.format("attachment; filename=Leave_Report_%d_%02d.xlsx", year, month);
        response.setHeader("Content-Disposition", headerValue);

        exportService.exportLeaveReportToExcel(response, leaves);
    }

    // 3. Export to PDF
    @GetMapping("/export/pdf")
    public void exportPdf(@RequestParam int year, @RequestParam int month, HttpServletResponse response) throws IOException {
        YearMonth yearMonth = YearMonth.of(year, month);
        List<LeaveRequest> leaves = leaveRepository.findByFromDateBetweenOrderByFromDateDesc(yearMonth.atDay(1), yearMonth.atEndOfMonth());

        response.setContentType("application/pdf");
        String headerValue = String.format("attachment; filename=Leave_Report_%d_%02d.pdf", year, month);
        response.setHeader("Content-Disposition", headerValue);

        exportService.exportLeaveReportToPdf(response, leaves);
    }
}