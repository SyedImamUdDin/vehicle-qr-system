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

    private final HttpClient httpClient = HttpClient.newHttpClient();

    public void sendVerificationCode(String email, String code) {

        String json = """
                {
                  "sender": {
                    "name": "Vehicle QR System",
                    "email": "%s"
                  },
                  "to": [
                    {
                      "email": "%s"
                    }
                  ],
                  "subject": "Vehicle QR - Email Verification Code",
                  "textContent": "Hello,\\n\\nYour Vehicle QR verification code is:\\n\\n%s\\n\\nThis code will expire in 10 minutes.\\n\\nIf you did not create an account, you can safely ignore this email.\\n\\nVehicle QR System"
                }
                """.formatted(
                escapeJson(senderEmail),
                escapeJson(email),
                escapeJson(code)
        );

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("https://api.brevo.com/v3/smtp/email"))
                .header("accept", "application/json")
                .header("api-key", brevoApiKey)
                .header("content-type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(json))
                .build();

        try {
            HttpResponse<String> response =
                    httpClient.send(
                            request,
                            HttpResponse.BodyHandlers.ofString()
                    );

            if (response.statusCode() < 200 ||
                    response.statusCode() >= 300) {

                throw new RuntimeException(
                        "Brevo email failed. HTTP " +
                        response.statusCode() +
                        ": " +
                        response.body()
                );
            }

        } catch (IOException e) {
            throw new RuntimeException(
                    "Unable to connect to Brevo email service",
                    e
            );

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();

            throw new RuntimeException(
                    "Email sending was interrupted",
                    e
            );
        }
    }

    private String escapeJson(String value) {
        return value
                .replace("\\", "\\\\")
                .replace("\"", "\\\"");
    }
}