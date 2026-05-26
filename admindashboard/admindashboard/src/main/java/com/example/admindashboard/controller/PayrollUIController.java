package com.example.admindashboard.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class PayrollUIController {

    @GetMapping("/payroll-dashboard")
    public String payrollDashboard() {
        return "payroll/payroll-dashboard";
    }

    @GetMapping("/tax-calculator")
    public String taxCalculator() {
        return "payroll/tax-calculator";
    }

}