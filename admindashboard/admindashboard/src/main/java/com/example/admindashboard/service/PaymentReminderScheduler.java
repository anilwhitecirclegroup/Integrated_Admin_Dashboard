package com.example.admindashboard.service;

import com.example.admindashboard.model.Payment;
import com.example.admindashboard.repository.PaymentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class PaymentReminderScheduler {

    @Autowired
    private PaymentRepository paymentRepository;

    @Autowired
    private EmailService emailService;

    // Runs automatically every day at 9:00 AM server time
    // Cron format: Seconds Minutes Hours DayOfMonth Month DayOfWeek
    @Scheduled(cron = "0 0 9 * * ?")
    public void checkAndSendPaymentReminders() {
        System.out.println("🕰️ [System] Running daily payment reminder check...");

        List<Payment> allPayments = paymentRepository.findAll();
        LocalDate today = LocalDate.now();

        for (Payment payment : allPayments) {
            // Only process payments that are NOT fully paid and have a billing date set
            if (!"Fully Paid".equalsIgnoreCase(payment.getPaymentStatus()) && payment.getNextBillingDate() != null) {

                long daysUntilDue = ChronoUnit.DAYS.between(today, payment.getNextBillingDate());

                // Trigger: Last 3 days of the billing cycle (as per requirement doc)
                if (daysUntilDue >= 0 && daysUntilDue <= 3) {

                    String clientEmail = payment.getProject().getClient().getEmail();
                    String clientName = payment.getProject().getClient().getFullName();

                    Map<String, Object> emailData = new HashMap<>();
                    emailData.put("projectName", payment.getProject().getProjectName());
                    emailData.put("dueDate", payment.getNextBillingDate().toString());
                    emailData.put("pendingAmount", "$" + payment.getPendingAmount().toString());
                    emailData.put("daysLeft", daysUntilDue);

                    // Re-using your existing EmailService
                    try {
                        emailService.sendRequestStatusUpdateToEmployee(
                                clientEmail,
                                clientName,
                                "Invoice Reminder: " + payment.getProject().getProjectName(),
                                "Pending Payment",
                                emailData
                        );
                        System.out.println("✅ Sent billing reminder to " + clientEmail + " for project " + payment.getProject().getProjectName());
                    } catch (Exception e) {
                        System.err.println("⚠️ Failed to send billing email to " + clientEmail + ": " + e.getMessage());
                    }
                }
            }
        }
    }
}