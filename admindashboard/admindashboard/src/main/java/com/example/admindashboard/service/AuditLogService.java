package com.example.admindashboard.service;

import com.example.admindashboard.model.AuditLog;
import com.example.admindashboard.repository.AuditLogRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import jakarta.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;

@Service
public class AuditLogService {

    @Autowired
    private AuditLogRepository auditLogRepository;

    @Autowired
    private HttpServletRequest request; // Automatically injects the current HTTP request to grab the IP

    public void logAction(String username, String action, String module, String oldValue, String newValue) {
        AuditLog log = new AuditLog();
        log.setUsername(username);
        log.setAction(action);
        log.setModule(module);
        log.setOldValue(oldValue);
        log.setNewValue(newValue);
        log.setTimestamp(LocalDateTime.now());

        // Safely extract the IP Address (handles local testing and production proxies)
        String ipAddress = request.getHeader("X-Forwarded-For");
        if (ipAddress == null || ipAddress.isEmpty() || "unknown".equalsIgnoreCase(ipAddress)) {
            ipAddress = request.getRemoteAddr();
        }

        // Handle IPv6 localhost format
        if ("0:0:0:0:0:0:0:1".equals(ipAddress)) {
            ipAddress = "127.0.0.1";
        }

        log.setIpAddress(ipAddress);

        auditLogRepository.save(log);
    }
}