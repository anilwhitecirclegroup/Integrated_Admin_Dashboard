package com.example.admindashboard.controller;

import com.example.admindashboard.model.EmployeeProfile;
import com.example.admindashboard.model.User;
import com.example.admindashboard.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Controller
public class ManageEmployeeDataController {

    @Autowired
    private UserRepository userRepository;

    //  1. THE LIST VIEW (With Search & Sort)
    // NEW LOCK: Only users with 'employee_view' can access this page
    @PreAuthorize("hasAuthority('employee_view')")
    @GetMapping("/admin/manage-employees")
    public String showManageEmployeesPage(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String sort,
            Model model) {

        // FIXED: Show ALL internal staff by excluding Clients and the Super Admin
        Stream<User> employeeStream = userRepository.findAll().stream()
                .filter(user -> user.getRole() != null &&
                        !"CLIENT".equalsIgnoreCase(user.getRole().getRoleName()) &&
                        !"SUPER_ADMIN".equalsIgnoreCase(user.getRole().getRoleName()));

        if (search != null && !search.trim().isEmpty()) {
            String lowerSearch = search.toLowerCase();
            employeeStream = employeeStream.filter(user ->
                    (user.getUsername() != null && user.getUsername().toLowerCase().contains(lowerSearch)) ||
                            (user.getFullName() != null && user.getFullName().toLowerCase().contains(lowerSearch))
            );
        }

        if ("name".equalsIgnoreCase(sort)) {
            employeeStream = employeeStream.sorted(Comparator.comparing(User::getFullName, Comparator.nullsLast(String::compareToIgnoreCase)));
        } else {
            employeeStream = employeeStream.sorted(Comparator.comparing(User::getUsername, Comparator.nullsLast(String::compareToIgnoreCase)));
        }

        List<User> employees = employeeStream.collect(Collectors.toList());

        model.addAttribute("employees", employees);
        model.addAttribute("currentSearch", search);
        model.addAttribute("currentSort", sort != null ? sort : "empId");
        model.addAttribute("editMode", false);

        return "admin/manage-employee-data";
    }

    //  2. THE EDIT VIEW (Full Page Form)
    // NEW LOCK: Only users with 'employee_edit' can open the edit form
    @PreAuthorize("hasAuthority('employee_edit')")
    @GetMapping("/admin/manage-employees/edit/{id}")
    public String showEditEmployeePage(@PathVariable Long id, Model model) {

        User employee = userRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Invalid employee Id: " + id));

        if (employee.getEmployeeProfile() == null) {
            employee.setEmployeeProfile(new EmployeeProfile());
            employee.getEmployeeProfile().setUser(employee);
        }

        model.addAttribute("emp", employee);
        model.addAttribute("editMode", true);

        return "admin/manage-employee-data";
    }

    //  3. THE SAVE LOGIC (Handles the Form Submission)
    // NEW LOCK: Only users with 'employee_edit' can submit data to the server
    @PreAuthorize("hasAuthority('employee_edit')")
    @PostMapping("/admin/update-employee")
    public String updateEmployee(
            @ModelAttribute User userUpdates,
            @ModelAttribute EmployeeProfile profileUpdates,
            RedirectAttributes redirectAttributes,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dob,
            @RequestParam(required = false) String gender,
            @RequestParam(required = false) String aadharNo,
            @RequestParam(required = false) String panNo,
            @RequestParam(required = false) String altMobile,
            @RequestParam(required = false) String personalEmail,
            @RequestParam(required = false) String permanentAddress,
            @RequestParam(required = false) String workingAddress,
            @RequestParam(required = false) String emergencyContactName,
            @RequestParam(required = false) String relationWithEmployee,
            @RequestParam(required = false) String emergencyPhone
    ) {
        // 1. Find the existing user
        User existingUser = userRepository.findById(userUpdates.getId())
                .orElseThrow(() -> new IllegalArgumentException("Invalid user Id:" + userUpdates.getId()));

        // 2. Update Core User Fields (Auth & Identity)
        existingUser.setFullName(userUpdates.getFullName());
        existingUser.setEmail(userUpdates.getEmail());

        // 3. Update HR & Professional Fields
        EmployeeProfile profile = existingUser.getEmployeeProfile();
        if (profile == null) {
            profile = new EmployeeProfile();
            profile.setUser(existingUser);
        }

        profile.setDesignation(profileUpdates.getDesignation());
        profile.setExperience(profileUpdates.getExperience());
        profile.setJoiningDate(profileUpdates.getJoiningDate());
        profile.setBusinessUnit(profileUpdates.getBusinessUnit());
        profile.setAccountName(profileUpdates.getAccountName());
        profile.setProjectName(profileUpdates.getProjectName());
        profile.setProjectCode(profileUpdates.getProjectCode());
        profile.setCustomerName(profileUpdates.getCustomerName());
        profile.setTeamGroup(profileUpdates.getTeamGroup());
        profile.setVerticalName(profileUpdates.getVerticalName());
        profile.setDomainIndustry(profileUpdates.getDomainIndustry());
        profile.setReportingManager(profileUpdates.getReportingManager());
        profile.setProjectManager(profileUpdates.getProjectManager());
        profile.setBuHrContact(profileUpdates.getBuHrContact());
        profile.setWorkLocation(profileUpdates.getWorkLocation());
        profile.setCity(profileUpdates.getCity());
        profile.setCountry(profileUpdates.getCountry());
        profile.setMobileNumber(profileUpdates.getMobileNumber());

        profile.setDob(dob);
        profile.setGender(gender);
        profile.setAadharNo(aadharNo);
        profile.setPanNo(panNo);
        profile.setAltMobile(altMobile);
        profile.setPersonalEmail(personalEmail);
        profile.setPermanentAddress(permanentAddress);
        profile.setWorkingAddress(workingAddress);
        profile.setEmergencyContactName(emergencyContactName);
        profile.setRelationWithEmployee(relationWithEmployee);
        profile.setEmergencyPhone(emergencyPhone);

        existingUser.setEmployeeProfile(profile);

        // 4. Save to the database
        userRepository.save(existingUser);

        // 5. Add the Success Message Flash Attribute
        redirectAttributes.addFlashAttribute("successMessage", "Employee record for " + existingUser.getFullName() + " (ID: " + existingUser.getUsername() + ") has been updated successfully.");

        // 6. Redirect back to the main list
        return "redirect:/admin/manage-employees";
    }
}