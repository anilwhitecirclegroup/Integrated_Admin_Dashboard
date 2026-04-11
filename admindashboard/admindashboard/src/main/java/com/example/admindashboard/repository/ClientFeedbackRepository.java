package com.example.admindashboard.repository;

import com.example.admindashboard.model.ClientFeedback;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ClientFeedbackRepository extends JpaRepository<ClientFeedback, Long> {
    Optional<ClientFeedback> findByProjectId(Long projectId);
}