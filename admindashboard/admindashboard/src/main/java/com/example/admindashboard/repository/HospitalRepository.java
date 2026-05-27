package com.example.admindashboard.repository;

import com.example.admindashboard.model.Hospital;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface HospitalRepository extends JpaRepository<Hospital, Long> {
    // JpaRepository provides findAll() out of the box, which is all we need here!
}