package com.example.admindashboard.service;

import com.example.admindashboard.model.Project;
import com.example.admindashboard.model.Subtask;
import com.example.admindashboard.model.Ticket;
import com.example.admindashboard.model.User;
import com.example.admindashboard.repository.ProjectRepository;
import com.example.admindashboard.repository.SubtaskRepository;
import com.example.admindashboard.repository.TicketRepository;
import com.example.admindashboard.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

@Component
@Order(2) // Ensures this runs AFTER your main DatabaseSeeder creates the users
public class ClientDatabaseSeeder implements CommandLineRunner {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ProjectRepository projectRepository;

    @Autowired
    private TicketRepository ticketRepository;

    @Autowired
    private SubtaskRepository subtaskRepository;

    @Override
    public void run(String... args) throws Exception {
        // List of clients we want to inject dummy data for
        List<String> clientsToSeed = Arrays.asList("CLI001", "CLI003");

        for (String clientId : clientsToSeed) {
            seedDataForClient(clientId);
        }
    }

    // Helper method to dynamically generate data per client
    private void seedDataForClient(String targetClientId) {
        Optional<User> clientOpt = userRepository.findByUsername(targetClientId);

        if (clientOpt.isPresent()) {
            User client = clientOpt.get();

            // 1. Grab or Create Projects
            List<Project> existingProjects = projectRepository.findByClientId(client.getId());
            Project project1; Project project2;

            if (existingProjects.isEmpty()) {
                project1 = new Project();
                // Give CLI001 distinct project names so you can tell them apart!
                project1.setProjectName(targetClientId.equals("CLI001") ? "ERP System Integration" : "E-Commerce Platform Development");
                project1.setClient(client);
                project1.setStage("Development");

                project2 = new Project();
                project2.setProjectName(targetClientId.equals("CLI001") ? "Legacy Data Migration" : "Cloud Infrastructure Migration");
                project2.setClient(client);
                project2.setStage("Planning");

                project1 = projectRepository.save(project1);
                project2 = projectRepository.save(project2);
            } else {
                project1 = existingProjects.get(0);
                project2 = existingProjects.size() > 1 ? existingProjects.get(1) : project1;
            }

            // 2. Grab or Create Tickets & Subtasks
            List<Ticket> project1Tickets = ticketRepository.findByProjectId(project1.getId());
            Ticket ticket1;
            Ticket ticket2;

            // Only generate tickets if this project currently has none
            if (project1Tickets.isEmpty()) {
                ticket1 = new Ticket();
                ticket1.setProject(project1);
                ticket1.setTitle(targetClientId.equals("CLI001") ? "Establish SAP Database Connection" : "Integrate Stripe Payment API");
                ticket1.setDescription("Set up the backend webhooks, connectivity, and data mapping.");
                ticket1.setPriority("High");
                ticket1.setStatus("Development");
                ticket1.setProgressPercentage(45);
                ticket1.setDeadline(LocalDate.now().plusDays(10));
                ticket1 = ticketRepository.save(ticket1);

                ticket2 = new Ticket();
                ticket2.setProject(project1);
                ticket2.setTitle(targetClientId.equals("CLI001") ? "Design Employee Portal Dashboard" : "Design User Profile Dashboard");
                ticket2.setDescription("Create the UI/UX mockups and get client sign-off.");
                ticket2.setPriority("Medium");
                ticket2.setStatus("Completed");
                ticket2.setProgressPercentage(100);
                ticket2.setDeadline(LocalDate.now().minusDays(2));
                ticket2 = ticketRepository.save(ticket2);

                // 3. SEED SUBTASKS (Attached directly to the newly created tickets)
                if (ticket1 != null) {
                    createSubtask(ticket1, "API Key & Sandbox Configuration", "Completed");
                    createSubtask(ticket1, "Develop Backend Listener Logic", "In Progress");
                    createSubtask(ticket1, "End-to-End Security Testing", "Pending");
                }

                if (ticket2 != null) {
                    createSubtask(ticket2, "Wireframe Initial Layouts", "Completed");
                    createSubtask(ticket2, "High-Fidelity Figma Mockups", "Completed");
                    createSubtask(ticket2, "Client Approval Sign-off", "Completed");
                }

                System.out.println("✅ SUCCESS: Injected test projects & tickets for Client -> " + targetClientId);
            }
        } else {
            System.out.println("⚠️ Notice: User " + targetClientId + " not found, skipping dummy data injection.");
        }
    }

    // Helper method to keep code clean
    private void createSubtask(Ticket ticket, String title, String status) {
        Subtask subtask = new Subtask();
        subtask.setTicket(ticket);
        subtask.setTitle(title);
        subtask.setStatus(status);
        subtaskRepository.save(subtask);
    }
}