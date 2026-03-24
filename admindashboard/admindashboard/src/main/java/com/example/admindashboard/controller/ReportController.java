package com.example.admindashboard.controller;

import com.example.admindashboard.model.Attendance;
import com.example.admindashboard.model.Timesheet;
import com.example.admindashboard.model.User;
import com.example.admindashboard.repository.AttendanceRepository;
import com.example.admindashboard.repository.TimesheetRepository;
import com.example.admindashboard.repository.UserRepository;
import com.example.admindashboard.service.ReportExportService;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.io.IOException;
import java.time.LocalDate;
import java.time.temporal.TemporalAdjusters;
import java.util.ArrayList;
import java.util.List;

@Controller
public class ReportController {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private com.example.admindashboard.repository.WeeklyTimesheetRepository weeklyTimesheetRepository;

    @Autowired
    private AttendanceRepository attendanceRepository;

    @Autowired
    private ReportExportService exportService;

    // @Autowired private LeaveRepository leaveRepository;

    @GetMapping("/admin/timesheets/view")
    public String showWeeklyTimesheetView(Model model) {
        List<User> employees = userRepository.findByRoleOrderByUsernameAsc("EMPLOYEE");
        model.addAttribute("employees", employees);

        // FIX: Change Timesheet to WeeklyTimesheet and use the new repository
        List<com.example.admindashboard.model.WeeklyTimesheet> allTimesheets = weeklyTimesheetRepository.findAll();
        model.addAttribute("allTimesheets", allTimesheets != null ? allTimesheets : new ArrayList<>());

        return "admin/admin-weekly-timesheet-report";
    }

    @GetMapping("/admin/reports")
    public String showReportsDashboard(
            @RequestParam(defaultValue = "employee") String type,
            @RequestParam(defaultValue = "") String search,
            @RequestParam(defaultValue = "asc") String sortDir,
            @RequestParam(required = false) LocalDate from,
            @RequestParam(required = false) LocalDate to,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            Model model) {

        // If the user doesn't pick a date, we default to the current month.
        if (from == null) from = LocalDate.now().with(TemporalAdjusters.firstDayOfMonth());
        if (to == null) to = LocalDate.now().with(TemporalAdjusters.lastDayOfMonth());

        // 1. Add Common Attributes (So filters stick in UI)
        model.addAttribute("currentType", type);
        model.addAttribute("currentSearch", search);
        model.addAttribute("currentSortDir", sortDir);
        model.addAttribute("fromDate", from);
        model.addAttribute("toDate", to);
        model.addAttribute("currentPage", page);

        String keyword = (search != null) ? search : "";

        // 2. LOGIC SWITCHER (Routes to the 4 separate pages)
        switch (type) {
            case "employee":
                Sort sort = sortDir.equalsIgnoreCase("asc") ?
                        Sort.by("username").ascending() : Sort.by("username").descending();
                Pageable empPageable = PageRequest.of(page, size, sort);

                Page<User> employeePage = (search == null || search.isEmpty()) ?
                        userRepository.findByRole("EMPLOYEE", empPageable) :
                        userRepository.searchEmployees(search, empPageable);

                model.addAttribute("dataPage", employeePage);
                return "admin/employee-master-report";

            case "timesheet":
                Pageable timePageable = PageRequest.of(page, size, Sort.by("weekStartDate").descending());
                keyword = (search != null) ? search : "";

                Page<com.example.admindashboard.model.WeeklyTimesheet> timesheetPage =
                        weeklyTimesheetRepository.searchTimesheets(from, to, keyword, timePageable);

                model.addAttribute("dataPage", timesheetPage);
                return "admin/timesheets-report";

            case "attendance":
                Pageable attPageable = PageRequest.of(page, size, Sort.by("weekStartDate").descending());
                keyword = (search != null) ? search : "";

                // Convert LocalDate to String for the DB comparison
                String fromStr = (from != null) ? from.toString() : null;
                String toStr = (to != null) ? to.toString() : null;

                Page<Attendance> attendancePage = attendanceRepository.searchAttendance(fromStr, toStr, keyword, attPageable);
                model.addAttribute("dataPage", attendancePage);
                return "admin/attendance-report";

            case "leave":
                return "admin/leave-report";

            default:
                return "redirect:/admin/reports?type=employee";
        }
    }

    @GetMapping("/admin/reports/download")
    public void downloadReport(
            @RequestParam String type,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) LocalDate from,
            @RequestParam(required = false) LocalDate to,
            HttpServletResponse response) throws IOException {

        // Standardize Date Range
        if (from == null) from = LocalDate.now().with(TemporalAdjusters.firstDayOfMonth());
        if (to == null) to = LocalDate.now().with(TemporalAdjusters.lastDayOfMonth());

        response.setContentType("application/octet-stream");
        String headerValue = "attachment; filename=" + type + "_report.xlsx";
        response.setHeader("Content-Disposition", headerValue);

        if ("employee".equals(type)) {
            List<User> exportList;
            if (search != null && !search.isEmpty()) {
                exportList = userRepository.findByFullNameContainingIgnoreCaseOrUsernameContainingIgnoreCase(search, search);
                exportList = exportList.stream().filter(u -> "EMPLOYEE".equals(u.getRole())).toList();
            } else {
                exportList = userRepository.findByRoleOrderByUsernameAsc("EMPLOYEE");
            }
            exportService.exportEmployeeReportToExcel(response, exportList);
        }

        else if ("timesheet".equals(type)) {
            List<com.example.admindashboard.model.WeeklyTimesheet> exportList;
            String keyword = (search != null) ? search : "";

            exportList = weeklyTimesheetRepository.findTimesheetsBySearchCriteria(from, to, keyword);
            exportService.exportTimesheetReportToExcel(response, exportList);
        }

        else if ("attendance".equals(type)) {
            List<Attendance> exportList;
            String keyword = (search != null) ? search : "";

            // Convert LocalDate to String for the DB comparison
            String fromStr = (from != null) ? from.toString() : null;
            String toStr = (to != null) ? to.toString() : null;

            exportList = attendanceRepository.findAttendanceBySearchCriteria(fromStr, toStr, keyword);
            exportService.exportAttendanceReportToExcel(response, exportList);
        }
    }
}