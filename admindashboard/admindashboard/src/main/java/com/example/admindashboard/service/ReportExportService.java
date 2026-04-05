package com.example.admindashboard.service;

import com.example.admindashboard.model.EmployeeProfile;
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
            EmployeeProfile profile = user.getEmployeeProfile(); // Grab the new profile object

            row.createCell(0).setCellValue(user.getUsername()); // EMP ID
            row.createCell(1).setCellValue(user.getFullName());
            row.createCell(2).setCellValue(user.getEmail() != null ? user.getEmail() : "N/A");

            // Extract HR data safely from the Profile
            row.createCell(3).setCellValue(profile != null && profile.getDesignation() != null ? profile.getDesignation() : "N/A");
            row.createCell(4).setCellValue(profile != null && profile.getBusinessUnit() != null ? profile.getBusinessUnit() : "N/A");
            row.createCell(5).setCellValue(profile != null && profile.getJoiningDate() != null ? profile.getJoiningDate().toString() : "N/A");
            row.createCell(6).setCellValue(profile != null && profile.getExperience() != null ? profile.getExperience() : "N/A");
            row.createCell(7).setCellValue(profile != null && profile.getMobileNumber() != null ? profile.getMobileNumber() : "N/A");
            row.createCell(8).setCellValue(profile != null && profile.getReportingManager() != null ? profile.getReportingManager() : "N/A");

            row.createCell(9).setCellValue(user.getStatus() != null ? user.getStatus() : "Active");
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

    //---- 2. EXPORT WEEKLY TIMESHEET REPORT
    public void exportTimesheetReportToExcel(HttpServletResponse response, List<com.example.admindashboard.model.WeeklyTimesheet> timesheets) throws IOException {
        Workbook workbook = new XSSFWorkbook();
        Sheet sheet = workbook.createSheet("Timesheet Details");

        // 1. Expanded Headers to include daily breakdown and task totals
        String[] columns = {
                "Employee ID", "Employee Name", "Designation", "Week Range",
                "Project ID", "Task ID", "Task Type",
                "Mon", "Tue", "Wed", "Thu", "Fri",
                "Task Total", "Overall Week Total", "Status", "Submitted On"
        };

        Row headerRow = sheet.createRow(0);
        CellStyle headerStyle = createHeaderStyle(workbook);

        for (int i = 0; i < columns.length; i++) {
            Cell cell = headerRow.createCell(i);
            cell.setCellValue(columns[i]);
            cell.setCellStyle(headerStyle);
        }

        int rowIdx = 1;
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy");

        // 2. Loop through timesheets, and then loop through the tasks INSIDE the timesheet
        for (com.example.admindashboard.model.WeeklyTimesheet ts : timesheets) {
            String empId = ts.getUser() != null ? ts.getUser().getUsername() : "N/A";
            String empName = ts.getUser() != null ? ts.getUser().getFullName() : "N/A";

            // Safely get Designation from the Profile
            String designation = "N/A";
            if (ts.getUser() != null && ts.getUser().getEmployeeProfile() != null && ts.getUser().getEmployeeProfile().getDesignation() != null) {
                designation = ts.getUser().getEmployeeProfile().getDesignation();
            }

            String weekRange = (ts.getWeekStartDate() != null && ts.getWeekEndDate() != null) ?
                    ts.getWeekStartDate().format(formatter) + " to " + ts.getWeekEndDate().format(formatter) : "N/A";

            String submittedOn = ts.getSubmittedAt() != null ? ts.getSubmittedAt().format(formatter) : "Not Submitted";
            double weekTotal = ts.getTotalWeekHours() != null ? ts.getTotalWeekHours() : 0.0;
            String status = ts.getStatus() != null ? ts.getStatus() : "DRAFT";

            // If the timesheet has logged tasks, print a row for each task
            if (ts.getEntries() != null && !ts.getEntries().isEmpty()) {
                for (com.example.admindashboard.model.WeeklyTimesheetEntry entry : ts.getEntries()) {
                    Row row = sheet.createRow(rowIdx++);

                    row.createCell(0).setCellValue(empId);
                    row.createCell(1).setCellValue(empName);
                    row.createCell(2).setCellValue(designation);
                    row.createCell(3).setCellValue(weekRange);

                    row.createCell(4).setCellValue(entry.getProjectId() != null ? "PRJ-" + entry.getProjectId() : "N/A");
                    row.createCell(5).setCellValue(entry.getTaskId() != null ? "TSK-" + entry.getTaskId() : "N/A");
                    row.createCell(6).setCellValue(entry.getType() != null ? entry.getType() : "Standard");

                    row.createCell(7).setCellValue(entry.getMonHours() != null ? entry.getMonHours() : 0.0);
                    row.createCell(8).setCellValue(entry.getTueHours() != null ? entry.getTueHours() : 0.0);
                    row.createCell(9).setCellValue(entry.getWedHours() != null ? entry.getWedHours() : 0.0);
                    row.createCell(10).setCellValue(entry.getThuHours() != null ? entry.getThuHours() : 0.0);
                    row.createCell(11).setCellValue(entry.getFriHours() != null ? entry.getFriHours() : 0.0);

                    row.createCell(12).setCellValue(entry.getRowTotalHours() != null ? entry.getRowTotalHours() : 0.0);
                    row.createCell(13).setCellValue(weekTotal);
                    row.createCell(14).setCellValue(status);
                    row.createCell(15).setCellValue(submittedOn);
                }
            } else {
                // Fallback row if they created a timesheet but haven't logged any specific tasks yet
                Row row = sheet.createRow(rowIdx++);
                row.createCell(0).setCellValue(empId);
                row.createCell(1).setCellValue(empName);
                row.createCell(2).setCellValue(designation);
                row.createCell(3).setCellValue(weekRange);
                row.createCell(4).setCellValue("No Projects Logged");
                row.createCell(5).setCellValue("-");
                row.createCell(6).setCellValue("-");
                row.createCell(7).setCellValue(0.0);
                row.createCell(8).setCellValue(0.0);
                row.createCell(9).setCellValue(0.0);
                row.createCell(10).setCellValue(0.0);
                row.createCell(11).setCellValue(0.0);
                row.createCell(12).setCellValue(0.0);
                row.createCell(13).setCellValue(weekTotal);
                row.createCell(14).setCellValue(status);
                row.createCell(15).setCellValue(submittedOn);
            }
        }

        for (int i = 0; i < columns.length; i++) {
            sheet.autoSizeColumn(i);
        }

        ServletOutputStream outputStream = response.getOutputStream();
        workbook.write(outputStream);
        workbook.close();
        outputStream.close();
    }


    // EXPORT ATTENDANCE REPORT TO EXCEL
    // ============================================================
    public void exportAttendanceReportToExcel(HttpServletResponse response, List<com.example.admindashboard.model.Attendance> attendanceList) throws IOException {
        Workbook workbook = new XSSFWorkbook();
        Sheet sheet = workbook.createSheet("Attendance Detailed Report");

        // 1. DEFINE DETAILED HEADERS
        String[] columns = {
                "EMP ID", "Employee Name", "Designation", "Week Range",
                "Present Days", "Absent Days", "Total Hours", "Status", "Submitted On",
                "Mon (Hrs/Status/Mode/Reason)",
                "Tue (Hrs/Status/Mode/Reason)",
                "Wed (Hrs/Status/Mode/Reason)",
                "Thu (Hrs/Status/Mode/Reason)",
                "Fri (Hrs/Status/Mode/Reason)",
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

            // Basic Info
            row.createCell(0).setCellValue(att.getUser() != null ? att.getUser().getUsername() : "N/A");
            row.createCell(1).setCellValue(att.getUser() != null ? att.getUser().getFullName() : "N/A");

            // Safely get Designation from the Profile
            String designation = "N/A";
            if (att.getUser() != null && att.getUser().getEmployeeProfile() != null && att.getUser().getEmployeeProfile().getDesignation() != null) {
                designation = att.getUser().getEmployeeProfile().getDesignation();
            }
            row.createCell(2).setCellValue(designation);

            // Range & Summary
            String weekRange = (att.getWeekStartDate() != null ? att.getWeekStartDate() : "N/A") +
                    " to " + (att.getWeekEndDate() != null ? att.getWeekEndDate() : "N/A");
            row.createCell(3).setCellValue(weekRange);
            row.createCell(4).setCellValue(att.getPresentDays() != null ? att.getPresentDays() : 0);
            row.createCell(5).setCellValue(att.getAbsentDays() != null ? att.getAbsentDays() : 0);
            row.createCell(6).setCellValue(att.getTotalHours() != null ? att.getTotalHours() : "0");
            row.createCell(7).setCellValue(att.getApprovalStatus() != null ? att.getApprovalStatus() : "Pending");

            String subDate = att.getSubmittedOn() != null ? att.getSubmittedOn().format(formatter) : "Not Submitted";
            row.createCell(8).setCellValue(subDate);

            // Detailed Daily Breakdown (Combined into single cells for readability)
            row.createCell(9).setCellValue(formatDayInfo(att.getMondayHours(), att.getMondayStatus(), att.getMondayMode(), att.getMondayReason()));
            row.createCell(10).setCellValue(formatDayInfo(att.getTuesdayHours(), att.getTuesdayStatus(), att.getTuesdayMode(), att.getTuesdayReason()));
            row.createCell(11).setCellValue(formatDayInfo(att.getWednesdayHours(), att.getWednesdayStatus(), att.getWednesdayMode(), att.getWednesdayReason()));
            row.createCell(12).setCellValue(formatDayInfo(att.getThursdayHours(), att.getThursdayStatus(), att.getThursdayMode(), att.getThursdayReason()));
            row.createCell(13).setCellValue(formatDayInfo(att.getFridayHours(), att.getFridayStatus(), att.getFridayMode(), att.getFridayReason()));
        }

        // 4. AUTO-SIZE COLUMNS
        for (int i = 0; i < columns.length; i++) {
            sheet.autoSizeColumn(i);
        }

        // 5. WRITE TO RESPONSE
        ServletOutputStream outputStream = response.getOutputStream();
        workbook.write(outputStream);
        workbook.close();
        outputStream.close();
    }

    // HELPER: Format daily details into a single readable string for Excel
    private String formatDayInfo(Double hours, String status, String mode, String reason) {
        if (status == null && hours == null) return "-";
        StringBuilder sb = new StringBuilder();
        sb.append(hours != null ? hours : 0).append("h | ");
        sb.append(status != null ? status : "N/A").append(" | ");
        sb.append(mode != null ? mode : "N/A");
        if (reason != null && !reason.isEmpty()) {
            sb.append(" (").append(reason).append(")");
        }
        return sb.toString();
    }


    // --- 3. HELPER METHOD: CREATE HEADER STYLE ---
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