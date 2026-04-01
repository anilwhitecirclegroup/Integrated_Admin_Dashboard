package com.example.admindashboard.controller;

import com.example.admindashboard.model.LeaveRequest;
import com.example.admindashboard.model.Meeting;
import com.example.admindashboard.model.Timesheet;
import com.example.admindashboard.model.User;
import com.example.admindashboard.model.ServiceRequest; // Added correct import
import com.example.admindashboard.repository.LeaveRequestRepository;
import com.example.admindashboard.repository.UserRepository;
import com.example.admindashboard.repository.ServiceRequestRepository; // Added correct import
import com.example.admindashboard.service.EmailService; // Added Email Service
import org.springframework.beans.factory.annotation.Autowired;
import java.security.Principal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;

import com.example.admindashboard.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import com.example.admindashboard.repository.TimesheetRepository;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import com.example.admindashboard.model.EmployeeProfile;
import com.example.admindashboard.model.Client;
import com.example.admindashboard.repository.ClientRepository;

@Controller
public class DashboardController {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private TimesheetRepository timesheetRepository;

    @Autowired
    private UserService userService;

    @Autowired
    private com.example.admindashboard.repository.MeetingRepository meetingRepository;

    @Autowired
    private ServiceRequestRepository serviceRequestRepository;

    @Autowired
    private ClientRepository clientRepository;

    // INJECT THE EMAIL SERVICE
    @Autowired
    private EmailService emailService;

    // --- 1. LOGIN PAGE MAPPINGS ---

    @GetMapping("/")
    public String rootRedirect() {
        return "redirect:/login";
    }

    @GetMapping("/login")
    public String showLoginPage() {
        return "index";
    }

    // --- 2. POST-LOGIN TRAFFIC COP ---

    @GetMapping("/default-redirect")
    public String defaultRedirect(HttpServletRequest request) {
        if (request.isUserInRole("ADMIN")) {
            return "redirect:/admin/dashboard";
        } else if (request.isUserInRole("CLIENT")) {
            return "redirect:/client/dashboard";
        } else if (request.isUserInRole("EMPLOYEE")) {
            return "redirect:/employee/dashboard";
        }
        return "redirect:/login?error=true";
    }

    // --- PROTECTED ROUTES ---

    @GetMapping("/admin/dashboard")
    public String showAdminDashboard(Model model) {
        long totalEmployees = userRepository.countByRole("EMPLOYEE");
        long totalClients = userRepository.countByRole("CLIENT");
        model.addAttribute("empCount", totalEmployees);
        model.addAttribute("clientCount", totalClients);
        return "admin-dashboard";
    }

    @GetMapping("/client/dashboard")
    public String showClientDashboard() { return "client-dashboard"; }

    @GetMapping("/employee/dashboard")
    public String showEmployeeDashboard() { return "employee-dashboard"; }

    // --- EMPLOYEE PROFILE SECTION ---
    @GetMapping("/employee/profile")
    public String viewProfile(Model model, Principal principal) {
        String username = principal.getName();
        User user = userRepository.findByUsername(username).orElse(null);
        model.addAttribute("user", user);
        return "employee-profile";
    }

    @GetMapping("/my-profile")
    public String showProfilePage() { return "my-profile"; }

    @GetMapping("/employee/full-profile")
    public String showFullProfile(Model model, Principal principal) {
        String username = principal.getName();
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (user.getEmployeeProfile() == null) {
            user.setEmployeeProfile(new EmployeeProfile());
        }
        model.addAttribute("user", user);
        return "full-profile";
    }

    @PostMapping("/employee/profile/save-detailed")
    public String saveDetailedProfile(
            @ModelAttribute EmployeeProfile formProfile,
            @RequestParam(value = "mobileNumber", required = false) String mobileNumber,
            @RequestParam(value = "city", required = false) String city,
            @RequestParam(value = "country", required = false) String country,
            @RequestParam(value = "experience", required = false) String experience,
            @RequestParam(value = "joiningDate", required = false) LocalDate joiningDate,
            Principal principal,
            RedirectAttributes redirectAttributes) {

        String username = principal.getName();
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (mobileNumber != null) user.setMobileNumber(mobileNumber.trim());
        if (city != null) user.setCity(city.trim());
        if (country != null) user.setCountry(country.trim());
        if (experience != null) user.setExperience(experience.trim());
        if (joiningDate != null) user.setJoiningDate(joiningDate);

        EmployeeProfile existingProfile = user.getEmployeeProfile();
        if (existingProfile == null) {
            existingProfile = new EmployeeProfile();
            existingProfile.setUser(user);
        }

        existingProfile.setDob(formProfile.getDob());
        existingProfile.setGender(formProfile.getGender());
        existingProfile.setPersonalEmail(formProfile.getPersonalEmail());
        existingProfile.setAadharNo(formProfile.getAadharNo());
        existingProfile.setPanNo(formProfile.getPanNo());
        existingProfile.setPermanentAddress(formProfile.getPermanentAddress());
        existingProfile.setWorkingAddress(formProfile.getWorkingAddress());

        existingProfile.setQual1Title(formProfile.getQual1Title());
        existingProfile.setQual1Inst(formProfile.getQual1Inst());
        existingProfile.setQual1Year(formProfile.getQual1Year());
        existingProfile.setQual2Title(formProfile.getQual2Title());
        existingProfile.setQual2Inst(formProfile.getQual2Inst());
        existingProfile.setQual2Year(formProfile.getQual2Year());

        existingProfile.setEmergencyContactName(formProfile.getEmergencyContactName());
        existingProfile.setRelationWithEmployee(formProfile.getRelationWithEmployee());
        existingProfile.setEmergencyPhone(formProfile.getEmergencyPhone());
        existingProfile.setAltMobile(formProfile.getAltMobile());

        user.setEmployeeProfile(existingProfile);
        userRepository.save(user);

        redirectAttributes.addFlashAttribute("successMessage", "Master Profile updated successfully!");
        return "redirect:/employee/full-profile";
    }

    @GetMapping("/employee/profile/edit")
    public String showEditMyProfileForm(Principal principal, Model model) {
        User currentEmployee = userService.findByUsername(principal.getName());
        model.addAttribute("employee", currentEmployee);
        return "edit-my-profile";
    }

    @PostMapping("/employee/profile/edit")
    public String updateMyProfile(@ModelAttribute("employee") User updatedEmployee,
                                  Principal principal,
                                  RedirectAttributes redirectAttributes) {

        userService.updateEmployeePersonalDetails(principal.getName(), updatedEmployee);
        redirectAttributes.addFlashAttribute("successMessage", "Your personal details have been updated successfully!");
        return "redirect:/employee/profile";
    }

    @GetMapping("/conference-room")
    public String showConferencePage(Model model, Principal principal) {
        String currentUsername = principal.getName();
        User currentUser = userRepository.findByUsername(currentUsername).orElse(null);

        List<Meeting> allUpcomingMeetings = meetingRepository
                .findByMeetingDateGreaterThanEqualOrderByMeetingDateAscStartTimeAsc(LocalDate.now());

        List<Meeting> myMeetings = allUpcomingMeetings.stream().filter(meeting -> {
            if (meeting.getOrganizer().getUsername().equals(currentUsername)) return true;
            if (meeting.getSpecificEmployeeIds() != null && meeting.getSpecificEmployeeIds().contains(currentUsername)) return true;
            if ("TEAM".equals(meeting.getParticipantType()) && currentUser != null && currentUser.getBusinessUnit() != null) {
                if (currentUser.getBusinessUnit().equals(meeting.getOrganizer().getBusinessUnit())) return true;
            }
            return false;
        }).toList();

        model.addAttribute("meetings", myMeetings);
        model.addAttribute("user", currentUser);

        return "conference-room";
    }

    @GetMapping("/apply-leave")
    public String showApplyLeavePage(Model model, Principal principal) {
        String username = principal.getName();
        User currentUser = userRepository.findByUsername(username).orElse(null);

        if (currentUser == null) {
            return "redirect:/login";
        }

        model.addAttribute("user", currentUser);
        return "apply-leave";
    }

    @GetMapping("/attendance")
    public String showAttendanceRegulation(Model model, Principal principal) {
        String username = principal.getName();
        User user = userRepository.findByUsername(username).orElse(null);
        model.addAttribute("user", user);
        model.addAttribute("currentWeek", LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-'W'ww")));
        return "attendance";
    }

    @GetMapping("/employee/my-timesheets")
    public String showMyTimesheets() {
        return "my-timesheets";
    }

    @GetMapping("/email-signature")
    public String showEmailSignaturePage(Model model, Principal principal) {
        String username = principal.getName();
        User currentUser = userRepository.findByUsername(username).orElse(new User());
        model.addAttribute("user", currentUser);
        return "email-signature";
    }

    @GetMapping("/password-reset")
    public String showPasswordResetPage() { return "password-reset"; }

    @GetMapping("/my-whitecircle")
    public String showMyWhiteCircle() { return "my-whitecircle"; }

    @GetMapping("/employee/erp-timesheet")
    public String showErpAndTimesheet() { return "erp-and-timesheet"; }

    @GetMapping("/employee/create-timesheet")
    public String showCreateTimesheet() { return "create-timesheet"; }

    @GetMapping("/employee/timesheet-report")
    public String showTimesheetReport() { return "timesheet-report"; }

    @GetMapping("/my-timeoff")
    public String showMyTimeoff(Model model, Principal principal) {
        if (principal != null) {
            String loginId = principal.getName();
            User currentUser = userRepository.findByUsername(loginId).orElse(new User());
            model.addAttribute("user", currentUser);
        }
        return "my-timeoff";
    }

    @GetMapping("/tickets")
    public String showTicketsPage() { return "tickets"; }

    @GetMapping("/service-requests")
    public String showServiceRequests(Model model, Principal principal) {
        if (principal != null) {
            String loginId = principal.getName();
            User currentUser = userRepository.findByUsername(loginId).orElse(new User());
            model.addAttribute("user", currentUser);

            // FIXED: Removed the old whitecircle package path
            List<ServiceRequest> userRequests = serviceRequestRepository.findByEmployeeIdOrderBySubmissionDateDesc(loginId);
            model.addAttribute("myRequests", userRequests);

        } else {
            model.addAttribute("user", new User());
        }
        return "service-requests";
    }

    @GetMapping("/my-assets")
    public String showMyAssets(Model model, Principal principal) {
        if (principal != null) {
            String loginId = principal.getName();
            User currentUser = userRepository.findByUsername(loginId).orElse(new User());
            model.addAttribute("user", currentUser);

            // FIXED: Removed the old whitecircle package path
            List<ServiceRequest> userRequests = serviceRequestRepository.findByEmployeeIdOrderBySubmissionDateDesc(loginId);
            model.addAttribute("myRequests", userRequests);

        } else {
            model.addAttribute("user", new User());
        }
        return "my-assets";
    }

    @GetMapping("/report-incident")
    public String showReportIncident(Model model, Principal principal) {
        if (principal != null) {
            String loginId = principal.getName();
            User currentUser = userRepository.findByUsername(loginId).orElse(new User());
            model.addAttribute("user", currentUser);

            // FIXED: Removed the old whitecircle package path
            List<ServiceRequest> userRequests = serviceRequestRepository.findByEmployeeIdOrderBySubmissionDateDesc(loginId);
            model.addAttribute("myRequests", userRequests);

        } else {
            model.addAttribute("user", new User());
        }
        return "report-incident";
    }

    @GetMapping("/knowledge-base")
    public String showKnowledgeBasePage() { return "knowledge-base"; }

    @GetMapping("/payroll")
    public String showPayrollPage() { return "payroll"; }

    @GetMapping("/holiday-list")
    public String showHolidayList() { return "holiday-list"; }

    // -- CLIENT PORTAL PAGES --

    @GetMapping("/client/profile")
    public String showClientProfile() { return "client-profile"; }

    // -- ADMIN PORTAL PAGE CONTROLLER--

    @GetMapping("/admin/add-employee")
    public String showAddEmployeeForm(Model model) {
        model.addAttribute("user", new User());
        return "add-employee";
    }

    @PostMapping("/admin/add-employee-submit")
    public String addEmployee(@ModelAttribute User user, Model model) {

        String rawUsername = user.getUsername() != null ? user.getUsername().trim() : "";

        if (!rawUsername.toUpperCase().startsWith("EMP")) {
            model.addAttribute("errorMessage", "Invalid ID Format! Employee IDs must start with 'EMP' (e.g., EMP101). Admin (ADM) IDs cannot be created here.");
            return "add-employee";
        }

        if (user.getDesignation() == null || user.getDesignation().trim().isEmpty()) {
            model.addAttribute("errorMessage", "Designation is a mandatory field.");
            return "add-employee";
        }

        if (user.getMobileNumber() == null || user.getMobileNumber().trim().isEmpty()) {
            model.addAttribute("errorMessage", "Mobile Number is a mandatory field.");
            return "add-employee";
        }

        if (userRepository.existsByUsername(rawUsername)) {
            model.addAttribute("errorMessage", "Employee ID '" + rawUsername + "' already exists. Please use a different ID.");
            return "add-employee";
        }

        user.setUsername(rawUsername.toUpperCase());
        user.setPassword("{noop}welcome123");
        user.setRole("EMPLOYEE");
        userRepository.save(user);

        return "redirect:/admin/reports?type=employee";
    }

    @GetMapping("/admin/timesheet-approval")
    public String showTimesheetApprovalPage() { return "admin-timesheet-approval"; }

    @GetMapping("/admin/attendance-regularization")
    public String showRegularizationPage() { return "admin-attendance-regularization"; }

    @PostMapping("/admin/timesheets/approve/{id}")
    public String approveTimesheet(@PathVariable Long id, java.security.Principal principal) {
        Timesheet timesheet = timesheetRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Invalid timesheet Id:" + id));

        timesheet.setStatus("Approved");

        if (principal != null) {
            timesheet.setApprovedBy(principal.getName());
        } else {
            timesheet.setApprovedBy("Admin");
        }

        timesheetRepository.save(timesheet);

        // --- EMAIL TRIGGER START ---
        try {
            // Note: If your simple 'Timesheet' model here contains a User object, this will seamlessly email them!
            if (timesheet.getUser() != null && timesheet.getUser().getEmail() != null) {
                Map<String, Object> emailData = new HashMap<>();
                emailData.put("empName", timesheet.getUser().getFullName());

                emailService.sendRequestStatusUpdateToEmployee(
                        timesheet.getUser().getEmail(),
                        timesheet.getUser().getFullName(),
                        "Timesheet",
                        "Approved",
                        emailData
                );
            }
        } catch (Exception e) {
            System.err.println("⚠️ Warning: Could not trigger Timesheet Quick-Approve email: " + e.getMessage());
        }
        // --- EMAIL TRIGGER END ---

        return "redirect:/admin/timesheet-approval";
    }

    @GetMapping("/admin/add-client")
    public String showAddClientPage() { return "add-new-client"; }

    @PostMapping("/admin/save-client")
    public String saveClient(@ModelAttribute Client client, RedirectAttributes redirectAttributes) {
        clientRepository.save(client);

        User clientUser = new User();
        clientUser.setUsername(client.getClientId());
        clientUser.setFullName(client.getContactPerson() + " (" + client.getCompanyName() + ")");
        clientUser.setRole("CLIENT");
        clientUser.setPassword("{noop}welcome123");

        userRepository.save(clientUser);

        redirectAttributes.addFlashAttribute("successMessage",
                "Client " + client.getCompanyName() + " successfully onboarded! Login ID: " + client.getClientId() + " | Temp Password: Welcome@123");

        return "redirect:/admin/dashboard";
    }

    @GetMapping("/admin/manage-clients")
    public String showManageClientsPage() { return "admin-manage-clients"; }

    @GetMapping("/api/admin/clients")
    @ResponseBody
    public ResponseEntity<List<Client>> getAllClients() {
        return ResponseEntity.ok(clientRepository.findAll());
    }

    @DeleteMapping("/api/admin/clients/{id}")
    @ResponseBody
    public ResponseEntity<?> deleteClient(@PathVariable Long id) {
        Optional<Client> clientOpt = clientRepository.findById(id);

        if (clientOpt.isPresent()) {
            Client client = clientOpt.get();
            String loginId = client.getClientId();
            clientRepository.delete(client);

            Optional<User> userOpt = userRepository.findByUsername(loginId);
            if (userOpt.isPresent()) {
                userRepository.delete(userOpt.get());
            }

            return ResponseEntity.ok("Client and login credentials deleted successfully.");
        }
        return ResponseEntity.notFound().build();
    }

    @PostMapping("/api/admin/clients/update")
    @ResponseBody
    public ResponseEntity<?> updateClient(@ModelAttribute Client updatedClient) {
        Optional<Client> existingOpt = clientRepository.findById(updatedClient.getId());

        if (existingOpt.isPresent()) {
            Client existing = existingOpt.get();
            existing.setCompanyName(updatedClient.getCompanyName());
            existing.setDomain(updatedClient.getDomain());
            existing.setAccountStatus(updatedClient.getAccountStatus());
            existing.setContactPerson(updatedClient.getContactPerson());
            existing.setOfficialEmail(updatedClient.getOfficialEmail());
            existing.setPhoneNumber(updatedClient.getPhoneNumber());
            existing.setAssignedTeam(updatedClient.getAssignedTeam());
            existing.setProjectManager(updatedClient.getProjectManager());
            existing.setAssignedEmployee(updatedClient.getAssignedEmployee());
            existing.setBillingAddress(updatedClient.getBillingAddress());
            existing.setCity(updatedClient.getCity());
            existing.setCountry(updatedClient.getCountry());

            clientRepository.save(existing);
            return ResponseEntity.ok("Client updated successfully.");
        }
        return ResponseEntity.notFound().build();
    }

    @GetMapping("/admin/staff")
    public String showStaffDirectory(Model model, @RequestParam(required = false) String keyword) {
        List<User> staffList;

        if (keyword != null && !keyword.isEmpty()) {
            staffList = userRepository.findByRoleAndFullNameContainingIgnoreCase("EMPLOYEE", keyword);
        } else {
            staffList = userRepository.findByRoleOrderByUsernameAsc("EMPLOYEE");
        }

        model.addAttribute("staffList", staffList);
        model.addAttribute("keyword", keyword);
        return "admin-staff";
    }

    @GetMapping("/admin/staff/edit/{id}")
    public String showEditEmployeeForm(@PathVariable("id") Long id, Model model) {
        User employee = userService.findById(id);
        model.addAttribute("employee", employee);
        return "admin/edit-employee";
    }

    @PostMapping("/admin/staff/edit/{id}")
    public String updateEmployee(@PathVariable("id") Long id, @ModelAttribute("employee") User updatedEmployee, RedirectAttributes redirectAttributes) {
        userService.updateEmployeeProfessionalDetails(id, updatedEmployee);
        redirectAttributes.addFlashAttribute("successMessage", "Employee Details Updated Successfully!");
        return "redirect:/admin/staff";
    }

    @GetMapping("/admin-helpdesk-requests")
    public String viewAdminHelpdeskPortal(Model model) {
        // FIXED: Removed the duplicate repository autowiring and pointing to the correct one
        List<ServiceRequest> allRequests = serviceRequestRepository.findAll();

        model.addAttribute("allRequests", allRequests);

        long softwareCount = allRequests.stream().filter(r -> "SOFTWARE".equals(r.getType())).count();
        long hardwareCount = allRequests.stream().filter(r -> "HARDWARE".equals(r.getType())).count();

        model.addAttribute("softwareCount", softwareCount);
        model.addAttribute("hardwareCount", hardwareCount);

        return "admin-helpdesk-requests";
    }

    @GetMapping("/api/employees/search")
    @ResponseBody
    public ResponseEntity<List<Map<String, Object>>> globalSearch(@RequestParam("query") String query) {

        if (query == null || query.trim().length() < 2) {
            return ResponseEntity.ok(Collections.emptyList());
        }

        String keyword = query.trim();
        List<Map<String, Object>> results = new ArrayList<>();

        List<User> employees = userRepository.searchByKeyword(keyword);
        for (User u : employees) {
            if (!"CLIENT".equalsIgnoreCase(u.getRole())) {
                Map<String, Object> map = new HashMap<>();
                map.put("dbId", u.getId());
                map.put("fullName", u.getFullName());
                map.put("identifier", u.getUsername());
                map.put("designation", u.getDesignation() != null ? u.getDesignation() : "Employee");
                map.put("department", u.getBusinessUnit() != null ? u.getBusinessUnit() : "General");
                map.put("email", u.getEmail() != null ? u.getEmail() : "N/A");
                map.put("phone", u.getMobileNumber() != null ? u.getMobileNumber() : "N/A");
                map.put("role", "EMPLOYEE");
                results.add(map);
            }
        }

        List<Client> clients = clientRepository.searchClients(keyword);
        for (Client c : clients) {
            Map<String, Object> map = new HashMap<>();
            map.put("dbId", c.getId());
            map.put("fullName", c.getCompanyName());
            map.put("identifier", c.getClientId());
            map.put("designation", "Client Partner (" + c.getContactPerson() + ")");
            map.put("department", c.getDomain() != null ? c.getDomain() : "External");
            map.put("email", c.getOfficialEmail() != null ? c.getOfficialEmail() : "N/A");
            map.put("phone", c.getPhoneNumber() != null ? c.getPhoneNumber() : "N/A");
            map.put("role", "CLIENT");
            results.add(map);
        }

        if (results.size() > 6) {
            results = results.subList(0, 6);
        }

        return ResponseEntity.ok(results);
    }
}