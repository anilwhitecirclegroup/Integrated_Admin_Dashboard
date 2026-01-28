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

    // 2. The Logic to Check Password (UPDATED: Accepts ANY valid input)
    @PostMapping("/login")
    public String verifyLogin(@RequestParam String username,
                              @RequestParam String password,
                              Model model) {

        // TEMPORARY LOGIC:
        // Instead of checking for "admin", we just check if the fields are NOT empty.
        // Once the Database is added, we will check if "username" exists in the DB.

        if (username != null && !username.trim().isEmpty() &&
                password != null && !password.trim().isEmpty()) {

            // Input is valid (not empty) -> Let them in!
            return "redirect:/dashboard";

        } else {
            // Input is empty -> Block them.
            model.addAttribute("error", "Username and Password cannot be empty");
            return "index";
        }
    }

    // 3. The Employee Portal
    @GetMapping("/employee")
    public String showEmployeePortal() {
        return "employee"; // This looks for employee.html
    }

    // 4. My Profile Section of the Employee portal
    @GetMapping("/my-profile")
    public String showProfilePage() {
        // This returns "my-profile.html" from the templates folder
        return "my-profile";
    }

    // 5.  "Back to Home" button
    @GetMapping("/employee/dashboard")
    public String showEmployeeDashboard() {
        return "employee"; // This loads employee.html
    }

    //6. "Apply for Leave" Page button
    @GetMapping("/apply-leave")
    public String showLeavePage() {
        return "apply-leave";
    }

    // 7. "Conference Room" Page button
    @GetMapping("/conference-room")
    public String showConferencePage() {
        return "conference-room";
    }



    // 6. The Client Dashboard (Updated to accept data)
    @GetMapping("/client")
    public String clientDashboard(@RequestParam String clientName,
                                  @RequestParam String phone,
                                  @RequestParam String pan,
                                  Model model) {

        // Send these values to the HTML page
        model.addAttribute("name", clientName);
        model.addAttribute("phone", "+91 " + phone);
        model.addAttribute("pan", pan);

        return "client-dashboard";
    }

    // 6. The Notification Center Route
    @GetMapping("/notifications")
    public String notificationsPage() {
        return "notifications";
    }
}

