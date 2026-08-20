package com.vehicle_qr_system.service;

import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class EmailService {

    private final JavaMailSender mailSender;

    public EmailService(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }


    public void sendVerificationCode(
            String email,
            String code) {

        SimpleMailMessage message =
                new SimpleMailMessage();

        message.setTo(email);

        message.setSubject(
                "Vehicle QR - Email Verification Code"
        );

        message.setText(
                "Hello,\n\n" +

                "Your Vehicle QR verification code is:\n\n" +

                code +

                "\n\n" +

                "This code will expire in 10 minutes.\n\n" +

                "If you did not create an account, " +
                "you can safely ignore this email.\n\n" +

                "Vehicle QR System"
        );

        mailSender.send(message);
    }
}