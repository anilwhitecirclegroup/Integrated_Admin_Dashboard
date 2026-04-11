package com.example.admindashboard.controller;

import com.example.admindashboard.model.Project;
import com.example.admindashboard.model.Ticket;
import com.example.admindashboard.model.User;
import com.example.admindashboard.repository.ProjectRepository;
import com.example.admindashboard.repository.TicketRepository;
import com.example.admindashboard.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.view.RedirectView;

import java.security.Principal;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/client/tickets")
public class ClientTicketController {

    @Autowired
    private TicketRepository ticketRepository;

    @Autowired
    private ProjectRepository projectRepository;

    @Autowired
    private UserRepository userRepository;

    // TASK 3.1: Automated Ticket Generation API
    @PreAuthorize("hasAuthority('ROLE_CLIENT')")
    @PostMapping("/submit")
    public RedirectView submitNewTask(
            @RequestParam Long projectId,
            @RequestParam String title,
            @RequestParam String description,
            @RequestParam String priority,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate deadline,
            Principal principal) {

        User client = userRepository.findByUsername(principal.getName()).orElseThrow();
        Project project = projectRepository.findById(projectId).orElseThrow();

        // Security check: Ensure the client actually owns this project!
        if (!project.getClient().getId().equals(client.getId())) {
            // Redirects back with an error flag if they try to hack the project ID
            return new RedirectView("/client/submit-task?error=unauthorized");
        }

        // Auto-Generate the Ticket
        Ticket newTicket = new Ticket();
        newTicket.setProject(project);
        newTicket.setTitle(title);
        newTicket.setDescription(description);
        newTicket.setPriority(priority);
        newTicket.setDeadline(deadline);
        newTicket.setStatus("Open"); // Default status upon creation
        newTicket.setProgressPercentage(0);

        // Saves to the database
        ticketRepository.save(newTicket);

        // CRITICAL UPDATE: Redirects the user back to the form with the success banner triggered
        return new RedirectView("/client/submit-task?success=true");
    }

    // TASK 3.3: Client Dashboard Ticket View (Used for loading tables via AJAX/JSON)
    @PreAuthorize("hasAuthority('ROLE_CLIENT')")
    @GetMapping("/my-tickets")
    public ResponseEntity<List<Ticket>> getMyTickets(Principal principal) {
        User client = userRepository.findByUsername(principal.getName()).orElseThrow();

        // Fetch all projects for this client, then get all tickets for those projects
        List<Project> myProjects = projectRepository.findByClientId(client.getId());
        List<Ticket> myTickets = myProjects.stream()
                .flatMap(project -> ticketRepository.findByProjectId(project.getId()).stream())
                .collect(Collectors.toList());

        return ResponseEntity.ok(myTickets);
    }
}