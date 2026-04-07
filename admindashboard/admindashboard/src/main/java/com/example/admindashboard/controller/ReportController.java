package com.example.admindashboard.controller;

import com.example.admindashboard.model.Attendance;
import com.example.admindashboard.model.User;
import com.example.admindashboard.repository.AttendanceRepository;
import com.example.admindashboard.repository.TimesheetRepository;
import com.example.admindashboard.repository.UserRepository;
import com.example.admindashboard.service.ReportExportService;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.io.IOException;
import java.security.Principal;
import java.time.LocalDate;
import java.time.temporal.TemporalAdjusters;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

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

    // LOCK: Restrict access to authorized reporting roles
    @PreAuthorize("hasAnyAuthority('ROLE_SUPER_ADMIN', 'ROLE_HR_ADMIN', 'ROLE_HR_EXECUTIVE', 'ROLE_MANAGER', 'ROLE_FINANCE')")
    @GetMapping("/admin/timesheets/view")
    public String showWeeklyTimesheetView(Model model, Principal principal) {
        User currentUser = userRepository.findByUsername(principal.getName()).orElse(null);
        boolean isManager = currentUser != null && currentUser.getRole() != null && "MANAGER".equalsIgnoreCase(currentUser.getRole().getRoleName());

        // FIXED: Show all staff (HR, Finance, Manager, Employee) EXCEPT Super Admin and Clients
        List<User> employees = userRepository.findAll().stream()
                .filter(u -> u.getRole() != null &&
                        !"CLIENT".equalsIgnoreCase(u.getRole().getRoleName()) &&
                        !"SUPER_ADMIN".equalsIgnoreCase(u.getRole().getRoleName()) &&
                        !"INACTIVE".equalsIgnoreCase(u.getStatus()))
                .sorted((u1, u2) -> u1.getUsername().compareToIgnoreCase(u2.getUsername()))
                .collect(Collectors.toList());

        employees = filterListByManager(employees, isManager, currentUser, u -> u);
        model.addAttribute("employees", employees);

        List<com.example.admindashboard.model.WeeklyTimesheet> allTimesheets = weeklyTimesheetRepository.findAll();
        if (allTimesheets == null) allTimesheets = new ArrayList<>();

        allTimesheets = filterListByManager(allTimesheets, isManager, currentUser, ts -> ts.getUser());

        model.addAttribute("allTimesheets", allTimesheets);

        return "admin/admin-weekly-timesheet-report";
    }

    // LOCK: Restrict access to authorized reporting roles
    @PreAuthorize("hasAnyAuthority('ROLE_SUPER_ADMIN', 'ROLE_HR_ADMIN', 'ROLE_HR_EXECUTIVE', 'ROLE_MANAGER', 'ROLE_FINANCE')")
    @GetMapping("/admin/reports")
    public String showReportsDashboard(
            @RequestParam(defaultValue = "employee") String type,
            @RequestParam(defaultValue = "") String search,
            @RequestParam(defaultValue = "asc") String sortDir,
            @RequestParam(required = false) LocalDate from,
            @RequestParam(required = false) LocalDate to,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            Principal principal,
            Model model) {

        User currentUser = userRepository.findByUsername(principal.getName()).orElse(null);
        boolean isManager = currentUser != null && currentUser.getRole() != null && "MANAGER".equalsIgnoreCase(currentUser.getRole().getRoleName());

        if (from == null) from = LocalDate.now().with(TemporalAdjusters.firstDayOfMonth());
        if (to == null) to = LocalDate.now().with(TemporalAdjusters.lastDayOfMonth());

        model.addAttribute("currentType", type);
        model.addAttribute("currentSearch", search);
        model.addAttribute("currentSortDir", sortDir);
        model.addAttribute("fromDate", from);
        model.addAttribute("toDate", to);
        model.addAttribute("currentPage", page);

        String keyword = (search != null) ? search : "";

        switch (type) {
            case "employee":
                // FIXED: Stream approach to safely filter out Super Admins, Clients, and Inactive users
                Stream<User> empStream = userRepository.findAll().stream()
                        .filter(u -> u.getRole() != null &&
                                !"CLIENT".equalsIgnoreCase(u.getRole().getRoleName()) &&
                                !"SUPER_ADMIN".equalsIgnoreCase(u.getRole().getRoleName()) &&
                                !"INACTIVE".equalsIgnoreCase(u.getStatus()));

                if (search != null && !search.trim().isEmpty()) {
                    String lowerSearch = search.toLowerCase();
                    empStream = empStream.filter(u ->
                            (u.getUsername() != null && u.getUsername().toLowerCase().contains(lowerSearch)) ||
                                    (u.getFullName() != null && u.getFullName().toLowerCase().contains(lowerSearch))
                    );
                }

                // Sorting
                if (sortDir.equalsIgnoreCase("desc")) {
                    empStream = empStream.sorted(Comparator.comparing(User::getUsername, Comparator.nullsLast(String::compareToIgnoreCase)).reversed());
                } else {
                    empStream = empStream.sorted(Comparator.comparing(User::getUsername, Comparator.nullsLast(String::compareToIgnoreCase)));
                }

                List<User> allFilteredUsers = empStream.collect(Collectors.toList());

                // Manual Pagination to ensure accurate pages with the new filters
                int start = Math.min(page * size, allFilteredUsers.size());
                int end = Math.min((start + size), allFilteredUsers.size());
                List<User> pageContent = allFilteredUsers.subList(start, end);

                Page<User> employeePage = new PageImpl<>(pageContent, PageRequest.of(page, size), allFilteredUsers.size());

                employeePage = filterPageByManager(employeePage, isManager, currentUser, u -> u);
                model.addAttribute("dataPage", employeePage);
                return "admin/employee-master-report";

            case "timesheet":
                Pageable timePageable = PageRequest.of(page, size, Sort.by("weekStartDate").descending());
                keyword = (search != null) ? search : "";

                Page<com.example.admindashboard.model.WeeklyTimesheet> timesheetPage =
                        weeklyTimesheetRepository.searchTimesheets(from, to, keyword, timePageable);

                timesheetPage = filterPageByManager(timesheetPage, isManager, currentUser, ts -> ts.getUser());
                model.addAttribute("dataPage", timesheetPage);
                return "admin/timesheets-report";

            case "attendance":
                Pageable attPageable = PageRequest.of(page, size, Sort.by("weekStartDate").descending());
                keyword = (search != null) ? search : "";

                String fromStr = (from != null) ? from.toString() : "1970-01-01";
                String toStr = (to != null) ? to.toString() : "9999-12-31";

                Page<Attendance> attendancePage = attendanceRepository.searchAttendance(fromStr, toStr, keyword, attPageable);

                attendancePage = filterPageByManager(attendancePage, isManager, currentUser, att -> att.getUser());
                model.addAttribute("dataPage", attendancePage);
                return "admin/attendance-report";

            case "leave":
                return "admin/leave-report";

            default:
                return "redirect:/admin/reports?type=employee";
        }
    }

    // LOCK: Restrict access to authorized reporting roles
    @PreAuthorize("hasAnyAuthority('ROLE_SUPER_ADMIN', 'ROLE_HR_ADMIN', 'ROLE_HR_EXECUTIVE', 'ROLE_MANAGER', 'ROLE_FINANCE')")
    @GetMapping("/admin/reports/download")
    public void downloadReport(
            @RequestParam String type,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) LocalDate from,
            @RequestParam(required = false) LocalDate to,
            Principal principal,
            HttpServletResponse response) throws IOException {

        User currentUser = userRepository.findByUsername(principal.getName()).orElse(null);
        boolean isManager = currentUser != null && currentUser.getRole() != null && "MANAGER".equalsIgnoreCase(currentUser.getRole().getRoleName());

        if (from == null) from = LocalDate.now().with(TemporalAdjusters.firstDayOfMonth());
        if (to == null) to = LocalDate.now().with(TemporalAdjusters.lastDayOfMonth());

        response.setContentType("application/octet-stream");
        String headerValue = "attachment; filename=" + type + "_report.xlsx";
        response.setHeader("Content-Disposition", headerValue);

        if ("employee".equals(type)) {
            // FIXED: Apply the same multi-role filter to the Excel Export logic
            Stream<User> exportStream = userRepository.findAll().stream()
                    .filter(u -> u.getRole() != null &&
                            !"CLIENT".equalsIgnoreCase(u.getRole().getRoleName()) &&
                            !"SUPER_ADMIN".equalsIgnoreCase(u.getRole().getRoleName()) &&
                            !"INACTIVE".equalsIgnoreCase(u.getStatus()));

            if (search != null && !search.trim().isEmpty()) {
                String lowerSearch = search.toLowerCase();
                exportStream = exportStream.filter(u ->
                        (u.getUsername() != null && u.getUsername().toLowerCase().contains(lowerSearch)) ||
                                (u.getFullName() != null && u.getFullName().toLowerCase().contains(lowerSearch))
                );
            }

            List<User> exportList = exportStream
                    .sorted((u1, u2) -> u1.getUsername().compareToIgnoreCase(u2.getUsername()))
                    .collect(Collectors.toList());

            exportList = filterListByManager(exportList, isManager, currentUser, u -> u);
            exportService.exportEmployeeReportToExcel(response, exportList);
        }
        else if ("timesheet".equals(type)) {
            List<com.example.admindashboard.model.WeeklyTimesheet> exportList;
            String keyword = (search != null) ? search : "";

            exportList = weeklyTimesheetRepository.findTimesheetsBySearchCriteria(from, to, keyword);
            exportList = filterListByManager(exportList, isManager, currentUser, ts -> ts.getUser());
            exportService.exportTimesheetReportToExcel(response, exportList);
        }
        else if ("attendance".equals(type)) {
            List<Attendance> exportList;
            String keyword = (search != null) ? search : "";

            String fromStr = (from != null) ? from.toString() : null;
            String toStr = (to != null) ? to.toString() : null;

            exportList = attendanceRepository.findAttendanceBySearchCriteria(fromStr, toStr, keyword);
            exportList = filterListByManager(exportList, isManager, currentUser, att -> att.getUser());
            exportService.exportAttendanceReportToExcel(response, exportList);
        }
    }

    // HELPER METHODS: Centralized Data Visibility Filters (Point 4 of RBAC)

    private <T> List<T> filterListByManager(List<T> list, boolean isManager, User currentUser, Function<T, User> userExtractor) {
        if (!isManager || currentUser == null || list == null) return list;
        return list.stream()
                .filter(item -> {
                    User u = userExtractor.apply(item);
                    return u != null && u.getManager() != null && u.getManager().getId().equals(currentUser.getId());
                })
                .collect(Collectors.toList());
    }

    private <T> Page<T> filterPageByManager(Page<T> page, boolean isManager, User currentUser, Function<T, User> userExtractor) {
        if (!isManager || currentUser == null || page == null) return page;
        List<T> filteredList = filterListByManager(page.getContent(), isManager, currentUser, userExtractor);
        return new PageImpl<>(filteredList, page.getPageable(), filteredList.size());
    }
}