package com.example.job_tracking.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class EmailService {

    private final JavaMailSender mailSender;

    @Value("${app.mail.from}")
    private String fromAddress;

    public EmailService(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    public void sendPasswordResetEmail(String to, String resetLink) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(fromAddress);
        message.setTo(to);
        message.setSubject("Reset your Job Tracker password");
        message.setText(
                "We received a request to reset your Job Tracker password.\n\n" +
                "Click the link below to choose a new one (valid for 1 hour):\n" +
                resetLink + "\n\n" +
                "If you didn't request this, you can safely ignore this email."
        );
        mailSender.send(message);
    }
}