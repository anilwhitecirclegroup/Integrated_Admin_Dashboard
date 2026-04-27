package com.example.admindashboard.repository;

import com.example.admindashboard.model.ServiceRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface ServiceRequestRepository extends JpaRepository<ServiceRequest, Long> {

    // Custom query method used by your controller
    List<ServiceRequest> findByEmployeeIdOrderBySubmissionDateDesc(String employeeId);

    // NEW: Fetches only the 3 most recent tickets for the dashboard widget
    List<ServiceRequest> findTop3ByEmployeeIdOrderBySubmissionDateDesc(String employeeId);

    // UPDATED: Sorts by ID Descending to guarantee the absolute newest tickets are fetched first
    List<ServiceRequest> findTop3ByEmployeeIdOrderByIdDesc(String employeeId);

}