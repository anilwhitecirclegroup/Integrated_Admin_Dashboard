package com.example.admindashboard.service;

import com.example.admindashboard.model.EmployeeProfile;
import com.example.admindashboard.model.User;
import com.example.admindashboard.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

    public User findById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Employee not found"));
    }

    public User findByUsername(String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("Employee not found with username: " + username));
    }

    @Transactional
    public void updateEmployeeProfessionalDetails(Long id, User updatedData) {
        User existingUser = findById(id);

        // 1. Fetch or create the profile
        EmployeeProfile existingProfile = existingUser.getEmployeeProfile();
        if (existingProfile == null) {
            existingProfile = new EmployeeProfile();
            existingProfile.setUser(existingUser);
        }

        // 2. Safely extract incoming profile updates
        EmployeeProfile incomingProfile = updatedData.getEmployeeProfile();
        if (incomingProfile != null) {
            existingProfile.setDesignation(incomingProfile.getDesignation());
            existingProfile.setBusinessUnit(incomingProfile.getBusinessUnit());
            existingProfile.setProjectName(incomingProfile.getProjectName());
            existingProfile.setReportingManager(incomingProfile.getReportingManager());
            existingProfile.setProjectCode(incomingProfile.getProjectCode());
            existingProfile.setBuHrContact(incomingProfile.getBuHrContact());
            existingProfile.setTeamGroup(incomingProfile.getTeamGroup());
            existingProfile.setProjectManager(incomingProfile.getProjectManager());
        }

        // 3. Save the linked data
        existingUser.setEmployeeProfile(existingProfile);
        userRepository.save(existingUser);
    }

    @Transactional
    public void updateEmployeePersonalDetails(String username, User updatedData) {
        // Find the logged-in user securely using their username
        User existingUser = findByUsername(username);

        // 1. Fetch or create the profile
        EmployeeProfile existingProfile = existingUser.getEmployeeProfile();
        if (existingProfile == null) {
            existingProfile = new EmployeeProfile();
            existingProfile.setUser(existingUser);
        }

        // 2. ONLY update personal fields allowed by Admin policy
        EmployeeProfile incomingProfile = updatedData.getEmployeeProfile();
        if (incomingProfile != null) {
            existingProfile.setMobileNumber(incomingProfile.getMobileNumber());
            existingProfile.setWorkLocation(incomingProfile.getWorkLocation());
            existingProfile.setCity(incomingProfile.getCity());
        }

        // 3. Save the changes
        existingUser.setEmployeeProfile(existingProfile);
        userRepository.save(existingUser);
    }
}