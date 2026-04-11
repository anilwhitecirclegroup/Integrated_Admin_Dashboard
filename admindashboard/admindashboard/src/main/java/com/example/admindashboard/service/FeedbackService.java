package com.example.admindashboard.service;

import com.example.admindashboard.model.ClientFeedback;
import com.example.admindashboard.model.Project;
import com.example.admindashboard.repository.ClientFeedbackRepository;
import com.example.admindashboard.repository.ProjectRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class FeedbackService {

    @Autowired private ClientFeedbackRepository feedbackRepository;
    @Autowired private ProjectRepository projectRepository;

    public void submitFeedback(Long projectId, int overallRating, int communicationRating, int qualityRating, String comments) {
        // Basic validation for all three ratings
        if (overallRating < 1 || overallRating > 5 || communicationRating < 1 || communicationRating > 5 || qualityRating < 1 || qualityRating > 5) {
            throw new IllegalArgumentException("All ratings must be between 1 and 5 stars");
        }

        Project project = projectRepository.findById(projectId).orElseThrow();

        ClientFeedback feedback = new ClientFeedback();
        feedback.setProject(project);

        // Use the newly upgraded setter methods
        feedback.setOverallRating(overallRating);
        feedback.setCommunicationRating(communicationRating);
        feedback.setQualityRating(qualityRating);
        feedback.setComments(comments);

        feedbackRepository.save(feedback);
    }

}