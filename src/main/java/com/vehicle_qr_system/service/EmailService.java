package com.vehicle_qr_system.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

@Service
public class EmailService {

    @Value("${BREVO_API_KEY}")
    private String brevoApiKey;

    @Value("${spring.mail.username}")
    private String senderEmail;

    private final HttpClient httpClient =
            HttpClient.newHttpClient();

    // =========================================================
    // EMAIL VERIFICATION
    // =========================================================

    public void sendVerificationCode(
            String email,
            String code) {

        String text =
                "Hello,\n\n" +
                "Your Vehicle QR verification code is:\n\n" +
                code +
                "\n\n" +
                "This code will expire in 10 minutes.\n\n" +
                "If you did not create an account, " +
                "you can safely ignore this email.\n\n" +
                "Vehicle QR System";

        sendEmail(
                email,
                "Vehicle QR - Email Verification Code",
                text
        );
    }

    // =========================================================
    // PASSWORD RESET
    // =========================================================

    public void sendPasswordResetCode(
            String email,
            String code) {

        String text =
                "Hello,\n\n" +
                "Your Vehicle QR password reset code is:\n\n" +
                code +
                "\n\n" +
                "This code will expire in 10 minutes.\n\n" +
                "If you did not request a password reset, " +
                "you can safely ignore this email.\n\n" +
                "Vehicle QR System";

        sendEmail(
                email,
                "Vehicle QR - Password Reset Code",
                text
        );
    }

    // =========================================================
    // SEND EMAIL THROUGH BREVO
    // =========================================================

    private void sendEmail(
            String email,
            String subject,
            String text) {

        if (brevoApiKey == null ||
                brevoApiKey.isBlank()) {

            throw new RuntimeException(
                    "BREVO_API_KEY is missing"
            );
        }

        if (senderEmail == null ||
                senderEmail.isBlank()) {

            throw new RuntimeException(
                    "spring.mail.username is missing"
            );
        }

        String json =
                "{"
                + "\"sender\":{"
                + "\"name\":\"Vehicle QR System\","
                + "\"email\":\"" + escapeJson(senderEmail) + "\""
                + "},"
                + "\"to\":[{"
                + "\"email\":\"" + escapeJson(email) + "\""
                + "}],"
                + "\"subject\":\"" + escapeJson(subject) + "\","
                + "\"textContent\":\"" + escapeJson(text) + "\""
                + "}";

        HttpRequest request =
                HttpRequest.newBuilder()
                        .uri(
                                URI.create(
                                        "https://api.brevo.com/v3/smtp/email"
                                )
                        )
                        .header(
                                "accept",
                                "application/json"
                        )
                        .header(
                                "api-key",
                                brevoApiKey
                        )
                        .header(
                                "content-type",
                                "application/json"
                        )
                        .POST(
                                HttpRequest.BodyPublishers.ofString(
                                        json
                        ))
                        .build();

        try {

            HttpResponse<String> response =
                    httpClient.send(
                            request,
                            HttpResponse.BodyHandlers.ofString()
                    );

            System.out.println(
                    "BREVO STATUS: " +
                    response.statusCode()
            );

            System.out.println(
                    "BREVO RESPONSE: " +
                    response.body()
            );

            if (response.statusCode() < 200 ||
                    response.statusCode() >= 300) {

                throw new RuntimeException(
                        "Brevo rejected email. HTTP " +
                        response.statusCode() +
                        " - " +
                        response.body()
                );
            }

        } catch (IOException e) {

            throw new RuntimeException(
                    "Unable to connect to Brevo: " +
                    e.getMessage(),
                    e
            );

        } catch (InterruptedException e) {

            Thread.currentThread().interrupt();

            throw new RuntimeException(
                    "Brevo request was interrupted",
                    e
            );
        }
    }

    // =========================================================
    // JSON ESCAPING
    // =========================================================

    private String escapeJson(String value) {

        if (value == null) {
            return "";
        }

        return value
                .replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\r", "\\r")
                .replace("\n", "\\n")
                .replace("\t", "\\t")
                .replace("\b", "\\b")
                .replace("\f", "\\f");
    }
}