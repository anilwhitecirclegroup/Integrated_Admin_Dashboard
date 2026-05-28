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


    @Async
    public void sendRequestStatusUpdateToEmployee(String employeeEmail, String employeeName, String requestType, String status, Map<String, Object> templateModel) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            // 1. Setup Email Routing
            helper.setTo(employeeEmail);
            helper.setFrom(systemEmail, "WhiteCircle HRMS");

            // 2. Dynamic Subject Line (e.g., "Update on your Leave Request: Approved")
            helper.setSubject("Update on your " + requestType + " Request: " + status.toUpperCase());

            // 3. Inject Data into Thymeleaf
            Context thymeleafContext = new Context();
            thymeleafContext.setVariables(templateModel);

            // Pass the dynamic title and status directly to the template context
            thymeleafContext.setVariable("requestType", requestType);
            thymeleafContext.setVariable("status", status);

            // 4. Process the Universal HTML Template
            // We will create this single file next: src/main/resources/templates/emails/employee-status-update.html
            String htmlBody = templateEngine.process("emails/employee-status-update", thymeleafContext);

            // 5. Send!
            helper.setText(htmlBody, true);
            mailSender.send(message);

            System.out.println("✅ Status Update (" + status + ") Email Sent Successfully to Employee: " + employeeEmail);

        } catch (Exception e) {
            System.err.println("❌ Failed to send status update email to employee: " + e.getMessage());
        }
    }


    @Async
    public void sendMeetingInvite(String participantEmail, String participantName, String meetingTitle, Map<String, Object> templateModel) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setTo(participantEmail);
            helper.setFrom(systemEmail, "WhiteCircle HRMS");
            helper.setSubject("Meeting Invite: " + meetingTitle);

            Context thymeleafContext = new Context();
            thymeleafContext.setVariables(templateModel);
            thymeleafContext.setVariable("participantName", participantName);

            String htmlBody = templateEngine.process("emails/meeting-invite", thymeleafContext);

            helper.setText(htmlBody, true);
            mailSender.send(message);

            System.out.println("✅ Meeting Invite Email Sent Successfully to: " + participantEmail);

        } catch (Exception e) {
            System.err.println("❌ Failed to send meeting invite email to " + participantEmail + ": " + e.getMessage());
        }
    }

    // =========================================================================
    // ADDED: DYNAMIC SEAMLESS ROUTING FOR SENDING EMPLOYEE TICKETS
    // =========================================================================
    @Async
    public void sendHelpdeskNotifications(String departmentCode, String employeeName, String employeeEmail, String formSubject, Map<String, Object> templateModel) {
        try {
            String cleanDept = (departmentCode != null) ? departmentCode.trim().toUpperCase() : "IT";

            // 1. Resolve Department Head Email via dynamic environment system variables (Render / System OS)
            String deptHeadEmail = System.getenv("DEPT_HEAD_" + cleanDept);
            if (deptHeadEmail == null || deptHeadEmail.trim().isEmpty()) {
                deptHeadEmail = "support@whitecircle.com"; // Smart fallback
            }

            // 2. Resolve Admin Target utilizing company's active properties registration variable
            String adminEmail = this.systemEmail;

            // 3. Construct HTML Payload using the shared template layout
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom(systemEmail, "WhiteCircle Helpdesk");
            helper.setReplyTo(employeeEmail, employeeName);
            helper.setSubject("New Helpdesk Submission [" + cleanDept + "]: " + formSubject);

            Context thymeleafContext = new Context();
            thymeleafContext.setVariables(templateModel);
            String htmlBody = templateEngine.process("emails/ticket-submission-email", thymeleafContext);
            helper.setText(htmlBody, true);

            // 4. Send to Department Head
            helper.setTo(deptHeadEmail);
            mailSender.send(message);

            // 5. Send to Admin
            helper.setTo(adminEmail);
            mailSender.send(message);

            System.out.println("✅ Helpdesk Notification successfully pushed to Admin & Head of " + cleanDept);

        } catch (Exception e) {
            System.err.println("❌ Helpdesk Notification Delivery Error: " + e.getMessage());
        }
    }
}