package com.vehicle_qr_system.controller;

import com.vehicle_qr_system.model.EmailVerification;
import com.vehicle_qr_system.model.User;
import com.vehicle_qr_system.repository.EmailVerificationRepository;
import com.vehicle_qr_system.repository.UserRepository;
import com.vehicle_qr_system.service.EmailService;

import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.Random;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserRepository userRepository;
    private final EmailVerificationRepository verificationRepository;
    private final EmailService emailService;

    private final BCryptPasswordEncoder passwordEncoder =
            new BCryptPasswordEncoder();

    private final Random random = new Random();


    public UserController(
            UserRepository userRepository,
            EmailVerificationRepository verificationRepository,
            EmailService emailService) {

        this.userRepository =
                userRepository;

        this.verificationRepository =
                verificationRepository;

        this.emailService =
                emailService;
    }


    // =========================================================
    // REGISTER
    // =========================================================

    @PostMapping("/register")
    public ResponseEntity<?> registerUser(
            @RequestBody RegisterRequest request) {

        if (request.name() == null ||
                request.name().isBlank()) {

            return ResponseEntity
                    .badRequest()
                    .body("Name is required");
        }


        if (request.email() == null ||
                request.email().isBlank()) {

            return ResponseEntity
                    .badRequest()
                    .body("Email is required");
        }


        if (request.phone() == null ||
                request.phone().isBlank()) {

            return ResponseEntity
                    .badRequest()
                    .body("Phone is required");
        }


        if (request.password() == null ||
                request.password().length() < 6) {

            return ResponseEntity
                    .badRequest()
                    .body("Password must be at least 6 characters");
        }


        if (!request.password()
                .equals(request.confirmPassword())) {

            return ResponseEntity
                    .badRequest()
                    .body("Passwords do not match");
        }


        String email =
                request.email()
                        .trim()
                        .toLowerCase();


        User existingUser =
                userRepository
                        .findByEmail(email)
                        .orElse(null);


        /*
         * If email already exists and is verified,
         * don't allow another account.
         */

        if (existingUser != null &&
                existingUser.isEmailVerified()) {

            return ResponseEntity
                    .badRequest()
                    .body("Email already registered");
        }


        User user;


        /*
         * If an unverified account already exists,
         * update it instead of creating another one.
         */

        if (existingUser != null) {

            user = existingUser;

        } else {

            user = new User();
        }


        user.setName(request.name().trim());

        user.setEmail(email);

        user.setPhone(request.phone().trim());

        user.setCity(
                request.city() == null
                        ? ""
                        : request.city().trim()
        );


        /*
         * Store password securely using BCrypt.
         */

        user.setPassword(
                passwordEncoder.encode(
                        request.password()
                )
        );


        /*
         * New registration requires email verification.
         */

        user.setEmailVerified(false);


        User savedUser =
                userRepository.save(user);


        /*
         * Generate 6-digit verification code.
         */

        String code =
                String.format(
                        "%06d",
                        random.nextInt(1_000_000)
                );


        /*
         * Remove previous OTP.
         */

        verificationRepository
                .deleteByEmail(email);


        /*
         * Create new OTP.
         */

        EmailVerification verification =
                new EmailVerification();

        verification.setEmail(email);

        verification.setCode(code);

        verification.setExpiresAt(
                LocalDateTime.now()
                        .plusMinutes(10)
        );


        verificationRepository.save(
                verification
        );


        /*
         * Send OTP email.
         */

        try {

            emailService.sendVerificationCode(
                    email,
                    code
            );

        } catch (Exception e) {

            /*
             * If email could not be sent,
             * remove the newly created user only
             * when it was a brand-new account.
             */

            if (savedUser.getId() != null) {

                // Keep account so user can resend OTP.
            }

            return ResponseEntity
                    .internalServerError()
                    .body(
                            "Account created, but verification email " +
                            "could not be sent. Please use Resend Code."
                    );
        }


        return ResponseEntity.ok(
                new RegisterResponse(
                        "Registration started. " +
                        "Please check your email for the verification code.",
                        email
                )
        );
    }


    // =========================================================
    // VERIFY EMAIL
    // =========================================================

    @PostMapping("/verify-email")
    public ResponseEntity<?> verifyEmail(
            @RequestBody VerifyEmailRequest request) {

        if (request.email() == null ||
                request.code() == null) {

            return ResponseEntity
                    .badRequest()
                    .body("Email and verification code are required");
        }


        String email =
                request.email()
                        .trim()
                        .toLowerCase();


        String code =
                request.code().trim();


        User user =
                userRepository
                        .findByEmail(email)
                        .orElse(null);


        if (user == null) {

            return ResponseEntity
                    .badRequest()
                    .body("User not found");
        }


        EmailVerification verification =
                verificationRepository
                        .findByEmail(email)
                        .orElse(null);


        if (verification == null) {

            return ResponseEntity
                    .badRequest()
                    .body(
                            "Verification code not found. " +
                            "Please request a new code."
                    );
        }


        if (verification
                .getExpiresAt()
                .isBefore(LocalDateTime.now())) {

            verificationRepository
                    .deleteByEmail(email);

            return ResponseEntity
                    .badRequest()
                    .body(
                            "Verification code has expired. " +
                            "Please request a new code."
                    );
        }


        if (!verification
                .getCode()
                .equals(code)) {

            return ResponseEntity
                    .badRequest()
                    .body("Incorrect verification code");
        }


        /*
         * Email successfully verified.
         */

        user.setEmailVerified(true);

        userRepository.save(user);


        /*
         * OTP is no longer needed.
         */

        verificationRepository
                .deleteByEmail(email);


        return ResponseEntity.ok(
                "Email verified successfully. " +
                "You can now login."
        );
    }


    // =========================================================
    // RESEND CODE
    // =========================================================

    @PostMapping("/resend-code")
    public ResponseEntity<?> resendCode(
            @RequestBody ResendCodeRequest request) {

        if (request.email() == null ||
                request.email().isBlank()) {

            return ResponseEntity
                    .badRequest()
                    .body("Email is required");
        }


        String email =
                request.email()
                        .trim()
                        .toLowerCase();


        User user =
                userRepository
                        .findByEmail(email)
                        .orElse(null);


        if (user == null) {

            return ResponseEntity
                    .badRequest()
                    .body("User not found");
        }


        if (user.isEmailVerified()) {

            return ResponseEntity
                    .badRequest()
                    .body("Email is already verified");
        }


        String code =
                String.format(
                        "%06d",
                        random.nextInt(1_000_000)
                );


        verificationRepository
                .deleteByEmail(email);


        EmailVerification verification =
                new EmailVerification();

        verification.setEmail(email);

        verification.setCode(code);

        verification.setExpiresAt(
                LocalDateTime.now()
                        .plusMinutes(10)
        );


        verificationRepository.save(
                verification
        );


        try {

            emailService.sendVerificationCode(
                    email,
                    code
            );

        } catch (Exception e) {

            return ResponseEntity
                    .internalServerError()
                    .body(
                            "Unable to send verification email"
                    );
        }


        return ResponseEntity.ok(
                "A new verification code has been sent."
        );
    }


    // =========================================================
    // LOGIN
    // =========================================================

    @PostMapping("/login")
    public ResponseEntity<?> loginUser(
            @RequestBody LoginRequest request) {

        String email =
                request.email()
                        .trim()
                        .toLowerCase();


        User user =
                userRepository
                        .findByEmail(email)
                        .orElse(null);


        if (user == null) {

            return ResponseEntity
                    .status(401)
                    .body(
                            "Invalid email or password"
                    );
        }


        /*
         * New accounts must verify their email.
         *
         * Existing users created before this feature
         * are treated as verified.
         */

        if (!user.isEmailVerified()) {

            return ResponseEntity
                    .status(403)
                    .body(
                            "Please verify your email before logging in"
                    );
        }


        boolean passwordMatches;


        /*
         * New passwords use BCrypt.
         *
         * Existing users may still have plain-text passwords
         * from the old version of the application.
         *
         * After a successful old-style login, their password
         * is automatically converted to BCrypt.
         */

        if (user.getPassword().startsWith("$2a$")
                || user.getPassword().startsWith("$2b$")
                || user.getPassword().startsWith("$2y$")) {

            passwordMatches =
                    passwordEncoder.matches(
                            request.password(),
                            user.getPassword()
                    );

        } else {

            passwordMatches =
                    user.getPassword()
                            .equals(request.password());


            /*
             * Upgrade old password to BCrypt.
             */

            if (passwordMatches) {

                user.setPassword(
                        passwordEncoder.encode(
                                request.password()
                        )
                );

                userRepository.save(user);
            }
        }


        if (!passwordMatches) {

            return ResponseEntity
                    .status(401)
                    .body(
                            "Invalid email or password"
                    );
        }


        return ResponseEntity.ok(
                new LoginResponse(
                        user.getId(),
                        user.getName(),
                        user.getEmail(),
                        user.getPhone(),
                        user.getCity()
                )
        );
    }


    // =========================================================
    // REQUEST / RESPONSE RECORDS
    // =========================================================

    public record RegisterRequest(
            String name,
            String email,
            String phone,
            String city,
            String password,
            String confirmPassword
    ) {
    }


    public record VerifyEmailRequest(
            String email,
            String code
    ) {
    }


    public record ResendCodeRequest(
            String email
    ) {
    }


    public record LoginRequest(
            String email,
            String password
    ) {
    }


    public record RegisterResponse(
            String message,
            String email
    ) {
    }


    public record LoginResponse(
            Integer id,
            String name,
            String email,
            String phone,
            String city
    ) {
    }
}