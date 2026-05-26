package com.example.admindashboard.repository;

import com.example.admindashboard.model.LeaveTypeMaster;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface LeaveTypeMasterRepository extends JpaRepository<LeaveTypeMaster, Long> {

    Optional<LeaveTypeMaster> findByLeaveCode(String leaveCode);

}