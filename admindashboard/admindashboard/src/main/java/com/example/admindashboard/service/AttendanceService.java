package com.example.admindashboard.service;

import com.example.admindashboard.model.Attendance;
import com.example.admindashboard.model.User;
import com.example.admindashboard.repository.AttendanceRepository;
import com.example.admindashboard.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

@Service
public class AttendanceService {

    @Autowired
    private AttendanceRepository attendanceRepository;

    @Autowired
    private UserRepository userRepository;

    // Logic: Employee Check-In
    public String checkIn(String username) {
        Optional<User> userOpt = userRepository.findByUsername(username);
        if (userOpt.isEmpty()) return "User not found!";
        User user = userOpt.get();

        LocalDate today = LocalDate.now();

        // Rule: Prevent Duplicate Entry
        Optional<Attendance> existing = attendanceRepository.findByUserAndDate(user, today);
        if (existing.isPresent()) {
            return "You have already checked in today!";
        }

        Attendance attendance = new Attendance();
        attendance.setUser(user);
        attendance.setDate(today);
        attendance.setCheckInTime(LocalTime.now());
        attendance.setStatus("Present");

        attendanceRepository.save(attendance);
        return "Check-In Successful at " + LocalTime.now();
    }

    // Logic: Employee Check-Out
    public String checkOut(String username) {
        Optional<User> userOpt = userRepository.findByUsername(username);
        if (userOpt.isEmpty()) return "User not found!";
        User user = userOpt.get();

        LocalDate today = LocalDate.now();

        // Rule: No Checkout without Checkin
        Optional<Attendance> existing = attendanceRepository.findByUserAndDate(user, today);
        if (existing.isEmpty()) {
            return "You have not checked in today!";
        }

        Attendance attendance = existing.get();
        if (attendance.getCheckOutTime() != null) {
            return "You have already checked out today!";
        }

        attendance.setCheckOutTime(LocalTime.now());

        // Rule: Auto Calculate Total Hours
        long minutes = Duration.between(attendance.getCheckInTime(), attendance.getCheckOutTime()).toMinutes();
        long hours = minutes / 60;
        long remainingMinutes = minutes % 60;
        attendance.setTotalHours(hours + "h " + remainingMinutes + "m");

        attendanceRepository.save(attendance);
        return "Check-Out Successful. Total Hours: " + attendance.getTotalHours();
    }

    // --- ADMIN FEATURE ---
    // Count how many people checked in today
    public long getTodayPresentCount() {
        LocalDate today = LocalDate.now();
        return attendanceRepository.countByDate(today);
    }

    // ==========================================
    // ATTENDANCE REGULATION (WEEKLY) LOGIC
    // ==========================================
    public Attendance saveWeeklyDraft(java.util.Map<String, Object> data, String username) {
        Optional<User> userOpt = userRepository.findByUsername(username);
        if (userOpt.isEmpty()) throw new RuntimeException("User not found!");
        User user = userOpt.get();

        String weekStartDateStr = (String) data.get("weekStartDate");
        LocalDate weekStartDate = LocalDate.parse(weekStartDateStr);
        
        // duplicate-check for submission
        String weekEndDateStr = weekStartDate.plusDays(6).toString();

        List<Attendance> existingWeeklyAttendance =
                attendanceRepository.findByUserAndWeekStartDateAndWeekEndDate(user,weekStartDate.toString(),weekEndDateStr );

        Attendance attendance;

        if (!existingWeeklyAttendance.isEmpty()) {
            attendance = existingWeeklyAttendance.get(0);

            if (!"Draft".equalsIgnoreCase(attendance.getApprovalStatus())) {
                throw new RuntimeException("You have already submitted an attendance regularization request for this week.");
            }
        } else {
            attendance = new Attendance();
            attendance.setUser(user);
            attendance.setWeekStartDate(weekStartDate.toString());
            attendance.setWeekEndDate(weekEndDateStr);
            attendance.setApprovalStatus("Draft");
        }

        double totalHours = 0.0;
        int presentDays = 0;
        int absentDays = 0;
        String generalReason = "Weekly attendance submission.";

        String[] days = {"monday", "tuesday", "wednesday", "thursday", "friday"};
        for (String day : days) {
            Object hoursObj = data.get(day + "Hours");
            double hours = hoursObj != null ? Double.parseDouble(hoursObj.toString()) : 0.0;
            totalHours += hours;

            String status = (String) data.get(day + "Status");
            if ("Absent".equalsIgnoreCase(status)) {
                absentDays++;
            } else if (hours > 0 || "Present".equalsIgnoreCase(status) || "Present (Late)".equalsIgnoreCase(status)) {
                presentDays++;
            }

            String reason = (String) data.get(day + "Reason");
            if (reason != null && !reason.trim().isEmpty() && generalReason.equals("Weekly attendance submission.")) {
                generalReason = reason;
            }
        }

        attendance.setTotalHours(String.valueOf(totalHours));
        attendance.setPresentDays(presentDays);
        attendance.setAbsentDays(absentDays);
        attendance.setReason(generalReason);
        attendance.setApprovalStatus("Draft");

        // --- SAVE DAILY BREAKDOWNS ---
        attendance.setMondayHours(data.get("mondayHours") != null ? Double.parseDouble(data.get("mondayHours").toString()) : 0.0);
        attendance.setMondayStatus((String) data.get("mondayStatus"));
        attendance.setMondayMode((String) data.get("mondayMode"));
        attendance.setMondayReason((String) data.get("mondayReason"));

        attendance.setTuesdayHours(data.get("tuesdayHours") != null ? Double.parseDouble(data.get("tuesdayHours").toString()) : 0.0);
        attendance.setTuesdayStatus((String) data.get("tuesdayStatus"));
        attendance.setTuesdayMode((String) data.get("tuesdayMode"));
        attendance.setTuesdayReason((String) data.get("tuesdayReason"));

        attendance.setWednesdayHours(data.get("wednesdayHours") != null ? Double.parseDouble(data.get("wednesdayHours").toString()) : 0.0);
        attendance.setWednesdayStatus((String) data.get("wednesdayStatus"));
        attendance.setWednesdayMode((String) data.get("wednesdayMode"));
        attendance.setWednesdayReason((String) data.get("wednesdayReason"));

        attendance.setThursdayHours(data.get("thursdayHours") != null ? Double.parseDouble(data.get("thursdayHours").toString()) : 0.0);
        attendance.setThursdayStatus((String) data.get("thursdayStatus"));
        attendance.setThursdayMode((String) data.get("thursdayMode"));
        attendance.setThursdayReason((String) data.get("thursdayReason"));

        attendance.setFridayHours(data.get("fridayHours") != null ? Double.parseDouble(data.get("fridayHours").toString()) : 0.0);
        attendance.setFridayStatus((String) data.get("fridayStatus"));
        attendance.setFridayMode((String) data.get("fridayMode"));
        attendance.setFridayReason((String) data.get("fridayReason"));

        return attendanceRepository.save(attendance);
    }

    public void submitWeeklyAttendance(Long id, String username) {
        // 1. Find the saved draft
        Attendance attendance = attendanceRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Attendance record not found"));

        // 2. Security Check: Make sure the logged-in user owns this record
        if (!attendance.getUser().getUsername().equals(username)) {
            throw new RuntimeException("You are not authorized to submit this record.");
        }

        // 3. Update status to Pending so the Manager sees it in My Approvals
        attendance.setApprovalStatus("Pending");
        attendance.setSubmittedOn(LocalDate.now());

        attendanceRepository.save(attendance);
    }

    // Fetch Attendance History for the logged-in user
    public List<Attendance> getMyAttendanceHistory(String username) {
        Optional<User> userOpt = userRepository.findByUsername(username);
        if (userOpt.isPresent()) {
            return attendanceRepository.findByUserOrderByIdDesc(userOpt.get());
        }
        return java.util.List.of(); // Return empty list if user not found
    }
}