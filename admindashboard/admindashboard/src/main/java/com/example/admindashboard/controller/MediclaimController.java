package com.example.admindashboard.controller;

import com.example.admindashboard.model.User;
import com.example.admindashboard.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.Map;
import java.util.Optional;

@Controller
@RequestMapping("/employee/mediclaim")
public class MediclaimController {

    @Autowired
    private UserRepository userRepository;

    @GetMapping("/auth")
    public String mediclaimAuth() { return "mediclaim-login"; }

    @GetMapping("/portal")
    public String mediclaimPortal() { return "mediclaim-dashboard"; }

    @GetMapping("/policy")
    public String mediclaimPolicy() { return "mediclaim-policy"; }

    @GetMapping("/dependents")
    public String mediclaimDependents() { return "mediclaim-dependents"; }

    @GetMapping("/claim")
    public String mediclaimClaim() { return "mediclaim-claim"; }

    @GetMapping("/track/{claimId}")
    public String mediclaimTrack(@PathVariable String claimId) { return "mediclaim-track"; }

    @GetMapping("/notifications")
    public String mediclaimNotifications() { return "mediclaim-notifications"; }

    @GetMapping("/profile")
    public String mediclaimProfile() { return "mediclaim-profile"; }

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
            // If the system uses another encoding, you can add that specific logic here
            // without affecting the global security configuration.
        }

        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid password.");
    }
}