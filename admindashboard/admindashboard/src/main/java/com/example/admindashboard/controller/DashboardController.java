package com.example.admindashboard.controller;

import com.example.admindashboard.model.LeaveRequest;
import com.example.admindashboard.model.Meeting;
import com.example.admindashboard.model.Timesheet;
import com.example.admindashboard.model.User;
import com.example.admindashboard.repository.LeaveRequestRepository;
import com.example.admindashboard.repository.UserRepository;
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
import com.whitecircle.hrms.repository.ServiceRequestRepository;
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

    // --- 1. LOGIN PAGE MAPPINGS ---

    // Maps localhost:8080/ to the login page
    @GetMapping("/")
    public String rootRedirect() {
        return "redirect:/login";
    }

    // Maps localhost:8080/login to the login page (REQUIRED by SecurityConfig)
    @GetMapping("/login")
    public String showLoginPage() {
        return "index"; // Looks for index.html
    }

    // --- 2. POST-LOGIN TRAFFIC COP ---

    // SecurityConfig sends successful logins here. We check the role and redirect.
    @GetMapping("/default-redirect")
    public String defaultRedirect(HttpServletRequest request) {
        if (request.isUserInRole("ADMIN")) {
            return "redirect:/admin/dashboard";
        } else if (request.isUserInRole("CLIENT")) {
            return "redirect:/client/dashboard";
        } else if (request.isUserInRole("EMPLOYEE")) { // Explicit check
            return "redirect:/employee/dashboard";
        }
        return "redirect:/login?error=true";
    }

    // --- PROTECTED ROUTES

    // Shows Admin Dashboard and Updated count for Employees and Clients
    @GetMapping("/admin/dashboard")
    public String showAdminDashboard(Model model) {
        // 1. Fetch live counts from the database
        long totalEmployees = userRepository.countByRole("EMPLOYEE");
        long totalClients = userRepository.countByRole("CLIENT");
        // 2. Send the numbers to the HTML page
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

    // -- EMPLOYEE PORTAL PAGES --

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
            // Capture User-Entity fields manually
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

        // 1. UPDATE USER ENTITY FIELDS
        if (mobileNumber != null) user.setMobileNumber(mobileNumber.trim());
        if (city != null) user.setCity(city.trim());
        if (country != null) user.setCountry(country.trim());
        if (experience != null) user.setExperience(experience.trim());
        if (joiningDate != null) user.setJoiningDate(joiningDate);

        // 2. FETCH OR CREATE PROFILE
        EmployeeProfile existingProfile = user.getEmployeeProfile();
        if (existingProfile == null) {
            existingProfile = new EmployeeProfile();
            existingProfile.setUser(user);
        }

        // 3. UPDATE PROFILE FIELDS (Personal, Education, Emergency)
        existingProfile.setDob(formProfile.getDob());
        existingProfile.setGender(formProfile.getGender());
        existingProfile.setPersonalEmail(formProfile.getPersonalEmail());
        existingProfile.setAadharNo(formProfile.getAadharNo());
        existingProfile.setPanNo(formProfile.getPanNo());
        existingProfile.setPermanentAddress(formProfile.getPermanentAddress());
        existingProfile.setWorkingAddress(formProfile.getWorkingAddress()); // Make sure to save this!

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

        // 4. SAVE
        user.setEmployeeProfile(existingProfile);
        userRepository.save(user);

        redirectAttributes.addFlashAttribute("successMessage", "Master Profile updated successfully!");
        return "redirect:/employee/full-profile";
    }

    @GetMapping("/employee/profile/edit")
    public String showEditMyProfileForm(Principal principal, Model model) {
        // Fetch the currently logged-in employee using Principal
        User currentEmployee = userService.findByUsername(principal.getName());
        model.addAttribute("employee", currentEmployee);

        return "edit-my-profile";
    }

    @PostMapping("/employee/profile/edit")
    public String updateMyProfile(@ModelAttribute("employee") User updatedEmployee,
                                  Principal principal,
                                  RedirectAttributes redirectAttributes) {

        // Securely update ONLY personal details using the logged-in user's identity
        userService.updateEmployeePersonalDetails(principal.getName(), updatedEmployee);

        // Send a success message back to the main profile page
        redirectAttributes.addFlashAttribute("successMessage", "Your personal details have been updated successfully!");

        return "redirect:/employee/profile";
    }

    //
    @GetMapping("/conference-room")
    public String showConferencePage(Model model, Principal principal) {
        // 1. Identify exactly who is looking at the page
        String currentUsername = principal.getName();
        User currentUser = userRepository.findByUsername(currentUsername).orElse(null);

        // 2. Fetch ALL upcoming meetings from the database
        List<Meeting> allUpcomingMeetings = meetingRepository
                .findByMeetingDateGreaterThanEqualOrderByMeetingDateAscStartTimeAsc(LocalDate.now());

        // 3. FILTER LOGIC: Keep only the meetings that belong to this user
        List<Meeting> myMeetings = allUpcomingMeetings.stream().filter(meeting -> {

            // Condition A: I am the person who booked the meeting
            if (meeting.getOrganizer().getUsername().equals(currentUsername)) {
                return true;
            }

            // Condition B: I was specifically invited (My ID is in the text box)
            if (meeting.getSpecificEmployeeIds() != null && meeting.getSpecificEmployeeIds().contains(currentUsername)) {
                return true;
            }

            // Condition C: It is a "Team" meeting, and we share the same Department/Business Unit
            if ("TEAM".equals(meeting.getParticipantType()) && currentUser != null && currentUser.getBusinessUnit() != null) {
                if (currentUser.getBusinessUnit().equals(meeting.getOrganizer().getBusinessUnit())) {
                    return true;
                }
            }
            // If none of the above are true, hide the meeting!
            return false;
        }).toList();

        // 4. Pass ONLY the filtered list to the HTML page
        model.addAttribute("meetings", myMeetings);

        // Pass the user object to the page so we can check their role
        model.addAttribute("user", currentUser);

        return "conference-room";
    }

    @GetMapping("/apply-leave")
    public String showApplyLeavePage(Model model, Principal principal) {
        String username = principal.getName();

        // Use .orElse(null) to handle the Optional return type
        User currentUser = userRepository.findByUsername(username).orElse(null);

        if (currentUser == null) {
            // Handle the case where the user isn't in the DB (optional)
            return "redirect:/login";
        }

        model.addAttribute("user", currentUser);
        return "apply-leave";
    }
    
    @GetMapping("/attendance")
    public String showAttendanceRegulation(Model model, Principal principal) {
        // 1. Identify the logged-in user
        String username = principal.getName();

        // 2. Fetch user from DB using the fixed Repository (Optional handle)
        User user = userRepository.findByUsername(username).orElse(null);

        // 3. Add to model
        model.addAttribute("user", user);

        // 4. (Optional) Add current week for the date picker
        model.addAttribute("currentWeek", LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-'W'ww")));

        return "attendance";
    }

    @GetMapping("/employee/my-timesheets")
    public String showMyTimesheets() {
        return "my-timesheets"; // Looks for my-timesheets.html
    }


    @GetMapping("/email-signature")
    public String showEmailSignaturePage(Model model, Principal principal) {
        // 1. Get the username (ID) of the currently logged-in employee
        String username = principal.getName();

        // 2. Fetch their full profile from the database
        User currentUser = userRepository.findByUsername(username).orElse(new User());

        // 3. Add the user object to the model so Thymeleaf can use it
        model.addAttribute("user", currentUser);

        // Return the name of your HTML file
        return "email-signature";
    }


    @GetMapping("/password-reset")
    public String showPasswordResetPage() { return "password-reset"; }

    @GetMapping("/my-whitecircle")
    public String showMyWhiteCircle() { return "my-whitecircle"; }

    @GetMapping("/employee/erp-timesheet")
    public String showErpAndTimesheet() {
        return "erp-and-timesheet";
    }

    @GetMapping("/employee/create-timesheet")
    public String showCreateTimesheet() {
        return "create-timesheet";
    }

    @GetMapping("/employee/timesheet-report")
    public String showTimesheetReport() {
        return "timesheet-report";
    }

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

            List<com.whitecircle.hrms.model.ServiceRequest> userRequests = serviceRequestRepository.findByEmployeeIdOrderBySubmissionDateDesc(loginId);
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

            List<com.whitecircle.hrms.model.ServiceRequest> userRequests = serviceRequestRepository.findByEmployeeIdOrderBySubmissionDateDesc(loginId);
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

            List<com.whitecircle.hrms.model.ServiceRequest> userRequests = serviceRequestRepository.findByEmployeeIdOrderBySubmissionDateDesc(loginId);
            model.addAttribute("myRequests", userRequests);

        } else {
            model.addAttribute("user", new User());
        }
        return "report-incident";
    }

    @GetMapping("/knowledge-base")
    public String showKnowledgeBasePage() {
        return "knowledge-base";
    }


    @GetMapping("/payroll")
    public String showPayrollPage() { return "payroll"; }

    @GetMapping("/holiday-list")
    public String showHolidayList() {
        return "holiday-list"; // This matches the name of your our HTML file
    }



    // -- CLIENT PORTAL PAGES --

    @GetMapping("/client/profile")
    public String showClientProfile() {
        return "client-profile";
    }


    // -- ADMIN PORTAL PAGE CONTROLLER--

    // Add New Employee page
    @GetMapping("/admin/add-employee")
    public String showAddEmployeeForm(Model model) {
        // We add a new User object so the form has something to hold the data
        model.addAttribute("user", new User());
        return "add-employee";
    }

    @PostMapping("/admin/add-employee-submit")
    public String addEmployee(@ModelAttribute User user, Model model) {

        // 1. Get the username and clean it up
        String rawUsername = user.getUsername() != null ? user.getUsername().trim() : "";

        // 2. NEW: Prefix Validation (Check for 'EMP')
        if (!rawUsername.toUpperCase().startsWith("EMP")) {
            model.addAttribute("errorMessage", "Invalid ID Format! Employee IDs must start with 'EMP' (e.g., EMP101). Admin (ADM) IDs cannot be created here.");
            return "add-employee"; // Returns to form with error, keeping user data intact
        }

        // 3. NEW: Mandatory Field Validation
        if (user.getDesignation() == null || user.getDesignation().trim().isEmpty()) {
            model.addAttribute("errorMessage", "Designation is a mandatory field.");
            return "add-employee";
        }

        if (user.getMobileNumber() == null || user.getMobileNumber().trim().isEmpty()) {
            model.addAttribute("errorMessage", "Mobile Number is a mandatory field.");
            return "add-employee";
        }

        // 4. Existing Duplicate Check
        if (userRepository.existsByUsername(rawUsername)) {
            model.addAttribute("errorMessage", "Employee ID '" + rawUsername + "' already exists. Please use a different ID.");
            return "add-employee";
        }

        // 5. If all checks pass, set defaults and save
        user.setUsername(rawUsername.toUpperCase()); // Force Uppercase for clean records
        user.setPassword("{noop}welcome123");
        user.setRole("EMPLOYEE");
        userRepository.save(user);

        // Redirect to the Employee Report so they can see the new entry immediately
        return "redirect:/admin/reports?type=employee";
    }

    // Timesheet Approval Page
    @GetMapping("/admin/timesheet-approval")
    public String showTimesheetApprovalPage() {
        return "admin-timesheet-approval";
    }

    // Attendance Regularization page
    @GetMapping("/admin/attendance-regularization")
    public String showRegularizationPage() {
        return "admin-attendance-regularization";
    }

    @PostMapping("/admin/timesheets/approve/{id}")
    public String approveTimesheet(@PathVariable Long id, java.security.Principal principal) {
        // 1. Find the timesheet
        Timesheet timesheet = timesheetRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Invalid timesheet Id:" + id));

        // 2. Update Status
        timesheet.setStatus("Approved");

        // 3. CAPTURE ADMIN NAME (Fixes the blank "Approved By" column)
        if (principal != null) {
            timesheet.setApprovedBy(principal.getName());
        } else {
            timesheet.setApprovedBy("Admin");
        }

        // 4. Save to Database
        timesheetRepository.save(timesheet);
        return "redirect:/admin/timesheet-approval";
    }

    @GetMapping("/admin/add-client")
    public String showAddClientPage() {
        return "add-new-client";
    }

    @PostMapping("/admin/save-client")
    public String saveClient(@ModelAttribute Client client, RedirectAttributes redirectAttributes) {

        // 1. Save the business profile to the 'clients' table
        clientRepository.save(client);

        // 2. Auto-generate the Login Account in the 'users' table
        User clientUser = new User();
        clientUser.setUsername(client.getClientId());
        clientUser.setFullName(client.getContactPerson() + " (" + client.getCompanyName() + ")");
        clientUser.setRole("CLIENT"); // Give them the strict Client role
        clientUser.setPassword("{noop}welcome123");

        userRepository.save(clientUser);

        // Add a success message (letting the admin know the default password)
        redirectAttributes.addFlashAttribute("successMessage",
                "Client " + client.getCompanyName() + " successfully onboarded! Login ID: " + client.getClientId() + " | Temp Password: Welcome@123");

        return "redirect:/admin/dashboard";
    }

    // 1. Show the Manage Clients HTML Page
    @GetMapping("/admin/manage-clients")
    public String showManageClientsPage() {
        return "admin-manage-clients";
    }

    // 2. API to fetch all clients for the table
    @GetMapping("/api/admin/clients")
    @ResponseBody
    public ResponseEntity<List<Client>> getAllClients() {
        return ResponseEntity.ok(clientRepository.findAll());
    }

    // 3. API to Delete a Client (AND their login account)
    @DeleteMapping("/api/admin/clients/{id}")
    @ResponseBody
    public ResponseEntity<?> deleteClient(@PathVariable Long id) {
        Optional<Client> clientOpt = clientRepository.findById(id);

        if (clientOpt.isPresent()) {
            Client client = clientOpt.get();
            String loginId = client.getClientId();

            // Delete the business profile
            clientRepository.delete(client);

            Optional<User> userOpt = userRepository.findByUsername(loginId);
            if (userOpt.isPresent()) {
                userRepository.delete(userOpt.get());
            }

            return ResponseEntity.ok("Client and login credentials deleted successfully.");
        }
        return ResponseEntity.notFound().build();
    }

    // 4. API to Update an Existing Client
    @PostMapping("/api/admin/clients/update")
    @ResponseBody
    public ResponseEntity<?> updateClient(@ModelAttribute Client updatedClient) {
        Optional<Client> existingOpt = clientRepository.findById(updatedClient.getId());

        if (existingOpt.isPresent()) {
            Client existing = existingOpt.get();

            // Note: We deliberately DO NOT update the clientId here,
            // because it is tied to their login account in the users table!
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
            // Simple search by name if they type in the search bar
            staffList = userRepository.findByRoleAndFullNameContainingIgnoreCase("EMPLOYEE", keyword);
        } else {
            // Default: Show all employees sorted alphabetically
            staffList = userRepository.findByRoleOrderByUsernameAsc("EMPLOYEE");
        }

        model.addAttribute("staffList", staffList);
        model.addAttribute("keyword", keyword);
        return "admin-staff";
    }

    // 1. Show the Edit Form
    @GetMapping("/admin/staff/edit/{id}")
    public String showEditEmployeeForm(@PathVariable("id") Long id, Model model) {
        User employee = userService.findById(id);
        model.addAttribute("employee", employee);
        return "admin/edit-employee";
    }

    // 2. Handle the Form Submission
    @PostMapping("/admin/staff/edit/{id}")
    public String updateEmployee(@PathVariable("id") Long id, @ModelAttribute("employee") User updatedEmployee, RedirectAttributes redirectAttributes) {
        userService.updateEmployeeProfessionalDetails(id, updatedEmployee);
        // Add the flash message that will survive the redirect
        redirectAttributes.addFlashAttribute("successMessage", "Employee Details Updated Successfully!");

        return "redirect:/admin/staff";
    }


    @Autowired
    private com.whitecircle.hrms.repository.ServiceRequestRepository repository;

    @GetMapping("/admin-helpdesk-requests")
    public String viewAdminHelpdeskPortal(Model model) {
        // Fetch all requests from database
        List<com.whitecircle.hrms.model.ServiceRequest> allRequests = repository.findAll();

        // Pass the list to Thymeleaf
        model.addAttribute("allRequests", allRequests);

        // Optional: Pass counts for the red badges on the tabs
        long softwareCount = allRequests.stream().filter(r -> "SOFTWARE".equals(r.getType())).count();
        long hardwareCount = allRequests.stream().filter(r -> "HARDWARE".equals(r.getType())).count();

        model.addAttribute("softwareCount", softwareCount);
        model.addAttribute("hardwareCount", hardwareCount);

        return "admin-helpdesk-requests";
    }

    @GetMapping("/api/employees/search")
    @ResponseBody
    public ResponseEntity<List<Map<String, Object>>> globalSearch(@RequestParam("query") String query) {

        // 1. Return empty if query is too short
        if (query == null || query.trim().length() < 2) {
            return ResponseEntity.ok(Collections.emptyList());
        }

        String keyword = query.trim();
        List<Map<String, Object>> results = new ArrayList<>();

        // 2. Search Employees (User Table)
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

        // 3. Search Real Clients (Client Table)
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

        // 4. Limit to top 6 results
        if (results.size() > 6) {
            results = results.subList(0, 6);
        }

        return ResponseEntity.ok(results);
    }


}