package com.example.job_tracking.service;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

@Service
public class EmailService {

    private final RestClient restClient;

    @Value("${app.mail.from:test@example.com}")
    private String fromAddress;

    // Render's free tier blocks outbound SMTP ports entirely, so email goes
    // out through Brevo's HTTPS API instead of SMTP — see
    // https://render.com/changelog/free-web-services-will-no-longer-allow-outbound-traffic-to-smtp-ports
    public EmailService(@Value("${app.brevo.api-key:}") String brevoApiKey) {
        this.restClient = RestClient.builder()
                .baseUrl("https://api.brevo.com/v3")
                .defaultHeader("api-key", brevoApiKey)
                .defaultHeader("content-type", "application/json")
                .defaultHeader("accept", "application/json")
                .build();
    }

    public void sendPasswordResetEmail(String to, String resetLink) {
        Map<String, Object> body = Map.of(
                "sender", Map.of("name", "Job Tracker", "email", fromAddress),
                "to", List.of(Map.of("email", to)),
                "subject", "Reset your Job Tracker password",
                "textContent",
                "We received a request to reset your Job Tracker password.\n\n" +
                "Click the link below to choose a new one (valid for 1 hour):\n" +
                resetLink + "\n\n" +
                "If you didn't request this, you can safely ignore this email."
        );

        restClient.post()
                .uri("/smtp/email")
                .body(body)
                .retrieve()
                .toBodilessEntity();
    }
}