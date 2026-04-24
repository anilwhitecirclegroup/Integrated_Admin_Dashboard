package com.example.admindashboard.repository;

import com.example.admindashboard.model.ClientFeedback;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ClientFeedbackRepository extends JpaRepository<ClientFeedback, Long> {

    // CHANGED: Returns a List instead of Optional to support multiple feedbacks per project
    List<ClientFeedback> findByProjectId(Long projectId);

    // NEW: Fetches all feedback for the Admin Hub, sorted newest first
    List<ClientFeedback> findAllByOrderBySubmittedAtDesc();
}