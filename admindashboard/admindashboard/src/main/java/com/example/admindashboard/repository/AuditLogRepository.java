package com.example.admindashboard.repository;

import com.example.admindashboard.model.AuditLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AuditLogRepository extends JpaRepository<AuditLog, Long> {

    // Allows us to easily search history for a specific user
    List<AuditLog> findByUsernameOrderByTimestampDesc(String username);

    // Allows us to filter history by module (e.g., show all Payroll changes)
    List<AuditLog> findByModuleOrderByTimestampDesc(String module);
}