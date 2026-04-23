package com.example.admindashboard.controller;

import com.example.admindashboard.model.Ticket;
import com.example.admindashboard.model.User;
import com.example.admindashboard.repository.TicketRepository;
import com.example.admindashboard.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.security.Principal;
import java.util.List;

@Controller
@RequestMapping("/developer")
public class DeveloperTaskController {

    @Autowired
    private TicketRepository ticketRepository;

    @Autowired
    private UserRepository userRepository;

    @GetMapping("/board")
    public String showDeveloperBoard(Model model, Principal principal) {
        // 1. Identify the logged-in developer
        User developer = userRepository.findByUsername(principal.getName()).orElseThrow();

        // 2. Fetch ONLY the tickets assigned to this specific developer
        List<Ticket> myTasks = ticketRepository.findByAssignedTo_Id(developer.getId());

        // 3. Pass data to the frontend
        model.addAttribute("developerName", developer.getFullName());
        model.addAttribute("myTasks", myTasks);

        return "developer-task-board";
    }
}