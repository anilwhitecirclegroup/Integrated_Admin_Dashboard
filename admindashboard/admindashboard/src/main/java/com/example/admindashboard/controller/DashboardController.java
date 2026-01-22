package com.example.admindashboard.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class DashboardController {

    @GetMapping("/")
    public String showDashboard(Model model) {
        // You can eventually pass dynamic data from your database here.
        // For now, we return the name of the HTML file (without .html extension).
        model.addAttribute("appName", "Integrated Admin Dashboard");
        return "dashboard";
    }
}