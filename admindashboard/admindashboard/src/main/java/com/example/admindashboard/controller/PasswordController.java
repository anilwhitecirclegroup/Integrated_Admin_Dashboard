package com.example.admindashboard.controller;

import com.example.admindashboard.model.User;
import com.example.admindashboard.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.security.Principal;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/password")
public class PasswordController {

    @Autowired
    private UserRepository userRepository;

    @PostMapping("/update")
    public ResponseEntity<?> updatePassword(@RequestBody Map<String, String> payload, Principal principal) {
        // Prevent unauthorized access
        if (principal == null) {
            return ResponseEntity.status(401).body("Not authenticated");
        }

        String currentPassword = payload.get("currentPassword");
        String newPassword = payload.get("newPassword");

        Optional<User> userOpt = userRepository.findByUsername(principal.getName());

        if (userOpt.isPresent()) {
            User user = userOpt.get();
            String dbPassword = user.getPassword();

            // Safely strip the {noop} prefix from the database password to compare it
            String actualDbPassword = dbPassword != null ? dbPassword.replace("{noop}", "") : "";

            if (!actualDbPassword.equals(currentPassword)) {
                return ResponseEntity.badRequest().body("The current password you entered is incorrect.");
            }

            // Save the new password with the {noop} prefix so Spring Security still accepts it!
            user.setPassword("{noop}" + newPassword);
            userRepository.save(user);

            return ResponseEntity.ok("Password updated successfully.");
        }

        return ResponseEntity.badRequest().body("User not found.");
    }
}