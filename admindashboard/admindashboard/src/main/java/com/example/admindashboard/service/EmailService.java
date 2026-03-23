package com.example.admindashboard.service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.thymeleaf.context.Context;
import org.thymeleaf.spring6.SpringTemplateEngine;
import org.springframework.mail.SimpleMailMessage;

import java.io.UnsupportedEncodingException;
import java.util.Map;

@Service
public class EmailService {

    @Autowired
    private JavaMailSender mailSender;

    @Autowired
    private SpringTemplateEngine templateEngine;

    // Pulls the email address from your application.properties
    @Value("${spring.mail.username}")
    private String systemEmail;

    @Async
    public void sendLeaveRequestToAdmin(String adminEmail, String employeeName, String employeeEmail, Map<String, Object> templateModel) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            // 1. Setup Email Routing & Headers
            helper.setTo(adminEmail);
            helper.setFrom(systemEmail, "WhiteCircle HRMS"); // Looks professional in the inbox
            helper.setReplyTo(employeeEmail, employeeName);  // Clicking 'Reply' goes to the employee
            helper.setSubject("New Leave Request: " + employeeName);

            // 2. Inject Data into Thymeleaf
            Context thymeleafContext = new Context();
            thymeleafContext.setVariables(templateModel);

            // 3. Process the HTML Template
            // It will look for "leave-request-email.html" inside src/main/resources/templates/emails/
            String htmlBody = templateEngine.process("emails/leave-request-email", thymeleafContext);

            // 4. Attach the HTML to the email and Send!
            helper.setText(htmlBody, true); // 'true' means this is HTML, not plain text
            mailSender.send(message);

            System.out.println("✅ Background Email Sent Successfully to: " + adminEmail);

        } catch (MessagingException | UnsupportedEncodingException e) {
            System.err.println("❌ Failed to send email: " + e.getMessage());
        }
    }


    @Async
    public void sendTimesheetSubmissionToAdmin(String adminEmail, String employeeName, String employeeEmail, Map<String, Object> templateModel) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setTo(adminEmail);
            helper.setFrom(systemEmail, "WhiteCircle HRMS");
            helper.setReplyTo(employeeEmail, employeeName);
            helper.setSubject("Timesheet Submitted: " + employeeName);

            Context thymeleafContext = new Context();
            thymeleafContext.setVariables(templateModel);

            // Looks for "timesheet-submission-email.html"
            String htmlBody = templateEngine.process("emails/timesheet-submission-email", thymeleafContext);

            helper.setText(htmlBody, true);
            mailSender.send(message);

            System.out.println("✅ Timesheet Email Sent Successfully to: " + adminEmail);

        } catch (Exception e) {
            System.err.println("❌ Failed to send timesheet email: " + e.getMessage());
        }
    }


    @Async
    public void sendTicketSubmissionToAdmin(String adminEmail, String employeeName, String employeeEmail, String ticketType, Map<String, Object> templateModel) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setTo(adminEmail);
            helper.setFrom(systemEmail, "WhiteCircle IT Helpdesk");
            helper.setReplyTo(employeeEmail, employeeName);
            // Dynamic Subject: "New HARDWARE Ticket: John Doe"
            helper.setSubject("New " + ticketType.toUpperCase() + " Ticket: " + employeeName);

            Context thymeleafContext = new Context();
            thymeleafContext.setVariables(templateModel);

            // Looks for "ticket-submission-email.html"
            String htmlBody = templateEngine.process("emails/ticket-submission-email", thymeleafContext);

            helper.setText(htmlBody, true);
            mailSender.send(message);

            System.out.println("✅ IT Ticket Email Sent Successfully to: " + adminEmail);

        } catch (Exception e) {
            System.err.println("❌ Failed to send IT ticket email: " + e.getMessage());
        }
    }


    @Async
    public void sendAttendanceSubmissionToAdmin(String adminEmail, String employeeName, String employeeEmail, Map<String, Object> templateModel) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setTo(adminEmail);
            helper.setFrom(systemEmail, "WhiteCircle HRMS");
            helper.setReplyTo(employeeEmail, employeeName);
            helper.setSubject("Attendance Regularization: " + employeeName);

            Context thymeleafContext = new Context();
            thymeleafContext.setVariables(templateModel);

            // Looks for "attendance-submission-email.html"
            String htmlBody = templateEngine.process("emails/attendance-submission-email", thymeleafContext);

            helper.setText(htmlBody, true);
            mailSender.send(message);

            System.out.println("✅ Attendance Email Sent Successfully to: " + adminEmail);

        } catch (Exception e) {
            System.err.println("❌ Failed to send attendance email: " + e.getMessage());
        }
    }



    @Async
    public void sendSimpleEmail(String to, String subject, String text) {
        try {
            org.springframework.mail.SimpleMailMessage message = new org.springframework.mail.SimpleMailMessage();
            message.setFrom(systemEmail);
            message.setTo(to);
            message.setSubject(subject);
            message.setText(text);

            mailSender.send(message);
            System.out.println("✅ Simple Text Email Sent Successfully to: " + to);

        } catch (Exception e) {
            System.err.println("❌ Failed to send simple email: " + e.getMessage());
        }
    }
}