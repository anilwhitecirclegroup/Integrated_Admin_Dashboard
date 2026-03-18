package com.example.admindashboard.service;

import com.example.admindashboard.dto.TimesheetRowDTO;
import com.example.admindashboard.dto.TimesheetSubmissionDTO;
import com.example.admindashboard.model.WeeklyTimesheet;
import com.example.admindashboard.model.WeeklyTimesheetEntry;
import com.example.admindashboard.repository.WeeklyTimesheetRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import com.example.admindashboard.model.User;
import com.example.admindashboard.repository.UserRepository;

import com.lowagie.text.Document;
import com.lowagie.text.Paragraph;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import java.io.ByteArrayOutputStream;


@Service
public class WeeklyTimesheetService {

    @Autowired
    private WeeklyTimesheetRepository timesheetRepository;

    @Autowired
    private UserRepository userRepository;

    // --- HELPER METHOD: Grabs the full User object from Spring Security ---
    private User getLoggedInUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated() || "anonymousUser".equals(authentication.getPrincipal())) {
            throw new RuntimeException("User is not authenticated");
        }
        String currentUsername = authentication.getName(); // This gets "EMP001"
        Optional<User> currentUserOpt = userRepository.findByUsername(currentUsername);

        if (currentUserOpt.isEmpty()) {
            throw new RuntimeException("User not found for username: " + currentUsername);
        }

        return currentUserOpt.get(); // Returns the actual User object
    }

    @Transactional
    public void saveWeeklyTimesheet(TimesheetSubmissionDTO payload) {

        // FIX 1: Get the full User object instead of just the ID
        User currentUser = getLoggedInUser();

        // 1. Check if a timesheet already exists for this week and employee
        Optional<WeeklyTimesheet> existingTimesheetOpt =
                timesheetRepository.findByUserAndWeekStartDate(currentUser, payload.getWeekStartDate());

        WeeklyTimesheet timesheet;

        if (existingTimesheetOpt.isPresent()) {
            // UPDATE EXISTING TIMESHEET
            timesheet = existingTimesheetOpt.get();

            // Prevent modifying if already approved
            if ("APPROVED".equals(timesheet.getStatus())) {
                throw new RuntimeException("Cannot edit a timesheet that has already been approved.");
            }
            timesheet.setStatus(payload.getStatus());
            timesheet.setOverallComments(payload.getOverallComments());
            timesheet.setTotalWeekHours(payload.getTotalWeekHours());
            timesheet.setSubmittedAt(LocalDateTime.now());

            // Clear old rows so we can replace them with the newly submitted ones
            timesheet.getEntries().clear();

        } else {
            // CREATE NEW TIMESHEET
            timesheet = new WeeklyTimesheet();

            // FIX 2: Set the full User object instead of employeeId
            timesheet.setUser(currentUser);

            timesheet.setWeekStartDate(payload.getWeekStartDate());
            timesheet.setWeekEndDate(payload.getWeekEndDate());
            timesheet.setStatus(payload.getStatus());
            timesheet.setOverallComments(payload.getOverallComments());
            timesheet.setTotalWeekHours(payload.getTotalWeekHours());
            timesheet.setSubmittedAt(LocalDateTime.now());
        }

        // 2. Loop through the DTO rows and add them to the timesheet
        if (payload.getRows() != null) {
            for (TimesheetRowDTO rowDto : payload.getRows()) {
                WeeklyTimesheetEntry entry = new WeeklyTimesheetEntry();

                entry.setProjectId(rowDto.getProjectId());
                entry.setTaskId(rowDto.getTaskId());
                entry.setType(rowDto.getType());
                entry.setSunHours(rowDto.getSun());
                entry.setMonHours(rowDto.getMon());
                entry.setTueHours(rowDto.getTue());
                entry.setWedHours(rowDto.getWed());
                entry.setThuHours(rowDto.getThu());
                entry.setFriHours(rowDto.getFri());
                entry.setSatHours(rowDto.getSat());
                entry.setRowTotalHours(rowDto.getRowTotal());
                entry.setComments(rowDto.getDetails());

                // Crucial Step: Link the child to the parent
                entry.setWeeklyTimesheet(timesheet);

                // Add the row
                timesheet.getEntries().add(entry);
            }
        }

        // 3. Save to database
        timesheetRepository.save(timesheet);
    }

    public List<WeeklyTimesheet> getAllMyTimesheets() {
        // FIX 3: Pass User object to repository
        User currentUser = getLoggedInUser();
        return timesheetRepository.findByUserOrderByWeekStartDateDesc(currentUser);
    }

    public Optional<WeeklyTimesheet> getTimesheetByWeekStartDate(LocalDate startDate) {
        // FIX 4: Pass User object to repository
        User currentUser = getLoggedInUser();
        return timesheetRepository.findByUserAndWeekStartDate(currentUser, startDate);
    }

    // ==========================================
    // EXPORT TO EXCEL
    // ==========================================
    public byte[] generateExcel(WeeklyTimesheet ts) {
        try (Workbook workbook = new XSSFWorkbook(); ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            Sheet sheet = workbook.createSheet("Timesheet");

            // Create Header Row
            Row headerRow = sheet.createRow(0);
            String[] columns = {"Project ID", "Task ID", "Type", "Mon", "Tue", "Wed", "Thu", "Fri", "Total Hours", "Comments"};

            // Explicitly use the Apache POI Font
            org.apache.poi.ss.usermodel.Font headerFont = workbook.createFont();
            headerFont.setBold(true);
            CellStyle headerCellStyle = workbook.createCellStyle();
            headerCellStyle.setFont(headerFont);

            for (int i = 0; i < columns.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(columns[i]);
                cell.setCellStyle(headerCellStyle);
            }

            // Daily Total Trackers
            double monTot = 0, tueTot = 0, wedTot = 0, thuTot = 0, friTot = 0;
            int rowIdx = 1;

            // Fill Data Rows
            if (ts.getEntries() != null) {
                for (var entry : ts.getEntries()) {
                    Row row = sheet.createRow(rowIdx++);

                    double mon = entry.getMonHours() != null ? entry.getMonHours() : 0.0;
                    double tue = entry.getTueHours() != null ? entry.getTueHours() : 0.0;
                    double wed = entry.getWedHours() != null ? entry.getWedHours() : 0.0;
                    double thu = entry.getThuHours() != null ? entry.getThuHours() : 0.0;
                    double fri = entry.getFriHours() != null ? entry.getFriHours() : 0.0;

                    // Add to running totals
                    monTot += mon; tueTot += tue; wedTot += wed; thuTot += thu; friTot += fri;

                    row.createCell(0).setCellValue(entry.getProjectId() != null ? String.valueOf(entry.getProjectId()) : "N/A");
                    row.createCell(1).setCellValue(entry.getTaskId() != null ? String.valueOf(entry.getTaskId()) : "N/A");
                    row.createCell(2).setCellValue(entry.getType() != null ? entry.getType() : "Billable");
                    row.createCell(3).setCellValue(mon);
                    row.createCell(4).setCellValue(tue);
                    row.createCell(5).setCellValue(wed);
                    row.createCell(6).setCellValue(thu);
                    row.createCell(7).setCellValue(fri);
                    row.createCell(8).setCellValue(entry.getRowTotalHours() != null ? entry.getRowTotalHours() : 0.0);
                    row.createCell(9).setCellValue(entry.getComments() != null ? entry.getComments() : "");
                }
            }

            // Add DAILY TOTALS Row
            Row dailyTotalRow = sheet.createRow(rowIdx++);
            Cell dailyLabel = dailyTotalRow.createCell(2);
            dailyLabel.setCellValue("DAILY TOTALS:");
            dailyLabel.setCellStyle(headerCellStyle); // Make it bold

            dailyTotalRow.createCell(3).setCellValue(monTot);
            dailyTotalRow.createCell(4).setCellValue(tueTot);
            dailyTotalRow.createCell(5).setCellValue(wedTot);
            dailyTotalRow.createCell(6).setCellValue(thuTot);
            dailyTotalRow.createCell(7).setCellValue(friTot);

            // Add GRAND TOTAL Row
            Row summaryRow = sheet.createRow(rowIdx + 1);
            Cell grandLabel = summaryRow.createCell(0);
            grandLabel.setCellValue("GRAND TOTAL HOURS:");
            grandLabel.setCellStyle(headerCellStyle);
            summaryRow.createCell(8).setCellValue(ts.getTotalWeekHours() != null ? ts.getTotalWeekHours() : 0.0);

            for (int i = 0; i < columns.length; i++) {
                sheet.autoSizeColumn(i);
            }

            workbook.write(out);
            return out.toByteArray();
        } catch (Exception e) {
            throw new RuntimeException("Failed to generate Excel file: " + e.getMessage());
        }
    }

    // ==========================================
    // EXPORT TO PDF
    // ==========================================
    public byte[] generatePdf(WeeklyTimesheet ts) {
        try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            Document document = new Document();
            PdfWriter.getInstance(document, out);
            document.open();

            com.lowagie.text.Font titleFont = new com.lowagie.text.Font(com.lowagie.text.Font.HELVETICA, 18, com.lowagie.text.Font.BOLD);
            document.add(new Paragraph("Weekly Timesheet Report", titleFont));
            document.add(new Paragraph("Week Starting: " + ts.getWeekStartDate()));
            document.add(new Paragraph("Status: " + ts.getStatus()));
            document.add(new Paragraph(" "));

            PdfPTable table = new PdfPTable(7);
            table.setWidthPercentage(100);

            String[] headers = {"Project", "Task", "Mon", "Tue", "Wed", "Thu", "Fri"};
            com.lowagie.text.Font boldFont = new com.lowagie.text.Font(com.lowagie.text.Font.HELVETICA, 12, com.lowagie.text.Font.BOLD);

            for (String header : headers) {
                table.addCell(new Paragraph(header, boldFont));
            }

            // Daily Total Trackers
            double monTot = 0, tueTot = 0, wedTot = 0, thuTot = 0, friTot = 0;

            if (ts.getEntries() != null) {
                for (var entry : ts.getEntries()) {
                    double mon = entry.getMonHours() != null ? entry.getMonHours() : 0.0;
                    double tue = entry.getTueHours() != null ? entry.getTueHours() : 0.0;
                    double wed = entry.getWedHours() != null ? entry.getWedHours() : 0.0;
                    double thu = entry.getThuHours() != null ? entry.getThuHours() : 0.0;
                    double fri = entry.getFriHours() != null ? entry.getFriHours() : 0.0;

                    // Add to running totals
                    monTot += mon; tueTot += tue; wedTot += wed; thuTot += thu; friTot += fri;

                    table.addCell(entry.getProjectId() != null ? String.valueOf(entry.getProjectId()) : "N/A");
                    table.addCell(entry.getTaskId() != null ? String.valueOf(entry.getTaskId()) : "N/A");
                    table.addCell(String.valueOf(mon));
                    table.addCell(String.valueOf(tue));
                    table.addCell(String.valueOf(wed));
                    table.addCell(String.valueOf(thu));
                    table.addCell(String.valueOf(fri));
                }
            }

            // Add DAILY TOTALS Row to PDF
            table.addCell(new Paragraph("DAILY", boldFont));
            table.addCell(new Paragraph("TOTALS", boldFont));
            table.addCell(new Paragraph(String.valueOf(monTot), boldFont));
            table.addCell(new Paragraph(String.valueOf(tueTot), boldFont));
            table.addCell(new Paragraph(String.valueOf(wedTot), boldFont));
            table.addCell(new Paragraph(String.valueOf(thuTot), boldFont));
            table.addCell(new Paragraph(String.valueOf(friTot), boldFont));

            document.add(table);
            document.add(new Paragraph(" "));

            document.add(new Paragraph("Total Week Hours: " + ts.getTotalWeekHours(), boldFont));

            document.close();
            return out.toByteArray();
        } catch (Exception e) {
            throw new RuntimeException("Failed to generate PDF file: " + e.getMessage());
        }
    }
}