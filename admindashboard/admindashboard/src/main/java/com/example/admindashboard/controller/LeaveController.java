package com.example.admindashboard.controller;

import java.time.LocalDate;
import java.util.List;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import com.example.admindashboard.model.LeaveRequest;
import com.example.admindashboard.model.LeaveLedger;
import com.example.admindashboard.model.User;
import com.example.admindashboard.model.Holiday;
import com.example.admindashboard.repository.LeaveRequestRepository;
import com.example.admindashboard.repository.UserRepository;
import com.example.admindashboard.repository.LeaveLedgerRepository;
import com.example.admindashboard.repository.HolidayRepository;
import com.example.admindashboard.service.LeaveRequestService;
import com.example.admindashboard.service.EmailService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.security.Principal;
import com.example.admindashboard.service.EmployeeLeaveWalletService;
import com.example.admindashboard.model.EmployeeLeaveWallet;
import com.example.admindashboard.repository.EmployeeLeaveWalletRepository;

@RestController
@RequestMapping("/api/leave")
public class LeaveController {

    @Autowired
    private LeaveRequestRepository leaveRequestRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private LeaveRequestService leaveRequestService;

    @Autowired
    private EmailService emailService;

    @Autowired
    private EmployeeLeaveWalletService employeeLeaveWalletService;

    @Autowired
    private EmployeeLeaveWalletRepository walletRepository;

    @Autowired
    private LeaveLedgerRepository leaveLedgerRepository;

    @Autowired
    private HolidayRepository holidayRepository;

    // 1. SUBMIT LEAVE REQUEST
    @PostMapping("/submit")
    public ResponseEntity<?> submitLeaveRequest(@RequestBody LeaveRequest leaveRequest, Principal principal) {
        try {
            // 1. Find the currently logged-in employee
            String username = principal.getName();
            User currentUser = userRepository.findByUsername(username)
                    .orElseThrow(() -> new RuntimeException("User not found"));
            employeeLeaveWalletService.initializeEmployeeWallet(currentUser);

            // 2. Attach the employee to the request and set status to "Pending"
            leaveRequest.setUser(currentUser);
            leaveRequest.setStatus("Pending");

            // 3. Automatically stamp today's date on the request before saving
            leaveRequest.setCreatedAt(LocalDate.now());

            // 4. Save it to the PostgreSQL database
            LeaveRequest savedRequest = leaveRequestRepository.save(leaveRequest);

            // 5. NEW: ASYNC EMAIL TRIGGER
            try {
                Map<String, Object> emailData = new HashMap<>();
                emailData.put("empName", currentUser.getFullName());
                emailData.put("empId", currentUser.getUsername());
                emailData.put("leaveType", savedRequest.getLeaveType());
                emailData.put("fromDate", savedRequest.getFromDate());
                emailData.put("toDate", savedRequest.getToDate());
                emailData.put("totalDays", savedRequest.getTotalDays());
                emailData.put("reason", savedRequest.getReason());

                // TODO: Replace with your actual Admin/HR email receiving the requests
                String adminEmail = "arcthunder07@gmail.com";

                // Fire and forget! The @Async service handles this in the background
                emailService.sendLeaveRequestToAdmin(adminEmail, currentUser.getFullName(), currentUser.getEmail(), emailData);
            } catch (Exception e) {
                // We catch exceptions here so an email failure doesn't break the actual leave submission!
                System.err.println("Non-fatal error: Failed to trigger admin email - " + e.getMessage());
            }

            return ResponseEntity.ok(savedRequest);

        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error submitting leave: " + e.getMessage());
        }
    }

    // 2. FETCH RECENT LEAVES (For the UI Card)
    @GetMapping("/my-leaves")
    public ResponseEntity<?> getMyRecentLeaves(Principal principal) {
        try {
            List<LeaveRequest> myLeaves = leaveRequestService.getMyLeaves(principal.getName());
            return ResponseEntity.ok(myLeaves);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error fetching leave history: " + e.getMessage());
        }
    }

    // 3. CALCULATE LEAVE BALANCE WITH LEAVE WALLET
    @GetMapping("/balance")
    public ResponseEntity<?> getLeaveBalance(Principal principal) {
        try {
            String username = principal.getName();
            User currentUser = userRepository.findByUsername(username)
                    .orElseThrow(() -> new RuntimeException("User not found"));

            // Ensure wallet exists
            employeeLeaveWalletService.initializeEmployeeWallet(currentUser);

            List<EmployeeLeaveWallet> wallets = walletRepository.findByUser(currentUser);

            Map<String, Map<String, Double>> balances = new HashMap<>();

            for (EmployeeLeaveWallet wallet : wallets) {
                String leaveCode = wallet.getLeaveType().getLeaveCode();
                Double total = wallet.getOpeningBalance();
                Double used = wallet.getUsedBalance();
                Double left = wallet.getAvailableBalance();

                Map<String, Double> leaveData = new HashMap<>();
                leaveData.put("used", used);
                leaveData.put("total", total);
                leaveData.put("left", left);
                balances.put(leaveCode, leaveData);
            }

            return ResponseEntity.ok(balances);

        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body("Error calculating balance: " + e.getMessage());
        }
    }

    // ══════════════════════════════════════════════════════════════════
    // 4. LEAVE LEDGER - Full Transaction History
    // ══════════════════════════════════════════════════════════════════
    @GetMapping("/ledger")
    public ResponseEntity<?> getLeaveLedger(Principal principal) {
        try {
            String username = principal.getName();
            User currentUser = userRepository.findByUsername(username)
                    .orElseThrow(() -> new RuntimeException("User not found"));

            List<LeaveLedger> ledgerEntries =
                    leaveLedgerRepository.findByUserOrderByTransactionDateDesc(currentUser);

            List<Map<String, Object>> result = new ArrayList<>();

            for (LeaveLedger entry : ledgerEntries) {
                Map<String, Object> map = new LinkedHashMap<>();
                map.put("id", entry.getId());
                map.put("date", entry.getTransactionDate() != null ?
                        entry.getTransactionDate().toString() : null);
                map.put("leaveType", entry.getLeaveType() != null ?
                        entry.getLeaveType().getLeaveName() : "Unknown");
                map.put("transactionType", entry.getTransactionType());
                map.put("credit", entry.getCredit());
                map.put("debit", entry.getDebit());
                map.put("balanceAfter", entry.getBalanceAfter());
                map.put("reason", entry.getRemarks());
                map.put("approvedBy", entry.getApprovedBy() != null ?
                        entry.getApprovedBy().getFullName() : "System");
                map.put("status", entry.getStatus());
                result.add(map);
            }

            return ResponseEntity.ok(result);

        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body("Error fetching leave ledger: " + e.getMessage());
        }
    }

    // ══════════════════════════════════════════════════════════════════
    // 5. WALLET SUMMARY - Full Table Data for Leave Wallet View
    // ══════════════════════════════════════════════════════════════════
    @GetMapping("/wallet-summary")
    public ResponseEntity<?> getWalletSummary(Principal principal) {
        try {
            String username = principal.getName();
            User currentUser = userRepository.findByUsername(username)
                    .orElseThrow(() -> new RuntimeException("User not found"));

            employeeLeaveWalletService.initializeEmployeeWallet(currentUser);

            List<EmployeeLeaveWallet> wallets = walletRepository.findByUser(currentUser);

            List<Map<String, Object>> result = new ArrayList<>();

            // Only return active leave types
            java.util.Set<String> activeTypes = java.util.Set.of("CL", "SL", "EL", "COMP_OFF", "LOP");

            for (EmployeeLeaveWallet wallet : wallets) {
                String code = wallet.getLeaveType().getLeaveCode();
                if (!activeTypes.contains(code)) continue;

                Map<String, Object> map = new LinkedHashMap<>();
                map.put("leaveType", wallet.getLeaveType().getLeaveName());
                map.put("leaveCode", code);
                map.put("openingBalance", wallet.getOpeningBalance());
                map.put("monthlyCredit", wallet.getLeaveType().getMonthlyCredit());
                map.put("earnedCredit", wallet.getEarnedCredit());
                map.put("used", wallet.getUsedBalance());
                map.put("encashed", wallet.getEncashedBalance());
                map.put("balance", wallet.getAvailableBalance());
                result.add(map);
            }

            return ResponseEntity.ok(result);

        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body("Error fetching wallet summary: " + e.getMessage());
        }
    }

    // ══════════════════════════════════════════════════════════════════
    // 6. CALENDAR DATA - Monthly Calendar with Leaves + Holidays
    // ══════════════════════════════════════════════════════════════════
    @GetMapping("/calendar-data")
    public ResponseEntity<?> getCalendarData(
            @RequestParam("month") int month,
            @RequestParam("year") int year,
            Principal principal) {
        try {
            String username = principal.getName();
            User currentUser = userRepository.findByUsername(username)
                    .orElseThrow(() -> new RuntimeException("User not found"));

            LocalDate monthStart = LocalDate.of(year, month, 1);
            LocalDate monthEnd = monthStart.withDayOfMonth(monthStart.lengthOfMonth());

            // --- Leaves: get all user's leaves and filter for this month ---
            List<LeaveRequest> allLeaves =
                    leaveRequestRepository.findByUserOrderByIdDesc(currentUser);

            List<Map<String, Object>> leaveEntries = new ArrayList<>();

            for (LeaveRequest leave : allLeaves) {
                if (leave.getFromDate() == null || leave.getToDate() == null) continue;

                // Check if the leave overlaps with the requested month
                if (leave.getToDate().isBefore(monthStart) || leave.getFromDate().isAfter(monthEnd)) {
                    continue; // No overlap
                }

                // Expand each day within the leave period that falls in this month
                LocalDate start = leave.getFromDate().isBefore(monthStart) ? monthStart : leave.getFromDate();
                LocalDate end = leave.getToDate().isAfter(monthEnd) ? monthEnd : leave.getToDate();

                for (LocalDate d = start; !d.isAfter(end); d = d.plusDays(1)) {
                    Map<String, Object> entry = new HashMap<>();
                    entry.put("date", d.toString());
                    entry.put("type", leave.getLeaveType());
                    entry.put("status", leave.getStatus() != null ?
                            leave.getStatus().toUpperCase() : "PENDING");
                    leaveEntries.add(entry);
                }
            }

            // --- Holidays for this month ---
            List<Holiday> holidays =
                    holidayRepository.findByHolidayDateBetweenAndActiveTrue(monthStart, monthEnd);

            List<Map<String, Object>> holidayEntries = new ArrayList<>();

            for (Holiday h : holidays) {
                Map<String, Object> entry = new HashMap<>();
                entry.put("date", h.getHolidayDate().toString());
                entry.put("name", h.getHolidayName());
                entry.put("type", h.getHolidayType());
                holidayEntries.add(entry);
            }

            // --- Combine ---
            Map<String, Object> result = new HashMap<>();
            result.put("leaves", leaveEntries);
            result.put("holidays", holidayEntries);

            return ResponseEntity.ok(result);

        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body("Error fetching calendar data: " + e.getMessage());
        }
    }
}