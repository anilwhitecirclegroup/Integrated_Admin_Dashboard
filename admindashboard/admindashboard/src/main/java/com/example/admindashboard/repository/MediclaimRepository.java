package com.example.admindashboard.repository;

import com.example.admindashboard.model.Mediclaim;
import com.example.admindashboard.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface MediclaimRepository extends JpaRepository<Mediclaim, Long> {
    // This will let us easily fetch all claims for the logged-in user later!
    List<Mediclaim> findByUserOrderBySubmissionDateDesc(User user);
}