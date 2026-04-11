package com.example.admindashboard.controller;

import com.example.admindashboard.model.Client;
import com.example.admindashboard.model.Project;
import com.example.admindashboard.model.Ticket;
import com.example.admindashboard.model.User;
import com.example.admindashboard.repository.ClientRepository;
import com.example.admindashboard.repository.ProjectRepository;
import com.example.admindashboard.repository.TicketRepository;
import com.example.admindashboard.repository.UserRepository;
import com.example.admindashboard.service.FeedbackService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.security.Principal;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/client")
public class ClientViewController {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ProjectRepository projectRepository;

    @Autowired
    private TicketRepository ticketRepository;

    @Autowired
    private FeedbackService feedbackService;

    @Autowired
    private ClientRepository clientRepository;

    @GetMapping("/submit-task")
    public String showSubmitTaskPage(Model model, Principal principal) {

        // 1. Get Logged In User
        User loggedInUser = userRepository.findByUsername(principal.getName()).orElseThrow();

        // 2. Fetch the actual Client entity linked to this user
        Client client = clientRepository.findByUser_Username(loggedInUser.getUsername()).orElse(null);

        String clientDisplayName = loggedInUser.getFullName(); // Fallback
        String managerName = "Pending Assignment";

        // 3. Format the data perfectly using your Client entity
        if (client != null) {
            // Set "Company Name (Domain)"
            if (client.getCompanyName() != null) {
                clientDisplayName = client.getCompanyName();
                if (client.getDomain() != null && !client.getDomain().trim().isEmpty()) {
                    clientDisplayName += " (" + client.getDomain() + ")";
                }
            }

            // Set the Assigned Team Lead
            if (client.getTeamLead() != null && !client.getTeamLead().trim().isEmpty()) {
                managerName = client.getTeamLead();
            }
        }

        // 4. Fetch Client Projects for the Dropdown
        List<Project> clientProjects = projectRepository.findByClientId(loggedInUser.getId());

        // 5. Add everything to the Model
        model.addAttribute("clientName", clientDisplayName);
        model.addAttribute("managerName", managerName);
        model.addAttribute("clientProjects", clientProjects);

        return "client-submit-task";
    }

    @PreAuthorize("hasAuthority('ROLE_CLIENT')")
    @GetMapping("/profile")
    public String showClientProfile(Model model, Principal principal) {
        User loggedInUser = userRepository.findByUsername(principal.getName()).orElseThrow();

        // FIX: Changed data type from ClientProfile to Client
        Client clientData = loggedInUser.getClientProfile();

        model.addAttribute("client", clientData);

        return "client-profile";
    }

    @PreAuthorize("hasAuthority('ROLE_CLIENT')")
    @GetMapping("/tickets")
    public String showTicketsPage() {
        // Returns the real HTML page instead of the WIP placeholder
        return "client-tickets";
    }

    @PreAuthorize("hasAuthority('ROLE_CLIENT')")
    @GetMapping("/ongoing-tasks")
    public String showOngoingTasksPage(Model model, Principal principal) {
        User loggedInUser = userRepository.findByUsername(principal.getName()).orElseThrow();

        // We still need this to display the company name on the page
        Client clientData = loggedInUser.getClientProfile();

        // BUG FIX: Use the 'User' ID to fetch projects, not the 'Client Profile' ID
        List<Project> myProjects = projectRepository.findByClientId(loggedInUser.getId());

        // 2. Fetch all tickets associated with these projects
        List<Ticket> myTickets = myProjects.stream()
                .flatMap(project -> ticketRepository.findByProjectId(project.getId()).stream())
                .collect(Collectors.toList());

        model.addAttribute("client", clientData);
        model.addAttribute("projects", myProjects);
        model.addAttribute("tickets", myTickets);

        return "client-ongoing-tasks";
    }

    // REMOVED the extra "/client" prefix!
    @PostMapping("/submit-feedback")
    public String handleFeedbackSubmission(
            @RequestParam Long projectId,
            @RequestParam int overallRating,
            @RequestParam int communicationRating,
            @RequestParam int qualityRating,
            @RequestParam(required = false) String comments) {

        feedbackService.submitFeedback(projectId, overallRating, communicationRating, qualityRating, comments);

        return "redirect:/client/dashboard?feedbackSuccess=true";
    }

    @GetMapping("/payments")
    public String showPaymentDashboard(Model model, Principal principal) {
        // 1. Get Logged In User
        User loggedInUser = userRepository.findByUsername(principal.getName()).orElseThrow();

        // 2. Format Client Name
        String clientDisplayName = loggedInUser.getFullName();
        Client client = clientRepository.findByUser_Username(loggedInUser.getUsername()).orElse(null);
        if (client != null && client.getCompanyName() != null) {
            clientDisplayName = client.getCompanyName();
        }

        // 3. Inject Financial Data based on BRD Requirements
        // (In the future, this will be fetched from a PaymentRepository)
        model.addAttribute("clientName", clientDisplayName);
        model.addAttribute("totalCost", "$12,500.00");
        model.addAttribute("paidAmount", "$8,000.00");
        model.addAttribute("pendingAmount", "$4,500.00");
        model.addAttribute("progressPercentage", 64); // ($8000 / $12500) * 100

        // Simulate an upcoming billing cycle reminder
        model.addAttribute("showReminder", true);
        model.addAttribute("nextDueDate", LocalDate.now().plusDays(3));

        return "client-payments";
    }

    @GetMapping("/team")
    public String showProjectTeamPage(Model model, Principal principal) {
        // 1. Get Logged In User
        User loggedInUser = userRepository.findByUsername(principal.getName()).orElseThrow();

        // 2. Add basic client info for the header
        model.addAttribute("clientName", loggedInUser.getFullName());

        // 3. Hardcoded project name for now (Will be dynamic later based on selected project)
        model.addAttribute("activeProject", "E-Commerce Platform Development");

        return "client-team";
    }

    @GetMapping("/file-vault")
    public String showFileVaultPage(Model model, Principal principal) {
        // 1. Get Logged In User
        User loggedInUser = userRepository.findByUsername(principal.getName()).orElseThrow();

        // 2. Add client info and active project context
        model.addAttribute("clientName", loggedInUser.getFullName());
        model.addAttribute("activeProject", "E-Commerce Platform Development");

        return "client-file-vault";
    }


    // ---------------------------------------------------------
    // PLACEHOLDER ROUTES FOR PHASE 4 & 5 DEVELOPMENT
    // ---------------------------------------------------------

    @PreAuthorize("hasAuthority('ROLE_CLIENT')")
    @GetMapping("/projects")
    public String showProjectsWIP(Model model) {
        model.addAttribute("pageTitle", "Projects Overview");
        model.addAttribute("pageIcon", "fa-folder-open");
        return "client-wip";
    }

    @PreAuthorize("hasAuthority('ROLE_CLIENT')")
    @GetMapping("/support")
    public String showSupportWIP(Model model) {
        model.addAttribute("pageTitle", "Support Helpdesk");
        model.addAttribute("pageIcon", "fa-headset");
        return "client-wip";
    }


}