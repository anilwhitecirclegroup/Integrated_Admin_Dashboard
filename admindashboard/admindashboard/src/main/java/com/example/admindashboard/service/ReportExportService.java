package com.example.admindashboard.service;

import com.example.admindashboard.model.Timesheet;
import com.example.admindashboard.model.User;
import jakarta.servlet.ServletOutputStream;
import jakarta.servlet.http.HttpServletResponse;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
public class ReportExportService {

    // --- 1. EXPORT EMPLOYEE MASTER REPORT ---
    public void exportEmployeeReportToExcel(HttpServletResponse response, List<User> employees) throws IOException {
        Workbook workbook = new XSSFWorkbook();
        Sheet sheet = workbook.createSheet("Employee Master Report");

        // Define Headers
        String[] columns = {
                "EMP ID", "Full Name", "Email", "Designation",
                "Department", "Joining Date", "Experience",
                "Contact Number", "Reporting Manager", "Status"
        };

        // Create Header Row
        Row headerRow = sheet.createRow(0);
        CellStyle headerStyle = createHeaderStyle(workbook);

        for (int i = 0; i < columns.length; i++) {
            Cell cell = headerRow.createCell(i);
            cell.setCellValue(columns[i]);
            cell.setCellStyle(headerStyle);
        }

        // Fill Data Rows
        int rowIdx = 1;
        for (User user : employees) {
            Row row = sheet.createRow(rowIdx++);

            row.createCell(0).setCellValue(user.getUsername()); // EMP ID
            row.createCell(1).setCellValue(user.getFullName());
            row.createCell(2).setCellValue(user.getEmail() != null ? user.getEmail() : "N/A");
            row.createCell(3).setCellValue(user.getDesignation() != null ? user.getDesignation() : "N/A");
            row.createCell(4).setCellValue(user.getBusinessUnit() != null ? user.getBusinessUnit() : "N/A");

            // Date handling
            row.createCell(5).setCellValue(user.getJoiningDate() != null ? user.getJoiningDate().toString() : "N/A");

            row.createCell(6).setCellValue(user.getExperience() != null ? user.getExperience() : "N/A");
            row.createCell(7).setCellValue(user.getMobileNumber() != null ? user.getMobileNumber() : "N/A");
            row.createCell(8).setCellValue(user.getReportingManager() != null ? user.getReportingManager() : "N/A");
            row.createCell(9).setCellValue("Active");
        }

        // Auto-size columns for a professional look
        for (int i = 0; i < columns.length; i++) {
            sheet.autoSizeColumn(i);
        }

        // Write to Response
        ServletOutputStream outputStream = response.getOutputStream();
        workbook.write(outputStream);
        workbook.close();
        outputStream.close();
    }

    //---- 2. EXPORT TIMESHEET REPORT
    public void exportTimesheetReportToExcel(HttpServletResponse response, List<Timesheet> timesheets) throws IOException {
        Workbook workbook = new XSSFWorkbook();
        Sheet sheet = workbook.createSheet("Timesheet Report");

        // 1. DEFINE HEADERS (Exact Order)
        String[] columns = {
                "Employee ID",      // Index 0
                "Employee Name",    // Index 1
                "Designation",      // Index 2 (NEW)
                "Week Range",       // Index 3
                "Submitted On",     // Index 4
                "Total Hours",      // Index 5
                "Status",           // Index 6
                "Approved By"       // Index 7 (NEW)
        };

        // 2. CREATE HEADER ROW
        Row headerRow = sheet.createRow(0);
        CellStyle headerStyle = createHeaderStyle(workbook);

        for (int i = 0; i < columns.length; i++) {
            Cell cell = headerRow.createCell(i);
            cell.setCellValue(columns[i]);
            cell.setCellStyle(headerStyle);
        }

        // 3. FILL DATA ROWS (Strict Index Mapping)
        int rowIdx = 1;
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy");

        for (Timesheet ts : timesheets) {
            Row row = sheet.createRow(rowIdx++);

            // --- COL 0: Employee ID ---
            row.createCell(0).setCellValue(ts.getUser().getUsername());

            // --- COL 1: Name ---
            row.createCell(1).setCellValue(ts.getUser().getFullName());

            // --- COL 2: Designation (The Missing Piece!) ---
            String designation = ts.getUser().getDesignation();
            row.createCell(2).setCellValue(designation != null && !designation.isEmpty() ? designation : "N/A");

            // --- COL 3: Week Range ---
            // Calculate End Date (Start + 6 days)
            String startDate = ts.getWeekStartDate().format(formatter);
            String endDate = ts.getWeekStartDate().plusDays(6).format(formatter);
            row.createCell(3).setCellValue(startDate + " - " + endDate);

            // --- COL 4: Submitted On ---
            if (ts.getSubmissionDate() != null) {
                row.createCell(4).setCellValue(ts.getSubmissionDate().format(formatter));
            } else {
                row.createCell(4).setCellValue("Not Submitted");
            }

            // --- COL 5: Total Hours ---
            row.createCell(5).setCellValue(ts.getTotalHours() != null ? ts.getTotalHours() : 0.0);

            // --- COL 6: Status ---
            row.createCell(6).setCellValue(ts.getStatus());

            // --- COL 7: Approved By ---
            String approver = ts.getApprovedBy();
            row.createCell(7).setCellValue(approver != null && !approver.isEmpty() ? approver : "-");
        }

        // 4. AUTO-SIZE COLUMNS
        for (int i = 0; i < columns.length; i++) {
            sheet.autoSizeColumn(i);
        }

        // 5. WRITE OUTPUT
        ServletOutputStream outputStream = response.getOutputStream();
        workbook.write(outputStream);
        workbook.close();
        outputStream.close();
    }

    // ==========================================
    // EXPORT ATTENDANCE REPORT TO EXCEL
    // ==========================================
    public void exportAttendanceReportToExcel(HttpServletResponse response, List<com.example.admindashboard.model.Attendance> attendanceList) throws IOException {
        Workbook workbook = new XSSFWorkbook();
        Sheet sheet = workbook.createSheet("Attendance Report");

        // 1. DEFINE HEADERS (Removed "Approved By")
        String[] columns = {
                "Employee ID",
                "Employee Name",
                "Designation",
                "Week Range",
                "Submitted On",
                "Total Hours",
                "Status"
        };

        // 2. CREATE HEADER ROW
        Row headerRow = sheet.createRow(0);
        CellStyle headerStyle = createHeaderStyle(workbook);

        for (int i = 0; i < columns.length; i++) {
            Cell cell = headerRow.createCell(i);
            cell.setCellValue(columns[i]);
            cell.setCellStyle(headerStyle);
        }

        // 3. FILL DATA ROWS
        int rowIdx = 1;
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy");

        for (com.example.admindashboard.model.Attendance att : attendanceList) {
            Row row = sheet.createRow(rowIdx++);

            // Employee Details
            row.createCell(0).setCellValue(att.getUser() != null ? att.getUser().getUsername() : "N/A");
            row.createCell(1).setCellValue(att.getUser() != null ? att.getUser().getFullName() : "N/A");

            String designation = att.getUser() != null ? att.getUser().getDesignation() : null;
            row.createCell(2).setCellValue(designation != null && !designation.isEmpty() ? designation : "N/A");

            // Week Range (Using String directly)
            String weekStart = att.getWeekStartDate() != null ? att.getWeekStartDate() : "N/A";
            String weekEnd = att.getWeekEndDate() != null ? att.getWeekEndDate() : "N/A";
            row.createCell(3).setCellValue(weekStart + " to " + weekEnd);

            // Submission Date (Using submittedOn)
            if (att.getSubmittedOn() != null) {
                row.createCell(4).setCellValue(att.getSubmittedOn().format(formatter));
            } else {
                row.createCell(4).setCellValue("Not Submitted");
            }

            // Hours & Status (Using String directly and approvalStatus)
            row.createCell(5).setCellValue(att.getTotalHours() != null ? att.getTotalHours() : "0");
            row.createCell(6).setCellValue(att.getApprovalStatus() != null ? att.getApprovalStatus() : "Pending");
        }

        for (int i = 0; i < columns.length; i++) {
            sheet.autoSizeColumn(i);
        }

        ServletOutputStream outputStream = response.getOutputStream();
        workbook.write(outputStream);
        workbook.close();
        outputStream.close();
    }


    // --- 3. HELPER METHOD: CREATE HEADER STYLE (This fixes the error) ---
    private CellStyle createHeaderStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();

        // Font settings
        Font font = workbook.createFont();
        font.setBold(true);
        font.setColor(IndexedColors.WHITE.getIndex());
        style.setFont(font);

        // Background color (Navy Blue)
        style.setFillForegroundColor(IndexedColors.DARK_BLUE.getIndex());
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);

        // Alignment
        style.setAlignment(HorizontalAlignment.CENTER);

        return style;
    }


    // EXPORT LEAVES TO EXCEL
    // ==========================================
    public void exportLeaveReportToExcel(jakarta.servlet.http.HttpServletResponse response, java.util.List<com.example.admindashboard.model.LeaveRequest> leaves) throws java.io.IOException {
        try (org.apache.poi.ss.usermodel.Workbook workbook = new org.apache.poi.xssf.usermodel.XSSFWorkbook();
             java.io.OutputStream out = response.getOutputStream()) {

            org.apache.poi.ss.usermodel.Sheet sheet = workbook.createSheet("Leave Report");

            // Header Row
            org.apache.poi.ss.usermodel.Row headerRow = sheet.createRow(0);
            String[] columns = {"Employee Name", "EMP ID", "Leave Type", "Start Date", "End Date", "Total Days", "Status", "Reason"};

            org.apache.poi.ss.usermodel.Font headerFont = workbook.createFont();
            headerFont.setBold(true);
            org.apache.poi.ss.usermodel.CellStyle headerStyle = workbook.createCellStyle();
            headerStyle.setFont(headerFont);

            for (int i = 0; i < columns.length; i++) {
                org.apache.poi.ss.usermodel.Cell cell = headerRow.createCell(i);
                cell.setCellValue(columns[i]);
                cell.setCellStyle(headerStyle);
            }

            // Data Rows
            int rowIdx = 1;
            for (com.example.admindashboard.model.LeaveRequest leave : leaves) {
                org.apache.poi.ss.usermodel.Row row = sheet.createRow(rowIdx++);
                row.createCell(0).setCellValue(leave.getUser() != null ? leave.getUser().getFullName() : "N/A");
                row.createCell(1).setCellValue(leave.getUser() != null ? leave.getUser().getUsername() : "N/A");
                row.createCell(2).setCellValue(leave.getLeaveType() != null ? leave.getLeaveType() : "N/A");
                row.createCell(3).setCellValue(leave.getFromDate() != null ? leave.getFromDate().toString() : "");
                row.createCell(4).setCellValue(leave.getToDate() != null ? leave.getToDate().toString() : "");
                row.createCell(5).setCellValue(leave.getTotalDays() != null ? leave.getTotalDays() : 0.0);
                row.createCell(6).setCellValue(leave.getStatus() != null ? leave.getStatus() : "Pending");
                row.createCell(7).setCellValue(leave.getReason() != null ? leave.getReason() : "");
            }

            for (int i = 0; i < columns.length; i++) {
                sheet.autoSizeColumn(i);
            }

            workbook.write(out);
        }
    }

    // EXPORT LEAVES TO PDF
    // ==========================================
    public void exportLeaveReportToPdf(jakarta.servlet.http.HttpServletResponse response, java.util.List<com.example.admindashboard.model.LeaveRequest> leaves) throws java.io.IOException {
        try {
            com.lowagie.text.Document document = new com.lowagie.text.Document(com.lowagie.text.PageSize.A4.rotate()); // Landscape for more columns
            com.lowagie.text.pdf.PdfWriter.getInstance(document, response.getOutputStream());
            document.open();

            com.lowagie.text.Font titleFont = new com.lowagie.text.Font(com.lowagie.text.Font.HELVETICA, 18, com.lowagie.text.Font.BOLD);
            document.add(new com.lowagie.text.Paragraph("Monthly Leave Report", titleFont));
            document.add(new com.lowagie.text.Paragraph(" "));

            com.lowagie.text.pdf.PdfPTable table = new com.lowagie.text.pdf.PdfPTable(7);
            table.setWidthPercentage(100);

            String[] headers = {"Employee", "ID", "Type", "Start", "End", "Days", "Status"};
            com.lowagie.text.Font headerFont = new com.lowagie.text.Font(com.lowagie.text.Font.HELVETICA, 12, com.lowagie.text.Font.BOLD);

            for (String header : headers) {
                table.addCell(new com.lowagie.text.Paragraph(header, headerFont));
            }

            for (com.example.admindashboard.model.LeaveRequest leave : leaves) {
                table.addCell(leave.getUser() != null ? leave.getUser().getFullName() : "N/A");
                table.addCell(leave.getUser() != null ? leave.getUser().getUsername() : "N/A");
                table.addCell(leave.getLeaveType() != null ? leave.getLeaveType() : "N/A");
                table.addCell(leave.getFromDate() != null ? leave.getFromDate().toString() : "");
                table.addCell(leave.getToDate() != null ? leave.getToDate().toString() : "");
                table.addCell(String.valueOf(leave.getTotalDays() != null ? leave.getTotalDays() : 0.0));
                table.addCell(leave.getStatus() != null ? leave.getStatus() : "Pending");
            }

            document.add(table);
            document.close();
        } catch (Exception e) {
            throw new java.io.IOException("Error generating PDF: " + e.getMessage());
        }
    }
}