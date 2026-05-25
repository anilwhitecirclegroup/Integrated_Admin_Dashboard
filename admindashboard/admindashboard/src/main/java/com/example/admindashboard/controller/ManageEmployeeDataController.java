package com.example.admindashboard.controller;

import com.example.admindashboard.model.EmployeeProfile;
import com.example.admindashboard.model.User;
import com.example.admindashboard.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
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

    //  1. THE LIST VIEW (With Search, Sort & Soft Delete Filter)
    @PreAuthorize("hasAuthority('employee_view')")
    @GetMapping("/admin/manage-employees")
    public String showManageEmployeesPage(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String sort,
            Authentication authentication,
            Model model) {

        boolean canDelete = false;
        if (authentication != null) {
            canDelete = authentication.getAuthorities().stream()
                    .anyMatch(a -> a.getAuthority().equals("SUPER_ADMIN") ||
                            a.getAuthority().equals("HR_ADMIN") ||
                            a.getAuthority().equals("ROLE_SUPER_ADMIN") ||
                            a.getAuthority().equals("ROLE_HR_ADMIN"));
        }
        model.addAttribute("canDelete", canDelete);

        // FILTER UPDATE: Exclude Super Admins, Clients, AND "INACTIVE" (Soft Deleted) users
        Stream<User> employeeStream = userRepository.findAll().stream()
                .filter(user -> user.getRole() != null &&
                        !"CLIENT".equalsIgnoreCase(user.getRole().getRoleName()) &&
                        !"SUPER_ADMIN".equalsIgnoreCase(user.getRole().getRoleName()) &&
                        !"INACTIVE".equalsIgnoreCase(user.getStatus())); // <--- HIDES DELETED USERS

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
        User existingUser = userRepository.findById(userUpdates.getId())
                .orElseThrow(() -> new IllegalArgumentException("Invalid user Id:" + userUpdates.getId()));
        String email = userUpdates.getEmail();

        if (email != null &&
            !email.matches("^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$")) {

            redirectAttributes.addFlashAttribute(
                "errorMessage",
                "Invalid email format"
            );

            return "redirect:/admin/manage-employees/edit/" + userUpdates.getId();
        }

        existingUser.setFullName(userUpdates.getFullName());
        existingUser.setEmail(userUpdates.getEmail());

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
        userRepository.save(existingUser);

        redirectAttributes.addFlashAttribute("successMessage", "Employee record for " + existingUser.getFullName() + " (ID: " + existingUser.getUsername() + ") has been updated successfully.");
        return "redirect:/admin/manage-employees";
    }

    // 4. THE DELETE LOGIC (Soft Delete / Archive)
    @PreAuthorize("hasAuthority('SUPER_ADMIN') or hasAuthority('HR_ADMIN') or hasAuthority('ROLE_SUPER_ADMIN') or hasAuthority('ROLE_HR_ADMIN')")
    @PostMapping("/admin/delete-employee/{id}")
    public String deleteEmployee(@PathVariable("id") Long id, RedirectAttributes redirectAttributes) {
        try {
            User userToDelete = userRepository.findById(id).orElse(null);

            if (userToDelete != null) {
                if (userToDelete.getRole() != null &&
                        ("SUPER_ADMIN".equalsIgnoreCase(userToDelete.getRole().getRoleName()) ||
                                "ROLE_SUPER_ADMIN".equalsIgnoreCase(userToDelete.getRole().getRoleName()))) {

                    redirectAttributes.addFlashAttribute("errorMessage", "Action Denied: You cannot delete a Super Admin account.");
                    return "redirect:/admin/manage-employees";
                }

                String deletedName = userToDelete.getFullName();

                // SOFT DELETE: Change status to INACTIVE instead of deleting from DB
                userToDelete.setStatus("INACTIVE");
                userRepository.save(userToDelete);

                redirectAttributes.addFlashAttribute("successMessage", "Employee record for " + deletedName + " has been successfully archived/deactivated.");
            } else {
                redirectAttributes.addFlashAttribute("errorMessage", "Employee not found.");
            }
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "System Error: Unable to archive employee. " + e.getMessage());
        }

        return "redirect:/admin/manage-employees";
    }
}