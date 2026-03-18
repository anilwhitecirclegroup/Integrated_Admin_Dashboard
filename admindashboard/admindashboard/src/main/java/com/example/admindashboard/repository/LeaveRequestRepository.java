package com.example.admindashboard.repository;

import com.example.admindashboard.model.LeaveRequest;
import com.example.admindashboard.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface LeaveRequestRepository extends JpaRepository<LeaveRequest, Long> {
    List<LeaveRequest> findByUserOrderByFromDateDesc(User user);

    List<LeaveRequest> findByStatus(String status);

    List<LeaveRequest> findByUserOrderByIdDesc(User user);

    // Finds requests by the User object AND the status (ignoring case), newest first!
    List<LeaveRequest> findByUserAndStatusIgnoreCaseOrderByIdDesc(User user, String status);

    // --- NEW: FOR MONTHLY LEAVE REPORTS ---
    // Finds all leaves where the start date falls within the selected month
    List<LeaveRequest> findByFromDateBetweenOrderByFromDateDesc(LocalDate startDate, LocalDate endDate);
}