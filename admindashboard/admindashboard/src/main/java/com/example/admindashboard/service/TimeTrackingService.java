package com.example.admindashboard.service;

import com.example.admindashboard.model.TimeLog;
import com.example.admindashboard.model.Ticket;
import com.example.admindashboard.model.User;
import com.example.admindashboard.repository.TimeLogRepository;
import com.example.admindashboard.repository.TicketRepository;
import com.example.admindashboard.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDateTime;

@Service
public class TimeTrackingService {

    @Autowired
    private TimeLogRepository timeLogRepository;
    @Autowired
    private TicketRepository ticketRepository;
    @Autowired
    private UserRepository userRepository;

    // 1. Live Timer: Start
    @Transactional
    public TimeLog startTimer(Long userId, Long ticketId) {
        User user = userRepository.findById(userId).orElseThrow();
        Ticket ticket = ticketRepository.findById(ticketId).orElseThrow();

        TimeLog newLog = new TimeLog();
        newLog.setUser(user);
        newLog.setTicket(ticket);
        newLog.setStartTime(LocalDateTime.now());
        newLog.setIsManualEntry(false); // Flagging as a live tracked session

        return timeLogRepository.save(newLog);
    }

    // 2. Live Timer: Stop & Calculate
    @Transactional
    public TimeLog stopTimer(Long timeLogId, String notes) {
        TimeLog log = timeLogRepository.findById(timeLogId)
                .orElseThrow(() -> new IllegalArgumentException("Active time log not found"));

        if (log.getEndTime() != null) {
            throw new IllegalStateException("Timer was already stopped for this log.");
        }

        log.setEndTime(LocalDateTime.now());
        log.setNotes(notes);

        // Calculate total minutes spent automatically
        Duration duration = Duration.between(log.getStartTime(), log.getEndTime());
        log.setDurationMinutes(duration.toMinutes());

        return timeLogRepository.save(log);
    }

    // 3. Manual Time Entry (For billing retroactively)
    @Transactional
    public TimeLog logManualTime(Long userId, Long ticketId, LocalDateTime start, LocalDateTime end, String notes) {
        User user = userRepository.findById(userId).orElseThrow();
        Ticket ticket = ticketRepository.findById(ticketId).orElseThrow();

        TimeLog manualLog = new TimeLog();
        manualLog.setUser(user);
        manualLog.setTicket(ticket);
        manualLog.setStartTime(start);
        manualLog.setEndTime(end);
        manualLog.setNotes(notes);
        manualLog.setIsManualEntry(true); // Flagging as manual for auditing

        Duration duration = Duration.between(start, end);
        manualLog.setDurationMinutes(duration.toMinutes());

        return timeLogRepository.save(manualLog);
    }
}