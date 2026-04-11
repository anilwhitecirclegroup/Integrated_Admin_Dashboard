package com.example.admindashboard.repository;

import com.example.admindashboard.model.TimeLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TimeLogRepository extends JpaRepository<TimeLog, Long> {
    List<TimeLog> findByTicketId(Long ticketId);
    List<TimeLog> findByUserId(Long userId);
}