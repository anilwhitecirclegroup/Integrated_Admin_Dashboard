package com.example.admindashboard.repository;

import com.example.admindashboard.model.TaskUpdateLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TaskUpdateLogRepository extends JpaRepository<TaskUpdateLog, Long> {
    // Optional: Useful if you ever need to fetch logs independently
    List<TaskUpdateLog> findByTicketIdOrderByUpdatedAtDesc(Long ticketId);
}