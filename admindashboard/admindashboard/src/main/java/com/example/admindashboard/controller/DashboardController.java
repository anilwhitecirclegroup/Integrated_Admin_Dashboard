package com.example.admindashboard.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class DashboardController {

    // 1. The Entry Point (Login Page)
    @GetMapping("/")
    public String loginPage() {
        return "index";
    }

    // 2. The Logic to Check Password (THE NEW FIX)
    @PostMapping("/login")
    public String verifyLogin(@RequestParam String username,
                              @RequestParam String password,
                              Model model) {

        // Simple Hardcoded Check (We will connect DB later)
        if ("admin".equals(username) && "admin123".equals(password)) {
            // Success: Go to Dashboard
            return "redirect:/dashboard";
        } else {
            // Failure: Stay on Login Page & Show Error
            model.addAttribute("error", "Invalid Username or Password");
            return "index";
        }
    }

    // 3. The Admin Dashboard
    @GetMapping("/dashboard")
    public String dashboard() {
        return "dashboard";
    }

    // 4. The Client Dashboard
    @GetMapping("/client")
    public String clientDashboard() {
        return "client-dashboard";
    }
}


