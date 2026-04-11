package com.example.admindashboard.repository;

import com.example.admindashboard.model.Ticket;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TicketRepository extends JpaRepository<Ticket, Long> {
    List<Ticket> findByProjectId(Long projectId);
    List<Ticket> findByStatus(String status);
}