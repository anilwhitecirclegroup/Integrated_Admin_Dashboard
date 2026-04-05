package com.example.admindashboard.controller;

import com.example.admindashboard.model.AuditLog;
import com.example.admindashboard.repository.AuditLogRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.Comparator;
import java.util.List;

@Controller
public class AuditLogController {

    @Autowired
    private AuditLogRepository auditLogRepository;

    // LOCK: Only someone with master settings access (Super Admin) can view security logs
    @PreAuthorize("hasAuthority('settings_manage_roles')")
    @GetMapping("/admin/audit-logs")
    public String showAuditLogsPage(Model model) {

        List<AuditLog> logs = auditLogRepository.findAll();

        // Sort logs so the newest actions appear at the top of the table
        logs.sort(Comparator.comparing(AuditLog::getTimestamp).reversed());

        model.addAttribute("auditLogs", logs);
        return "admin-audit-logs";
    }
}