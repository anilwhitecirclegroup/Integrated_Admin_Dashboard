package com.example.admindashboard.repository;

import com.example.admindashboard.model.EmployeeProfile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface EmployeeProfileRepository extends JpaRepository<EmployeeProfile, Long> {

    // NEW: The underscore tells Spring to look inside the 'user' object for the 'username'
    Optional<EmployeeProfile> findByUser_Username(String username);

}