package com.example.admindashboard.controller;

import com.example.admindashboard.model.TaskUpdateLog;
import com.example.admindashboard.model.Ticket;
import com.example.admindashboard.model.TimeLog;
import com.example.admindashboard.model.User;
import com.example.admindashboard.repository.TaskUpdateLogRepository;
import com.example.admindashboard.repository.TicketRepository;
import com.example.admindashboard.repository.TimeLogRepository;
import com.example.admindashboard.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.security.Principal;
import java.time.LocalDateTime;
import java.util.List;

@Controller
@RequestMapping("/developer")
public class DeveloperTaskController {

    @Autowired
    private TicketRepository ticketRepository;

    @Autowired
    private UserRepository userRepository;

    // NEW: Inject the log repository
    @Autowired
    private TaskUpdateLogRepository taskUpdateLogRepository;

    @Autowired
    private TimeLogRepository timeLogRepository;

    @GetMapping("/board")
    public String showDeveloperBoard(Model model, Principal principal) {
        User developer = userRepository.findByUsername(principal.getName()).orElseThrow();
        List<Ticket> myTasks = ticketRepository.findByAssignedTo_Id(developer.getId());

        model.addAttribute("developerName", developer.getFullName());
        model.addAttribute("myTasks", myTasks);

        return "developer-task-board";
    }

    @PostMapping("/task/update")
    public String updateTaskProgress(@RequestParam Long taskId,
                                     @RequestParam Integer progressPercentage,
                                     @RequestParam(required = false) String updateNote) {

        Ticket task = ticketRepository.findById(taskId).orElse(null);

        if (task != null) {
            task.setProgressPercentage(progressPercentage);

            if (progressPercentage >= 100) {
                task.setStatus("Completed");
                task.setProgressPercentage(100);
            } else if (progressPercentage > 0) {
                task.setStatus("Development");
            }

            ticketRepository.save(task);

            // NEW: Save the Audit Trail Log
            if (updateNote != null && !updateNote.trim().isEmpty()) {
                TaskUpdateLog log = new TaskUpdateLog();
                log.setTicket(task);
                log.setUpdateNote(updateNote.trim());
                log.setRecordedPercentage(progressPercentage);
                taskUpdateLogRepository.save(log);
            }
        }

        return "redirect:/developer/board";
    }


    @PostMapping("/task/log-time")
    public String logTaskTime(@RequestParam Long taskId,
                              @RequestParam Integer hours,
                              @RequestParam Integer minutes,
                              Principal principal) { // Added Principal to get the User

        Ticket task = ticketRepository.findById(taskId).orElse(null);
        User developer = userRepository.findByUsername(principal.getName()).orElseThrow();

        if (task != null) {
            // Convert to seconds for the Ticket's running total
            long newSecondsToLog = (hours * 3600L) + (minutes * 60L);
            // Convert to minutes for the TimeLog ledger
            long durationInMinutes = (hours * 60L) + minutes;

            if (newSecondsToLog > 0) {
                // 1. Update the overall accumulated time on the Ticket
                task.setTotalTimeTracked(task.getTotalTimeTracked() + newSecondsToLog);
                ticketRepository.save(task);

                // 2. Create the Ledger entry using your EXISTING TimeLog model
                TimeLog timeLog = new TimeLog();
                timeLog.setTicket(task);
                timeLog.setUser(developer);
                timeLog.setDurationMinutes(durationInMinutes);
                timeLog.setIsManualEntry(true);

                // 3. Since the model requires a non-null startTime, we calculate a virtual one
                LocalDateTime now = LocalDateTime.now();
                timeLog.setEndTime(now);
                timeLog.setStartTime(now.minusMinutes(durationInMinutes));

                // 4. Save to the database
                timeLogRepository.save(timeLog);
            }
        }

        return "redirect:/developer/board";
    }
}