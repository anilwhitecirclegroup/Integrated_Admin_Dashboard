package com.example.admindashboard.controller;

import com.example.admindashboard.model.LeaveRequest;
import com.example.admindashboard.model.Attendance;
import com.example.admindashboard.model.WeeklyTimesheet;
import com.example.admindashboard.model.User;
import com.example.admindashboard.repository.LeaveRequestRepository;
import com.example.admindashboard.repository.AttendanceRepository;
import com.example.admindashboard.repository.WeeklyTimesheetRepository;
import com.example.admindashboard.repository.UserRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.security.Principal;
import java.util.List;

@Controller
public class EmployeeApprovalsController {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private LeaveRequestRepository leaveRepo;

    @Autowired
    private AttendanceRepository attendanceRepo;

    @Autowired
    private WeeklyTimesheetRepository timesheetRepo;

    @GetMapping("/my-approvals")
    public String viewMyApprovals(Principal principal, Model model) {

        // 1. Get current employee
        String username = principal.getName();
        User currentUser = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));

        // 2. FETCH PENDING REQUESTS
        // ==========================================
        List<LeaveRequest> pendingLeaves = leaveRepo.findByUserAndStatusIgnoreCaseOrderByIdDesc(currentUser, "Pending");
        List<Attendance> pendingAttendances = attendanceRepo.findByUserAndApprovalStatusIgnoreCaseOrderByIdDesc(currentUser, "Pending");

        // FIX: Using currentUser instead of employeeId
        List<WeeklyTimesheet> pendingTimesheets = timesheetRepo.findByUserAndStatusIgnoreCaseOrderByIdDesc(currentUser, "Submitted");

        model.addAttribute("pendingLeaves", pendingLeaves);
        model.addAttribute("pendingAttendances", pendingAttendances);
        model.addAttribute("pendingTimesheets", pendingTimesheets);

        // Calculate Total
        int totalPending = pendingLeaves.size() + pendingAttendances.size() + pendingTimesheets.size();
        model.addAttribute("totalPending", totalPending);

        // 3. FETCH APPROVED REQUESTS
        // ==========================================
        List<LeaveRequest> approvedLeaves = leaveRepo.findByUserAndStatusIgnoreCaseOrderByIdDesc(currentUser, "Approved");
        List<Attendance> approvedAttendances = attendanceRepo.findByUserAndApprovalStatusIgnoreCaseOrderByIdDesc(currentUser, "Approved");

        // FIX: Using currentUser instead of employeeId
        List<WeeklyTimesheet> approvedTimesheets = timesheetRepo.findByUserAndStatusIgnoreCaseOrderByIdDesc(currentUser, "Approved");

        model.addAttribute("approvedLeaves", approvedLeaves);
        model.addAttribute("approvedAttendances", approvedAttendances);
        model.addAttribute("approvedTimesheets", approvedTimesheets);

        int totalApproved = approvedLeaves.size() + approvedAttendances.size() + approvedTimesheets.size();
        model.addAttribute("totalApproved", totalApproved);

        // 4. FETCH REJECTED REQUESTS
        // ==========================================
        List<LeaveRequest> deniedLeaves = leaveRepo.findByUserAndStatusIgnoreCaseOrderByIdDesc(currentUser, "Rejected");
        List<Attendance> deniedAttendances = attendanceRepo.findByUserAndApprovalStatusIgnoreCaseOrderByIdDesc(currentUser, "Rejected");

        // FIX: Using currentUser instead of employeeId
        List<WeeklyTimesheet> deniedTimesheets = timesheetRepo.findByUserAndStatusIgnoreCaseOrderByIdDesc(currentUser, "Rejected");

        model.addAttribute("deniedLeaves", deniedLeaves);
        model.addAttribute("deniedAttendances", deniedAttendances);
        model.addAttribute("deniedTimesheets", deniedTimesheets);

        int totalDenied = deniedLeaves.size() + deniedAttendances.size() + deniedTimesheets.size();
        model.addAttribute("totalDenied", totalDenied);

        // Grand Total for empty states
        model.addAttribute("totalCount", totalPending + totalApproved + totalDenied);

        return "my-approvals";
    }
}