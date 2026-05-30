package com.example.admindashboard.controller;

import com.example.admindashboard.model.EmployeeProfile;
import com.example.admindashboard.model.User;
import com.example.admindashboard.repository.UserRepository;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.security.Principal;
import java.util.Optional;

@Controller
public class PayrollUIController {

    @Autowired
    private UserRepository userRepository;

    /* ─── PAYROLL LOGIN ─────────────────────────────────────────── */

    @GetMapping("/payroll-login")
    public String payrollLoginPage(HttpSession session) {
        if (Boolean.TRUE.equals(session.getAttribute("payrollAuthenticated"))) {
            return "redirect:/payroll-dashboard";
        }
        return "payroll/payroll-login";
    }

    @PostMapping("/payroll-login")
    public String payrollLoginSubmit(
            @RequestParam("loginId") String loginId,
            @RequestParam("password") String password,
            HttpSession session,
            Model model,
            Principal principal) {

        // Use logged-in user's username if loginId is empty (safety fallback)
        String usernameToCheck = (loginId != null && !loginId.trim().isEmpty())
                ? loginId.trim()
                : (principal != null ? principal.getName() : "");

        Optional<User> userOpt = userRepository.findByUsername(usernameToCheck);

        if (userOpt.isPresent()) {
            User user = userOpt.get();
            String dbPassword = user.getPassword() != null
                    ? user.getPassword().replace("{noop}", "")
                    : "";

            if (dbPassword.equals(password)) {
                session.setAttribute("payrollAuthenticated", true);
                session.setAttribute("payrollUser", usernameToCheck);
                return "redirect:/payroll-dashboard";
            }
        }

        model.addAttribute("error", "Invalid Employee ID or password. Please try again.");
        return "payroll/payroll-login";
    }

    /* ─── HELPER: session guard ─────────────────────────────────── */

    private boolean isPayrollAuthenticated(HttpSession session) {
        return Boolean.TRUE.equals(session.getAttribute("payrollAuthenticated"));
    }

    /* ─── PAYROLL DASHBOARD ─────────────────────────────────────── */

    @GetMapping("/payroll-dashboard")
    public String payrollDashboard(HttpSession session, Model model, Principal principal) {
        if (!isPayrollAuthenticated(session)) return "redirect:/payroll-login";
        
        String username = (String) session.getAttribute("payrollUser");
        if (username == null && principal != null) {
            username = principal.getName();
        }
        
        if (username != null) {
            Optional<User> userOpt = userRepository.findByUsername(username);
            if (userOpt.isPresent()) {
                User user = userOpt.get();
                model.addAttribute("user", user);
                model.addAttribute("profile", user.getEmployeeProfile());
                
                // Get initials for Avatar
                String fullName = user.getFullName();
                String initials = "";
                if (fullName != null && !fullName.isEmpty()) {
                    String[] parts = fullName.split(" ");
                    if (parts.length > 0 && parts[0].length() > 0) {
                        initials += parts[0].substring(0, 1).toUpperCase();
                    }
                    if (parts.length > 1 && parts[1].length() > 0) {
                        initials += parts[1].substring(0, 1).toUpperCase();
                    }
                }
                model.addAttribute("initials", initials);
            }
        }
        return "payroll/payroll-dashboard";
    }

    /* ─── PAYROLL & TAX ─────────────────────────────────────────── */

    @GetMapping("/payroll/tax")
    public String payrollTax(HttpSession session, Model model, Principal principal) {
        if (!isPayrollAuthenticated(session)) return "redirect:/payroll-login";
        
        String username = (String) session.getAttribute("payrollUser");
        if (username == null && principal != null) {
            username = principal.getName();
        }
        
        if (username != null) {
            Optional<User> userOpt = userRepository.findByUsername(username);
            if (userOpt.isPresent()) {
                User user = userOpt.get();
                model.addAttribute("user", user);
                model.addAttribute("profile", user.getEmployeeProfile());
                
                String fullName = user.getFullName();
                String initials = "";
                if (fullName != null && !fullName.isEmpty()) {
                    String[] parts = fullName.split(" ");
                    if (parts.length > 0 && parts[0].length() > 0) {
                        initials += parts[0].substring(0, 1).toUpperCase();
                    }
                    if (parts.length > 1 && parts[1].length() > 0) {
                        initials += parts[1].substring(0, 1).toUpperCase();
                    }
                }
                model.addAttribute("initials", initials);
            }
        }
        return "payroll/payroll-tax";
    }

    /* ─── TAX CALCULATOR ────────────────────────────────────────── */

    @GetMapping("/tax-calculator")
    public String taxCalculator(HttpSession session, Model model, Principal principal) {
        if (!isPayrollAuthenticated(session)) return "redirect:/payroll-login";
        
        String username = (String) session.getAttribute("payrollUser");
        if (username == null && principal != null) {
            username = principal.getName();
        }
        
        if (username != null) {
            Optional<User> userOpt = userRepository.findByUsername(username);
            if (userOpt.isPresent()) {
                User user = userOpt.get();
                model.addAttribute("user", user);
                model.addAttribute("profile", user.getEmployeeProfile());
                
                String fullName = user.getFullName();
                String initials = "";
                if (fullName != null && !fullName.isEmpty()) {
                    String[] parts = fullName.split(" ");
                    if (parts.length > 0 && parts[0].length() > 0) {
                        initials += parts[0].substring(0, 1).toUpperCase();
                    }
                    if (parts.length > 1 && parts[1].length() > 0) {
                        initials += parts[1].substring(0, 1).toUpperCase();
                    }
                }
                model.addAttribute("initials", initials);
            }
        }
        return "payroll/tax-calculator";
    }

    /* ─── PF CONTRIBUTION ───────────────────────────────────────── */

    @GetMapping("/payroll/pf-contribution")
    public String pfContribution(HttpSession session, Model model, Principal principal) {
        if (!isPayrollAuthenticated(session)) return "redirect:/payroll-login";
        
        String username = (String) session.getAttribute("payrollUser");
        if (username == null && principal != null) {
            username = principal.getName();
        }
        
        if (username != null) {
            Optional<User> userOpt = userRepository.findByUsername(username);
            if (userOpt.isPresent()) {
                User user = userOpt.get();
                model.addAttribute("user", user);
                model.addAttribute("profile", user.getEmployeeProfile());
                
                String fullName = user.getFullName();
                String initials = "";
                if (fullName != null && !fullName.isEmpty()) {
                    String[] parts = fullName.split(" ");
                    if (parts.length > 0 && parts[0].length() > 0) {
                        initials += parts[0].substring(0, 1).toUpperCase();
                    }
                    if (parts.length > 1 && parts[1].length() > 0) {
                        initials += parts[1].substring(0, 1).toUpperCase();
                    }
                }
                model.addAttribute("initials", initials);
            }
        }
        return "payroll/pf-contribution";
    }

    /* ─── MY PROFILE ────────────────────────────────────────────── */

    @GetMapping("/payroll/my-profile")
    public String myProfile(HttpSession session, Model model, Principal principal) {
        if (!isPayrollAuthenticated(session)) return "redirect:/payroll-login";
        
        String username = (String) session.getAttribute("payrollUser");
        if (username == null && principal != null) {
            username = principal.getName();
        }
        
        if (username != null) {
            Optional<User> userOpt = userRepository.findByUsername(username);
            if (userOpt.isPresent()) {
                User user = userOpt.get();
                model.addAttribute("user", user);
                model.addAttribute("profile", user.getEmployeeProfile());
                
                // Get initials for Avatar
                String fullName = user.getFullName();
                String initials = "";
                if (fullName != null && !fullName.isEmpty()) {
                    String[] parts = fullName.split(" ");
                    if (parts.length > 0 && parts[0].length() > 0) {
                        initials += parts[0].substring(0, 1).toUpperCase();
                    }
                    if (parts.length > 1 && parts[1].length() > 0) {
                        initials += parts[1].substring(0, 1).toUpperCase();
                    }
                }
                model.addAttribute("initials", initials);
            }
        }
        return "payroll/my-profile";
    }

    /* ─── MY QUERIES (Helpdesk) ─────────────────────────────────── */

    @GetMapping("/payroll/my-queries")
    public String myQueries(HttpSession session, Model model, Principal principal) {
        if (!isPayrollAuthenticated(session)) return "redirect:/payroll-login";
        
        String username = (String) session.getAttribute("payrollUser");
        if (username == null && principal != null) {
            username = principal.getName();
        }
        
        if (username != null) {
            Optional<User> userOpt = userRepository.findByUsername(username);
            if (userOpt.isPresent()) {
                User user = userOpt.get();
                model.addAttribute("user", user);
                model.addAttribute("profile", user.getEmployeeProfile());
                
                String fullName = user.getFullName();
                String initials = "";
                if (fullName != null && !fullName.isEmpty()) {
                    String[] parts = fullName.split(" ");
                    if (parts.length > 0 && parts[0].length() > 0) {
                        initials += parts[0].substring(0, 1).toUpperCase();
                    }
                    if (parts.length > 1 && parts[1].length() > 0) {
                        initials += parts[1].substring(0, 1).toUpperCase();
                    }
                }
                model.addAttribute("initials", initials);
            }
        }
        return "payroll/my-queries";
    }

    /* ─── TRAVEL & EXPENSES ─────────────────────────────────────── */

    @GetMapping("/payroll/travel-expenses")
    public String travelExpenses(HttpSession session, Model model, Principal principal) {
        if (!isPayrollAuthenticated(session)) return "redirect:/payroll-login";
        
        String username = (String) session.getAttribute("payrollUser");
        if (username == null && principal != null) {
            username = principal.getName();
        }
        
        if (username != null) {
            Optional<User> userOpt = userRepository.findByUsername(username);
            if (userOpt.isPresent()) {
                User user = userOpt.get();
                model.addAttribute("user", user);
                model.addAttribute("profile", user.getEmployeeProfile());
                
                String fullName = user.getFullName();
                String initials = "";
                if (fullName != null && !fullName.isEmpty()) {
                    String[] parts = fullName.split(" ");
                    if (parts.length > 0 && parts[0].length() > 0) {
                        initials += parts[0].substring(0, 1).toUpperCase();
                    }
                    if (parts.length > 1 && parts[1].length() > 0) {
                        initials += parts[1].substring(0, 1).toUpperCase();
                    }
                }
                model.addAttribute("initials", initials);
            }
        }
        return "payroll/travel-expenses";
    }

    /* ─── FAQS ──────────────────────────────────────────────────── */

    @GetMapping("/payroll/faqs")
    public String faqs(HttpSession session, Model model, Principal principal) {
        if (!isPayrollAuthenticated(session)) return "redirect:/payroll-login";
        
        String username = (String) session.getAttribute("payrollUser");
        if (username == null && principal != null) {
            username = principal.getName();
        }
        
        if (username != null) {
            Optional<User> userOpt = userRepository.findByUsername(username);
            if (userOpt.isPresent()) {
                User user = userOpt.get();
                model.addAttribute("user", user);
                model.addAttribute("profile", user.getEmployeeProfile());
                
                String fullName = user.getFullName();
                String initials = "";
                if (fullName != null && !fullName.isEmpty()) {
                    String[] parts = fullName.split(" ");
                    if (parts.length > 0 && parts[0].length() > 0) {
                        initials += parts[0].substring(0, 1).toUpperCase();
                    }
                    if (parts.length > 1 && parts[1].length() > 0) {
                        initials += parts[1].substring(0, 1).toUpperCase();
                    }
                }
                model.addAttribute("initials", initials);
            }
        }
        return "payroll/faqs";
    }

    /* ─── PAYROLL LOGOUT ────────────────────────────────────────── */

    @GetMapping("/payroll/logout")
    public String payrollLogout(HttpSession session) {
        session.removeAttribute("payrollAuthenticated");
        session.removeAttribute("payrollUser");
        return "redirect:/employee/dashboard";
    }

    /* ─── UPDATE BANK DETAILS ───────────────────────────────────── */

    @PostMapping("/payroll/update-bank-details")
    public String updateBankDetails(
            @RequestParam("bankAccountHolder") String bankAccountHolder,
            @RequestParam("bankAccountNumber") String bankAccountNumber,
            @RequestParam("bankIfscCode") String bankIfscCode,
            @RequestParam("bankName") String bankName,
            @RequestParam("bankBranch") String bankBranch,
            @RequestParam("bankAccountType") String bankAccountType,
            HttpSession session,
            Principal principal) {
        
        if (!isPayrollAuthenticated(session)) return "redirect:/payroll-login";
        
        String username = (String) session.getAttribute("payrollUser");
        if (username == null && principal != null) {
            username = principal.getName();
        }
        
        if (username != null) {
            Optional<User> userOpt = userRepository.findByUsername(username);
            if (userOpt.isPresent()) {
                User user = userOpt.get();
                EmployeeProfile profile = user.getEmployeeProfile();
                if (profile != null) {
                    profile.setBankAccountHolder(bankAccountHolder);
                    profile.setBankAccountNumber(bankAccountNumber);
                    profile.setBankIfscCode(bankIfscCode);
                    profile.setBankName(bankName);
                    profile.setBankBranch(bankBranch);
                    profile.setBankAccountType(bankAccountType);
                    userRepository.save(user);
                }
            }
        }
        
        return "redirect:/payroll/my-profile";
    }

}