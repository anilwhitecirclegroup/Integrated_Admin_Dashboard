package com.example.admindashboard.controller;

import com.example.admindashboard.model.EmployeeProfile;
import com.example.admindashboard.model.User;
import com.example.admindashboard.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat; // <-- NEW CLEAN IMPORT
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

    // --- 1. THE LIST VIEW (With Search & Sort) ---
    @GetMapping("/admin/manage-employees")
    public String showManageEmployeesPage(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String sort,
            Model model) {

        Stream<User> employeeStream = userRepository.findAll().stream()
                .filter(user -> "EMPLOYEE".equals(user.getRole()));

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

    // --- 2. THE EDIT VIEW (Full Page Form) ---
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

    // --- 3. THE SAVE LOGIC (Handles the Form Submission) ---
    @PostMapping("/admin/update-employee")
    public String updateEmployee(
            @ModelAttribute User userUpdates,
            RedirectAttributes redirectAttributes,
            // THE FIX: Cleanly using the @DateTimeFormat annotation!
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

        // 2. Update Professional Fields
        existingUser.setFullName(userUpdates.getFullName());
        existingUser.setEmail(userUpdates.getEmail());
        existingUser.setDesignation(userUpdates.getDesignation());
        existingUser.setExperience(userUpdates.getExperience());
        existingUser.setJoiningDate(userUpdates.getJoiningDate());
        existingUser.setBusinessUnit(userUpdates.getBusinessUnit());
        existingUser.setAccountName(userUpdates.getAccountName());
        existingUser.setProjectName(userUpdates.getProjectName());
        existingUser.setProjectCode(userUpdates.getProjectCode());
        existingUser.setCustomerName(userUpdates.getCustomerName());
        existingUser.setTeamGroup(userUpdates.getTeamGroup());
        existingUser.setVerticalName(userUpdates.getVerticalName());
        existingUser.setDomainIndustry(userUpdates.getDomainIndustry());
        existingUser.setReportingManager(userUpdates.getReportingManager());
        existingUser.setProjectManager(userUpdates.getProjectManager());
        existingUser.setBuHrContact(userUpdates.getBuHrContact());
        existingUser.setWorkLocation(userUpdates.getWorkLocation());
        existingUser.setCity(userUpdates.getCity());
        existingUser.setCountry(userUpdates.getCountry());
        existingUser.setMobileNumber(userUpdates.getMobileNumber());

        // 3. Update Personal Fields
        EmployeeProfile profile = existingUser.getEmployeeProfile();
        if (profile == null) {
            profile = new EmployeeProfile();
            profile.setUser(existingUser);
        }
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