package com.example.admindashboard.controller;

import com.example.admindashboard.model.LeaveRequest;
import com.example.admindashboard.model.User;
import com.example.admindashboard.repository.LeaveRequestRepository;
import com.example.admindashboard.repository.UserRepository;
import com.example.admindashboard.service.EmailService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import java.security.Principal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import com.example.admindashboard.model.EmployeeLeaveWallet;
import com.example.admindashboard.model.LeaveLedger;
import com.example.admindashboard.model.LeaveTypeMaster;
import com.example.admindashboard.repository.EmployeeLeaveWalletRepository;
import com.example.admindashboard.repository.LeaveLedgerRepository;
import com.example.admindashboard.repository.LeaveTypeMasterRepository;

@Controller
public class AdminLeaveController {

    @Autowired
    private LeaveRequestRepository leaveRequestRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private EmailService emailService;

    @Autowired
    private EmployeeLeaveWalletRepository walletRepository;

    @Autowired
    private LeaveTypeMasterRepository leaveTypeRepository;

    @Autowired
    private LeaveLedgerRepository leaveLedgerRepository;

    // 1. LOAD THE ADMIN DASHBOARD PAGE
    // LOCK: User must have 'leave_view' permission
    @PreAuthorize("hasAuthority('leave_view')")
    @GetMapping("/admin/leave-approvals")
    public String showLeaveApprovals(Model model, Principal principal) {
        User currentUser = userRepository.findByUsername(principal.getName()).orElse(null);
        boolean isManager = currentUser != null && currentUser.getRole() != null && "MANAGER".equalsIgnoreCase(currentUser.getRole().getRoleName());

        // Fetch and filter the data based on hierarchy rules
        List<LeaveRequest> pending = filterLeavesByHierarchy(leaveRequestRepository.findByStatus("Pending"), isManager, currentUser);
        List<LeaveRequest> approved = filterLeavesByHierarchy(leaveRequestRepository.findByStatus("Approved"), isManager, currentUser);
        List<LeaveRequest> rejected = filterLeavesByHierarchy(leaveRequestRepository.findByStatus("Rejected"), isManager, currentUser);

        model.addAttribute("pendingLeaves", pending);
        model.addAttribute("approvedLeaves", approved);
        model.addAttribute("rejectedLeaves", rejected);

        return "admin-leave-approvals";
    }

    // HELPER METHOD: Applies the strict Data Visibility Rule (Point 4)
    private List<LeaveRequest> filterLeavesByHierarchy(List<LeaveRequest> leaves, boolean isManager, User currentUser) {
        if (!isManager || currentUser == null) {
            return leaves; // Super Admin and HR Admin can see all leaves
        }

        // Managers only see leaves where the applicant's manager_id matches the Manager's ID
        return leaves.stream()
                .filter(leave -> leave.getUser() != null
                        && leave.getUser().getManager() != null
                        && leave.getUser().getManager().getId().equals(currentUser.getId()))
                .collect(Collectors.toList());
    }

    // 2. HANDLE APPROVAL (Called by JavaScript)
    // LOCK: User must have 'leave_approve' permission
    @PreAuthorize("hasAuthority('leave_approve')")
    @PostMapping("/api/admin/leave/approve/{id}")
    @ResponseBody
    public ResponseEntity<String> approveLeave(@PathVariable Long id, Principal principal) {
        try {
            User currentUser = userRepository.findByUsername(principal.getName()).orElseThrow();
            boolean isManager = currentUser.getRole() != null && "MANAGER".equalsIgnoreCase(currentUser.getRole().getRoleName());

            LeaveRequest leave = leaveRequestRepository.findById(id)
                    .orElseThrow(() -> new IllegalArgumentException("Invalid leave Id:" + id));

            // CRITICAL SECURITY BLOCK: Prevent a Manager from approving leaves outside their team via API bypass
            if (isManager && (leave.getUser().getManager() == null || !leave.getUser().getManager().getId().equals(currentUser.getId()))) {
                return ResponseEntity.status(403).body("Error: 403 Forbidden. You are not authorized to approve leaves for employees outside your reporting hierarchy.");
            }
            
            if ("Approved".equalsIgnoreCase(leave.getStatus())) {
                return ResponseEntity.badRequest()
                        .body("Leave already approved.");
            }
            leave.setStatus("Approved");
            leaveRequestRepository.save(leave);

            // Map leave type string to code
            String leaveCode;
            switch (leave.getLeaveType()) {
                case "Casual": leaveCode = "CL"; break;
                case "Sick": leaveCode = "SL"; break;
                case "Earned": leaveCode = "EL"; break;
                case "CompOff": leaveCode = "COMP_OFF"; break;
                default: leaveCode = leave.getLeaveType().toUpperCase(); break;
            }

            LeaveTypeMaster leaveTypeMaster =
                    leaveTypeRepository.findByLeaveCode(leaveCode)
                            .orElseThrow(() -> new RuntimeException("Leave type not found for code: " + leaveCode));

            EmployeeLeaveWallet wallet =
                    walletRepository.findByUserAndLeaveType(leave.getUser(), leaveTypeMaster)
                            .orElseThrow(() -> new RuntimeException("Employee wallet not found"));

            double requestedDays = leave.getTotalDays();
            double availableBalance = wallet.getAvailableBalance();

            // ══════════════════════════════════════════════════════════════════
            // SMART LOP CONVERSION: If balance is insufficient, auto-split
            // Regular deduction (what's available) + LOP (excess days)
            // ══════════════════════════════════════════════════════════════════
            double regularDays;
            double lopDays;

            if (availableBalance >= requestedDays) {
                // Sufficient balance — full deduction from this leave type
                regularDays = requestedDays;
                lopDays = 0;
            } else {
                // Insufficient balance — deduct what's available, convert rest to LOP
                regularDays = Math.max(availableBalance, 0);
                lopDays = requestedDays - regularDays;
            }

            // --- Deduct from primary leave type ---
            if (regularDays > 0) {
                wallet.setAvailableBalance(wallet.getAvailableBalance() - regularDays);
                wallet.setUsedBalance(wallet.getUsedBalance() + regularDays);
                walletRepository.save(wallet);

                // Create ledger entry for regular deduction
                LeaveLedger ledger = new LeaveLedger();
                ledger.setTransactionDate(java.time.LocalDateTime.now());
                ledger.setUser(leave.getUser());
                ledger.setLeaveType(leaveTypeMaster);
                ledger.setTransactionType("DEBIT");
                ledger.setDebit(regularDays);
                ledger.setCredit(0.0);
                ledger.setBalanceAfter(wallet.getAvailableBalance());
                ledger.setStatus("APPROVED");
                ledger.setReferenceType("LEAVE_APPROVAL");
                ledger.setReferenceId(leave.getId());
                ledger.setRemarks("Leave approved by admin/manager");
                ledger.setApprovedBy(currentUser);
                leaveLedgerRepository.save(ledger);
            } else if (regularDays == 0 && lopDays > 0) {
                // No regular days to deduct, just save wallet as-is
                walletRepository.save(wallet);
            }

            // --- Deduct excess from LOP ---
            if (lopDays > 0) {
                LeaveTypeMaster lopType = leaveTypeRepository.findByLeaveCode("LOP")
                        .orElseThrow(() -> new RuntimeException("LOP leave type not found"));

                EmployeeLeaveWallet lopWallet = walletRepository.findByUserAndLeaveType(leave.getUser(), lopType)
                        .orElse(null);

                if (lopWallet != null) {
                    lopWallet.setUsedBalance(lopWallet.getUsedBalance() + lopDays);
                    walletRepository.save(lopWallet);
                }

                // Create ledger entry for LOP portion
                LeaveLedger lopLedger = new LeaveLedger();
                lopLedger.setTransactionDate(java.time.LocalDateTime.now());
                lopLedger.setUser(leave.getUser());
                lopLedger.setLeaveType(lopType);
                lopLedger.setTransactionType("DEBIT");
                lopLedger.setDebit(lopDays);
                lopLedger.setCredit(0.0);
                lopLedger.setBalanceAfter(0.0);
                lopLedger.setStatus("APPROVED");
                lopLedger.setReferenceType("LOP_AUTO_CONVERSION");
                lopLedger.setReferenceId(leave.getId());
                lopLedger.setRemarks("Auto-converted to LOP (" + lopDays + " days) — insufficient " + leave.getLeaveType() + " balance");
                lopLedger.setApprovedBy(currentUser);
                leaveLedgerRepository.save(lopLedger);
            }

            // --- EMAIL TRIGGER START ---
            Map<String, Object> emailData = new HashMap<>();
            emailData.put("empName", leave.getUser().getFullName());
            emailData.put("specificType", leave.getLeaveType());
            emailData.put("submittedOn", leave.getCreatedAt());
            emailData.put("duration", leave.getFromDate() + " to " + leave.getToDate() + " (" + leave.getTotalDays() + " Days)");

            if (lopDays > 0) {
                emailData.put("lopNote", regularDays + " day(s) from " + leave.getLeaveType() + " + " + lopDays + " day(s) as LOP");
            }

            emailService.sendRequestStatusUpdateToEmployee(
                    leave.getUser().getEmail(),
                    leave.getUser().getFullName(),
                    "Leave",
                    "Approved",
                    emailData
            );
            // --- EMAIL TRIGGER END ---

            String successMsg = "Leave Approved successfully";
            if (lopDays > 0) {
                successMsg += " (" + regularDays + " day(s) from " + leave.getLeaveType() + " + " + lopDays + " day(s) converted to LOP)";
            }
            return ResponseEntity.ok(successMsg);

        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error: " + e.getMessage());
        }
    }

    // 3. HANDLE REJECTION & NOTES (Called by JS)
    // LOCK: User must have 'leave_approve' permission
    @PreAuthorize("hasAuthority('leave_approve')")
    @PostMapping("/api/admin/leave/reject/{id}")
    @ResponseBody
    public ResponseEntity<String> rejectLeave(
            @PathVariable Long id,
            @RequestParam(value = "note", required = false) String note,
            Principal principal) {

        try {
            User currentUser = userRepository.findByUsername(principal.getName()).orElseThrow();
            boolean isManager = currentUser.getRole() != null && "MANAGER".equalsIgnoreCase(currentUser.getRole().getRoleName());

            LeaveRequest leave = leaveRequestRepository.findById(id)
                    .orElseThrow(() -> new IllegalArgumentException("Invalid leave Id:" + id));

            // CRITICAL SECURITY BLOCK: Prevent a Manager from rejecting leaves outside their team via API bypass
            if (isManager && (leave.getUser().getManager() == null || !leave.getUser().getManager().getId().equals(currentUser.getId()))) {
                return ResponseEntity.status(403).body("Error: 403 Forbidden. You are not authorized to reject leaves for employees outside your reporting hierarchy.");
            }

            leave.setStatus("Rejected");
            leave.setAdminComments(note);

            leaveRequestRepository.save(leave);

            // --- EMAIL TRIGGER START ---
            Map<String, Object> emailData = new HashMap<>();
            emailData.put("empName", leave.getUser().getFullName());
            emailData.put("specificType", leave.getLeaveType());
            emailData.put("submittedOn", leave.getCreatedAt());
            emailData.put("duration", leave.getFromDate() + " to " + leave.getToDate() + " (" + leave.getTotalDays() + " Days)");
            emailData.put("adminComments", note);

            emailService.sendRequestStatusUpdateToEmployee(
                    leave.getUser().getEmail(),
                    leave.getUser().getFullName(),
                    "Leave",
                    "Rejected",
                    emailData
            );
            // --- EMAIL TRIGGER END ---

            return ResponseEntity.ok("Leave Rejected successfully");

        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error: " + e.getMessage());
        }
    }
}