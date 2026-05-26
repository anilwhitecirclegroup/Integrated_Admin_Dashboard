package com.example.admindashboard.controller;

import com.example.admindashboard.model.MediclaimDependent;
import com.example.admindashboard.model.User;
import com.example.admindashboard.repository.EmployeeProfileRepository;
import com.example.admindashboard.repository.InsurancePolicyRepository;
import com.example.admindashboard.repository.MediclaimDependentRepository;
import com.example.admindashboard.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Controller
@RequestMapping("/employee/mediclaim")
public class MediclaimController {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private EmployeeProfileRepository profileRepository;

    @Autowired
    private InsurancePolicyRepository policyRepository;

    @Autowired
    private MediclaimDependentRepository dependentRepository;

    @GetMapping("/auth")
    public String mediclaimAuth() { return "mediclaim-login"; }

    @GetMapping("/portal")
    public String mediclaimPortal() { return "mediclaim-dashboard"; }

    @GetMapping("/policy")
    public String mediclaimPolicy() { return "mediclaim-policy"; }

    @GetMapping("/claim")
    public String mediclaimClaim() { return "mediclaim-claim"; }

    @GetMapping("/track/{claimId}")
    public String mediclaimTrack(@PathVariable String claimId) { return "mediclaim-track"; }

    @GetMapping("/notifications")
    public String mediclaimNotifications() { return "mediclaim-notifications"; }

    // UPDATED: Profile mapping to fetch dynamic data
    @GetMapping("/profile")
    public String mediclaimProfile(Principal principal, Model model) {
        if (principal == null) {
            return "redirect:/employee/mediclaim/auth";
        }

        String username = principal.getName();
        Optional<User> userOpt = userRepository.findByUsername(username);

        if (userOpt.isPresent()) {
            User user = userOpt.get();
            model.addAttribute("user", user);

            // Fetch Employee Profile using the smart repository method
            profileRepository.findByUser_Username(username).ifPresent(profile -> {
                model.addAttribute("profile", profile);
            });

            // Fetch Insurance Policy
            policyRepository.findByUser(user).ifPresent(policy -> {
                model.addAttribute("policy", policy);
            });

            // Fetch Dependents
            List<MediclaimDependent> dependents = dependentRepository.findByUser(user);
            model.addAttribute("dependents", dependents);
        }

        return "mediclaim-profile";
    }

    @GetMapping("/hospitals")
    public String mediclaimHospitals() { return "mediclaim-hospitals"; }

    @PostMapping("/verify-login")
    @ResponseBody
    public ResponseEntity<String> verifyMediclaimLogin(@RequestBody Map<String, String> payload, Principal principal) {
        String empId = payload.get("empId");
        String password = payload.get("password");

        if (principal == null || !principal.getName().equals(empId)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid Employee ID.");
        }

        Optional<User> userOpt = userRepository.findByUsername(empId);

        if (userOpt.isPresent()) {
            String dbPassword = userOpt.get().getPassword();

            // Logic: Compare {noop} password manually
            if (dbPassword != null && dbPassword.startsWith("{noop}")) {
                String rawDbPassword = dbPassword.replace("{noop}", "");
                if (password.equals(rawDbPassword)) {
                    return ResponseEntity.ok("Success");
                }
            }
        }

        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid password.");
    }

    // 1. UPDATE THIS GET MAPPING
    @GetMapping("/dependents")
    public String mediclaimDependents(Principal principal, Model model) {
        if (principal == null) {
            return "redirect:/employee/mediclaim/auth";
        }

        String username = principal.getName();
        Optional<User> userOpt = userRepository.findByUsername(username);

        if (userOpt.isPresent()) {
            // Fetch real dependents from the database and send to the UI
            List<MediclaimDependent> dependents = dependentRepository.findByUser(userOpt.get());
            model.addAttribute("dependents", dependents);
        }

        return "mediclaim-dependents";
    }

    // 2. ADD THIS NEW POST MAPPING
    @PostMapping("/dependents/add")
    public String addDependent(@RequestParam("fullName") String fullName,
                               @RequestParam("relationship") String relationship,
                               @RequestParam("dob") String dob,
                               @RequestParam(value = "isCovered", required = false) String isCovered,
                               Principal principal) {

        if (principal == null) return "redirect:/employee/mediclaim/auth";

        Optional<User> userOpt = userRepository.findByUsername(principal.getName());

        if (userOpt.isPresent()) {
            MediclaimDependent dependent = new MediclaimDependent();
            dependent.setUser(userOpt.get());
            dependent.setFullName(fullName);
            dependent.setRelationship(relationship);
            dependent.setDob(java.time.LocalDate.parse(dob)); // Parses HTML5 yyyy-mm-dd format

            // If the checkbox is checked, it sends a value ("on"). If unchecked, it sends null.
            dependent.setCovered(isCovered != null);

            dependentRepository.save(dependent);
        }

        // Refresh the page to show the newly added dependent
        return "redirect:/employee/mediclaim/dependents";
    }

}