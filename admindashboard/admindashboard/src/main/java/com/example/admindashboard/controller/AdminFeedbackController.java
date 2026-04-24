package com.example.admindashboard.controller;

import com.example.admindashboard.model.ClientFeedback;
import com.example.admindashboard.model.User;
import com.example.admindashboard.repository.ClientFeedbackRepository;
import com.example.admindashboard.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.security.Principal;
import java.util.List;

@Controller
@RequestMapping("/admin")
public class AdminFeedbackController {

    @Autowired
    private ClientFeedbackRepository feedbackRepository;

    @Autowired
    private UserRepository userRepository;

    @GetMapping("/feedback")
    public String showFeedbackHub(Model model, Principal principal) {

        // 1. Fetch all feedback, sorted with the newest reviews at the top
        List<ClientFeedback> allFeedback = feedbackRepository.findAllByOrderBySubmittedAtDesc();

        // 2. Calculate quick metrics for the dashboard cards
        int totalReviews = allFeedback.size();
        double avgRating = 0.0;
        long fiveStarCount = 0;

        if (totalReviews > 0) {
            avgRating = allFeedback.stream()
                    .mapToInt(ClientFeedback::getOverallRating)
                    .average()
                    .orElse(0.0);

            fiveStarCount = allFeedback.stream()
                    .filter(f -> f.getOverallRating() != null && f.getOverallRating() == 5)
                    .count();
        }

        // 3. Add to the view model
        model.addAttribute("feedbacks", allFeedback);
        model.addAttribute("totalReviews", totalReviews);
        // Format average to 1 decimal place (e.g., 4.5)
        model.addAttribute("avgRating", String.format("%.1f", avgRating));
        model.addAttribute("fiveStarCount", fiveStarCount);

        // Fetch Admin's name for the top header
        if (principal != null) {
            User admin = userRepository.findByUsername(principal.getName()).orElse(null);
            if (admin != null) {
                model.addAttribute("managerName", admin.getFullName());
            }
        }

        return "admin-client-feedback";
    }
}