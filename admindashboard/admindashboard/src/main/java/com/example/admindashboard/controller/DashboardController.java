package com.example.admindashboard.controller;

import com.example.admindashboard.model.*;
import com.example.admindashboard.repository.*;
import com.example.admindashboard.service.AuditLogService;
import com.example.admindashboard.service.EmailService;
import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.security.access.prepost.PreAuthorize;

import java.security.Principal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

import com.example.admindashboard.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

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

    @Autowired
    private EmailService emailService;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private AuditLogService auditLogService;

    @Autowired
    private ProjectRepository projectRepository;

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
        if (request.isUserInRole("SUPER_ADMIN") || request.isUserInRole("HR_ADMIN")) {
            return "redirect:/admin/dashboard";
        } else if (request.isUserInRole("CLIENT")) {
            return "redirect:/client/dashboard";
        } else {
            return "redirect:/employee/dashboard";
        }
    }

    // --- PROTECTED ROUTES ---

    // FIXED LOCK: Any user with the 'admin_dashboard_view' key can enter the portal
    @PreAuthorize("hasAuthority('admin_dashboard_view')")
    @GetMapping("/admin/dashboard")
    public String showAdminDashboard(Model model) {

        // Count all internal staff (Super Admin, HR, Manager, Employee, etc.)
        // EXCLUDES Clients and Soft-Deleted (INACTIVE) accounts
        long totalEmployees = userRepository.findAll().stream()
                .filter(u -> u.getRole() != null &&
                        !"CLIENT".equalsIgnoreCase(u.getRole().getRoleName()) &&
                        !"INACTIVE".equalsIgnoreCase(u.getStatus()))
                .count();

        // FIX: Count directly from the Client repository to perfectly match the Client Directory page.
        // This ignores any old, orphaned "User" test accounts (like CLI001) that don't have a real company profile.
        long totalClients = clientRepository.count();

        model.addAttribute("empCount", totalEmployees);
        model.addAttribute("clientCount", totalClients);

        return "admin-dashboard";
    }

    @GetMapping("/client/dashboard")
    public String showClientDashboard(Model model, Principal principal) {

        // 1. Get the currently logged-in user
        User loggedInUser = userRepository.findByUsername(principal.getName()).orElseThrow();

        // 2. Fetch the projects specifically assigned to this client
        List<Project> clientProjects = projectRepository.findByClientId(loggedInUser.getId());

        // 3. Attach the projects to the model so the Modal can see them
        model.addAttribute("clientProjects", clientProjects);

        return "client-dashboard";
    }

    @GetMapping("/employee/dashboard")
    public String showEmployeeDashboard() { return "employee-dashboard"; }

    // --- EMPLOYEE PROFILE SECTION (Self-Service - No locks needed) ---

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

        EmployeeProfile existingProfile = user.getEmployeeProfile();
        if (existingProfile == null) {
            existingProfile = new EmployeeProfile();
            existingProfile.setUser(user);
        }

        if (mobileNumber != null) existingProfile.setMobileNumber(mobileNumber.trim());
        if (city != null) existingProfile.setCity(city.trim());
        if (country != null) existingProfile.setCountry(country.trim());
        if (experience != null) existingProfile.setExperience(experience.trim());
        if (joiningDate != null) existingProfile.setJoiningDate(joiningDate);

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

    // --- VARIOUS EMPLOYEE PAGES ---

    @GetMapping("/conference-room")
    public String showConferencePage(Model model, Principal principal, Authentication authentication) {
        String currentUsername = principal.getName();
        User currentUser = userRepository.findByUsername(currentUsername).orElse(null);

        List<Meeting> allUpcomingMeetings = meetingRepository
                .findByMeetingDateGreaterThanEqualOrderByMeetingDateAscStartTimeAsc(LocalDate.now());

        List<Meeting> myMeetings = allUpcomingMeetings.stream().filter(meeting -> {
            if (meeting.getOrganizer().getUsername().equals(currentUsername)) return true;
            if (meeting.getSpecificEmployeeIds() != null && meeting.getSpecificEmployeeIds().contains(currentUsername)) return true;

            EmployeeProfile myProfile = currentUser != null ? currentUser.getEmployeeProfile() : null;
            EmployeeProfile organizerProfile = meeting.getOrganizer() != null ? meeting.getOrganizer().getEmployeeProfile() : null;

            if ("TEAM".equals(meeting.getParticipantType()) && myProfile != null && myProfile.getBusinessUnit() != null) {
                if (organizerProfile != null && myProfile.getBusinessUnit().equals(organizerProfile.getBusinessUnit())) {
                    return true;
                }
            }
            return false;
        }).toList();

        // FIXED: Dynamic Routing Logic for the "Back" Button
        String backUrl = "/employee/dashboard"; // Default for standard employees

        if (authentication != null && authentication.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("admin_dashboard_view"))) {
            backUrl = "/admin/dashboard"; // Override for anyone with Admin access
        }

        model.addAttribute("meetings", myMeetings);
        model.addAttribute("user", currentUser);
        model.addAttribute("backUrl", backUrl); // Send the dynamic URL to the HTML page

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
    public String showMyTimesheets() { return "my-timesheets"; }

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


    // --- ADMIN & MANAGER PORTAL ROUTES (Secured via RBAC) ---

    // LOCK: Need 'employee_create' permission to access or submit this form
    @PreAuthorize("hasAuthority('employee_create')")
    @GetMapping("/admin/add-employee")
    public String showAddEmployeeForm(Model model) {
        model.addAttribute("user", new User());
        return "add-employee";
    }

    @PreAuthorize("hasAuthority('employee_create')")
    @PostMapping("/admin/add-employee-submit")
    public String addEmployee(@ModelAttribute User user, Model model) {
        String rawUsername = user.getUsername() != null ? user.getUsername().trim() : "";

        if (!rawUsername.toUpperCase().startsWith("EMP")) {
            model.addAttribute("errorMessage", "Invalid ID Format! Employee IDs must start with 'EMP' (e.g., EMP101). Admin (ADM) IDs cannot be created here.");
            return "add-employee";
        }

        if (userRepository.existsByUsername(rawUsername)) {
            model.addAttribute("errorMessage", "Employee ID '" + rawUsername + "' already exists. Please use a different ID.");
            return "add-employee";
        }

        user.setUsername(rawUsername.toUpperCase());
        user.setPassword("{noop}welcome123");

        Role empRole = roleRepository.findByRoleName("EMPLOYEE").orElse(null);
        user.setRole(empRole);

        userRepository.save(user);
        return "redirect:/admin/reports?type=employee";
    }

    // FIXED LOCK: Approvals are strictly for leadership roles via permissions
    @PreAuthorize("hasAnyAuthority('attendance_approve', 'attendance_edit')")
    @GetMapping("/admin/timesheet-approval")
    public String showTimesheetApprovalPage() { return "admin-timesheet-approval"; }

    @PreAuthorize("hasAnyAuthority('attendance_approve', 'attendance_edit')")
    @GetMapping("/admin/attendance-regularization")
    public String showRegularizationPage() { return "admin-attendance-regularization"; }

    @PreAuthorize("hasAnyAuthority('attendance_approve', 'attendance_edit')")
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

        try {
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
            System.err.println("⚠️ Warning: Could not trigger Timesheet email: " + e.getMessage());
        }

        return "redirect:/admin/timesheet-approval";
    }

    // LOCK: Client/Company configuration is secured by 'settings_manage_company'
    @PreAuthorize("hasAuthority('settings_manage_company')")
    @GetMapping("/admin/add-client")
    public String showAddClientPage() { return "add-new-client"; }

    @PreAuthorize("hasAuthority('settings_manage_company')")
    @PostMapping("/admin/save-client")
    public String saveClient(@ModelAttribute Client client, RedirectAttributes redirectAttributes) {

        // 1. Create the Authentication User Account FIRST
        User clientUser = new User();
        clientUser.setUsername(client.getClientId());
        clientUser.setFullName(client.getContactPerson() + " (" + client.getCompanyName() + ")");

        // Fixed: Matches the success message password exactly
        clientUser.setPassword("{noop}client123");

        Role clientRole = roleRepository.findByRoleName("CLIENT").orElse(null);
        clientUser.setRole(clientRole);

        // 2. Link the entities together (Bidirectional Mapping)
        client.setUser(clientUser);
        clientUser.setClientProfile(client);

        // 3. Save to database. Saving the User will cascade and save the Client properly linked!
        userRepository.save(clientUser);
        clientRepository.save(client); // Ensures the Client table gets the foreign key updated

        redirectAttributes.addFlashAttribute("successMessage",
                "Client " + client.getCompanyName() + " successfully onboarded! Login ID: " + client.getClientId() + " | Temp Password: client123");

        return "redirect:/admin/dashboard";
    }

    @PreAuthorize("hasAuthority('settings_manage_company')")
    @GetMapping("/admin/manage-clients")
    public String showManageClientsPage() { return "admin-manage-clients"; }

    @PreAuthorize("hasAuthority('settings_manage_company')")
    @GetMapping("/api/admin/clients")
    @ResponseBody
    public ResponseEntity<List<Client>> getAllClients() {
        return ResponseEntity.ok(clientRepository.findAll());
    }

    @PreAuthorize("hasAuthority('settings_manage_company')")
    @DeleteMapping("/api/admin/clients/{id}")
    @ResponseBody
    public ResponseEntity<?> deleteClient(@PathVariable Long id) {
        Optional<Client> clientOpt = clientRepository.findById(id);

        if (clientOpt.isPresent()) {
            Client client = clientOpt.get();
            User associatedUser = client.getUser(); // Get the dynamically linked user

            // Delete the client profile first
            clientRepository.delete(client);

            // Delete the login credentials so they can't log in anymore
            if (associatedUser != null) {
                userRepository.delete(associatedUser);
            }

            return ResponseEntity.ok("Client profile and login credentials deleted successfully.");
        }
        return ResponseEntity.notFound().build();
    }

    @PreAuthorize("hasAuthority('settings_manage_company')")
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
            existing.setTeamLead(updatedClient.getTeamLead());
            existing.setAssignedEmployee(updatedClient.getAssignedEmployee());
            existing.setBillingAddress(updatedClient.getBillingAddress());
            existing.setCity(updatedClient.getCity());
            existing.setCountry(updatedClient.getCountry());

            // Sync changes with the User table
            User associatedUser = existing.getUser();
            if (associatedUser != null) {
                associatedUser.setFullName(existing.getContactPerson() + " (" + existing.getCompanyName() + ")");
                userRepository.save(associatedUser);
            }

            clientRepository.save(existing);
            return ResponseEntity.ok("Client updated successfully.");
        }
        return ResponseEntity.notFound().build();
    }

    // LOCK: Employee Directory requires 'employee_view'
    @PreAuthorize("hasAuthority('employee_view')")
    @GetMapping("/admin/staff")
    public String showStaffDirectory(Model model, @RequestParam(required = false) String keyword) {
        List<User> staffList = userRepository.findAll().stream()
                // FIXED: Show ALL internal staff by excluding only Clients and the Super Admin
                .filter(u -> u.getRole() != null &&
                        !"CLIENT".equalsIgnoreCase(u.getRole().getRoleName()) &&
                        !"SUPER_ADMIN".equalsIgnoreCase(u.getRole().getRoleName()))
                .filter(u -> keyword == null || keyword.isEmpty() || (u.getFullName() != null && u.getFullName().toLowerCase().contains(keyword.toLowerCase())))
                .sorted(Comparator.comparing(User::getUsername, Comparator.nullsLast(String::compareToIgnoreCase)))
                .collect(Collectors.toList());

        model.addAttribute("staffList", staffList);
        model.addAttribute("keyword", keyword);
        return "admin-staff";
    }

    // LOCK: Editing an employee record requires 'employee_edit'
    @PreAuthorize("hasAuthority('employee_edit')")
    @GetMapping("/admin/staff/edit/{id}")
    public String showEditEmployeeForm(@PathVariable("id") Long id, Model model) {
        User employee = userService.findById(id);
        model.addAttribute("employee", employee);
        return "admin/edit-employee";
    }

    @PreAuthorize("hasAuthority('employee_edit')")
    @PostMapping("/admin/staff/edit/{id}")
    public String updateEmployee(@PathVariable("id") Long id, @ModelAttribute("employee") User updatedEmployee, RedirectAttributes redirectAttributes) {
        userService.updateEmployeeProfessionalDetails(id, updatedEmployee);
        redirectAttributes.addFlashAttribute("successMessage", "Employee Details Updated Successfully!");
        return "redirect:/admin/staff";
    }

    // FIXED LOCK: Helpdesk requires Asset or IT Permissions
    @PreAuthorize("hasAnyAuthority('asset_assign', 'asset_view', 'settings_manage_roles')")
    @GetMapping("/admin-helpdesk-requests")
    public String viewAdminHelpdeskPortal(Model model) {
        List<ServiceRequest> allRequests = serviceRequestRepository.findAll();

        model.addAttribute("allRequests", allRequests);

        long softwareCount = allRequests.stream().filter(r -> "SOFTWARE".equals(r.getType())).count();
        long hardwareCount = allRequests.stream().filter(r -> "HARDWARE".equals(r.getType())).count();

        model.addAttribute("softwareCount", softwareCount);
        model.addAttribute("hardwareCount", hardwareCount);

        return "admin-helpdesk-requests";
    }

    // FIXED LOCK: Global search requires basic admin view rights so Finance/Recruiters can use it
    @PreAuthorize("hasAnyAuthority('employee_view', 'admin_dashboard_view')")
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
            if (u.getRole() != null && !"CLIENT".equalsIgnoreCase(u.getRole().getRoleName())) {
                Map<String, Object> map = new HashMap<>();
                EmployeeProfile profile = u.getEmployeeProfile();

                map.put("dbId", u.getId());
                map.put("fullName", u.getFullName());
                map.put("identifier", u.getUsername());
                map.put("designation", (profile != null && profile.getDesignation() != null) ? profile.getDesignation() : "Employee");
                map.put("department", (profile != null && profile.getBusinessUnit() != null) ? profile.getBusinessUnit() : "General");
                map.put("email", u.getEmail() != null ? u.getEmail() : "N/A");
                map.put("phone", (profile != null && profile.getMobileNumber() != null) ? profile.getMobileNumber() : "N/A");
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


    // ROLE MANAGEMENT MODULE (SUPER ADMIN ONLY)
    @PreAuthorize("hasAuthority('settings_manage_roles')")
    @GetMapping("/admin/manage-roles")
    public String showManageRolesPage(Model model, @RequestParam(required = false) String search) {

        // 1. Fetch all roles, but strictly filter out external and legacy roles
        List<Role> allRoles = roleRepository.findAll().stream()
                .filter(role -> !"CLIENT".equalsIgnoreCase(role.getRoleName()) &&
                        !"ADMIN".equalsIgnoreCase(role.getRoleName()))
                .collect(Collectors.toList());

        // 2. Fetch users (with optional search filter)
        List<User> usersList;
        if (search != null && !search.trim().isEmpty()) {
            usersList = userRepository.searchByKeyword(search.trim());
        } else {
            usersList = userRepository.findAll();
        }

        // FIXED: Security Filters
        // 1. Hide ADM001 to prevent the SuperAdmin from locking themselves out.
        // 2. Hide CLIENT accounts so external users cannot be given internal admin roles.
        usersList = usersList.stream()
                .filter(user -> !"ADM001".equalsIgnoreCase(user.getUsername()))
                .filter(user -> user.getRole() == null || !"CLIENT".equalsIgnoreCase(user.getRole().getRoleName()))
                .collect(Collectors.toList());

        // Sort users alphabetically for a cleaner UI
        usersList.sort(Comparator.comparing(User::getUsername, Comparator.nullsLast(String::compareToIgnoreCase)));

        model.addAttribute("usersList", usersList);
        model.addAttribute("allRoles", allRoles);
        model.addAttribute("currentSearch", search);

        return "admin-manage-roles";
    }

    @PostMapping("/admin/update-role")
    public String updateUserRole(@RequestParam("userId") Long userId,
                                 @RequestParam("roleId") Long roleId,
                                 @RequestParam(value = "designation", required = false) String designation,
                                 RedirectAttributes redirectAttributes) {

        User user = userRepository.findById(userId).orElse(null);
        Role newRole = roleRepository.findById(roleId).orElse(null);

        if (user != null && newRole != null) {
            // 1. Update the System Role
            user.setRole(newRole);

            // 2. Update Designation if the admin selected one from the new dropdown
            if (user.getEmployeeProfile() != null && designation != null && !designation.trim().isEmpty()) {
                user.getEmployeeProfile().setDesignation(designation);
            }

            userRepository.save(user);
            redirectAttributes.addFlashAttribute("successMessage", "Security clearance and designation updated for " + user.getFullName());
        } else {
            redirectAttributes.addFlashAttribute("errorMessage", "Error updating clearance. User or Role not found.");
        }

        return "redirect:/admin/manage-roles";
    }


    @GetMapping("/admin/profile")
    public String viewAdminProfile(Principal principal, Model model) {
        String username = principal.getName();
        User currentUser = userService.findByUsername(username);

        model.addAttribute("user", currentUser);
        return "admin-profile";
    }

    @PostMapping("/admin/profile/update")
    public String updateAdminProfile(
            // Contact & Emergency
            @RequestParam("fullName") String fullName,
            @RequestParam("mobileNumber") String mobileNumber,
            @RequestParam(value = "altMobile", required = false) String altMobile,
            @RequestParam(value = "personalEmail", required = false) String personalEmail,
            @RequestParam("permanentAddress") String permanentAddress,
            @RequestParam(value = "workingAddress", required = false) String workingAddress,
            @RequestParam(value = "city", required = false) String city,
            @RequestParam(value = "country", required = false) String country,
            @RequestParam("emergencyContactName") String emergencyContactName,
            @RequestParam(value = "relationWithEmployee", required = false) String relationWithEmployee,
            @RequestParam("emergencyPhone") String emergencyPhone,

            // Newly Editable: Identity & Compliance
            @RequestParam(value = "dob", required = false) @org.springframework.format.annotation.DateTimeFormat(pattern = "yyyy-MM-dd") java.time.LocalDate dob,
            @RequestParam(value = "gender", required = false) String gender,
            @RequestParam(value = "panNo", required = false) String panNo,
            @RequestParam(value = "aadharNo", required = false) String aadharNo,

            // Newly Editable: Education
            @RequestParam(value = "qual1Title", required = false) String qual1Title,
            @RequestParam(value = "qual1Inst", required = false) String qual1Inst,
            @RequestParam(value = "qual1Year", required = false) String qual1Year,
            @RequestParam(value = "qual2Title", required = false) String qual2Title,
            @RequestParam(value = "qual2Inst", required = false) String qual2Inst,
            @RequestParam(value = "qual2Year", required = false) String qual2Year,

            Principal principal,
            RedirectAttributes redirectAttributes) {

        String username = principal.getName();
        User existingUser = userService.findByUsername(username);

        existingUser.setFullName(fullName);

        EmployeeProfile profile = existingUser.getEmployeeProfile();
        if (profile == null) {
            profile = new EmployeeProfile();
            profile.setUser(existingUser);
        }

        // Map Contact Data
        profile.setMobileNumber(mobileNumber);
        profile.setAltMobile(altMobile);
        profile.setPersonalEmail(personalEmail);
        profile.setPermanentAddress(permanentAddress);
        profile.setWorkingAddress(workingAddress);
        profile.setCity(city);
        profile.setCountry(country);

        // Map Emergency Data
        profile.setEmergencyContactName(emergencyContactName);
        profile.setRelationWithEmployee(relationWithEmployee);
        profile.setEmergencyPhone(emergencyPhone);

        // Map Identity Data
        profile.setDob(dob);
        profile.setGender(gender);
        profile.setPanNo(panNo);
        profile.setAadharNo(aadharNo);

        // Map Education Data
        profile.setQual1Title(qual1Title);
        profile.setQual1Inst(qual1Inst);
        profile.setQual1Year(qual1Year);
        profile.setQual2Title(qual2Title);
        profile.setQual2Inst(qual2Inst);
        profile.setQual2Year(qual2Year);

        existingUser.setEmployeeProfile(profile);
        userRepository.save(existingUser);

        redirectAttributes.addFlashAttribute("successMessage", "Profile updated successfully.");
        return "redirect:/admin/profile";
    }

}