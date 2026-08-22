package com.vehicle_qr_system.controller;

import com.vehicle_qr_system.model.EmailVerification;
import com.vehicle_qr_system.model.PasswordResetCode;
import com.vehicle_qr_system.model.User;
import com.vehicle_qr_system.repository.EmailVerificationRepository;
import com.vehicle_qr_system.repository.PasswordResetCodeRepository;
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
    private final PasswordResetCodeRepository passwordResetCodeRepository;
    private final EmailService emailService;

    private final BCryptPasswordEncoder passwordEncoder =
            new BCryptPasswordEncoder();

    private final Random random = new Random();

    public UserController(
            UserRepository userRepository,
            EmailVerificationRepository verificationRepository,
            PasswordResetCodeRepository passwordResetCodeRepository,
            EmailService emailService) {

        this.userRepository = userRepository;
        this.verificationRepository = verificationRepository;
        this.passwordResetCodeRepository = passwordResetCodeRepository;
        this.emailService = emailService;
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

        if (!isStrongPassword(request.password())) {

            return ResponseEntity
                    .badRequest()
                    .body(
                            "Password must be at least 8 characters and include "
                            + "an uppercase letter, lowercase letter, number, "
                            + "and special character."
                    );
        }

        if (request.confirmPassword() == null ||
                !request.password()
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
                generateSixDigitCode();

        /*
         * Remove previous verification code.
         */
        verificationRepository.deleteByEmail(email);

        /*
         * Create new verification code.
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
         * Send verification email.
         */
        try {

            emailService.sendVerificationCode(
                    email,
                    code
            );

        } catch (Exception e) {

            e.printStackTrace();

            return ResponseEntity
                    .internalServerError()
                    .body(
                            "Account created, but verification email failed: "
                            + e.getMessage()
                    );
        }

        return ResponseEntity.ok(
                new RegisterResponse(
                        "Registration started. "
                        + "Please check your email for the verification code.",
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
                    .body(
                            "Email and verification code are required"
                    );
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
                            "Verification code not found. "
                            + "Please request a new code."
                    );
        }

        if (verification
                .getExpiresAt()
                .isBefore(LocalDateTime.now())) {

            verificationRepository.deleteByEmail(email);

            return ResponseEntity
                    .badRequest()
                    .body(
                            "Verification code has expired. "
                            + "Please request a new code."
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
        verificationRepository.deleteByEmail(email);

        return ResponseEntity.ok(
                "Email verified successfully. "
                + "You can now login."
        );
    }

    // =========================================================
    // RESEND VERIFICATION CODE
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
                generateSixDigitCode();

        verificationRepository.deleteByEmail(email);

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

            e.printStackTrace();

            return ResponseEntity
                    .internalServerError()
                    .body(
                            "Unable to send verification email: "
                            + e.getMessage()
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

        if (request.email() == null ||
                request.password() == null) {

            return ResponseEntity
                    .status(401)
                    .body("Invalid email or password");
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
                    .status(401)
                    .body("Invalid email or password");
        }

        /*
         * New accounts must verify their email.
         */
        if (!user.isEmailVerified()) {

            return ResponseEntity
                    .status(403)
                    .body(
                            "Please verify your email before logging in"
                    );
        }

        boolean passwordMatches;

        String storedPassword =
                user.getPassword();

        /*
         * New passwords use BCrypt.
         *
         * Existing users may still have plain-text
         * passwords from the old version.
         */
        if (storedPassword != null &&
                (storedPassword.startsWith("$2a$")
                        || storedPassword.startsWith("$2b$")
                        || storedPassword.startsWith("$2y$"))) {

            passwordMatches =
                    passwordEncoder.matches(
                            request.password(),
                            storedPassword
                    );

        } else {

            /*
             * Legacy plain-text password support.
             */
            passwordMatches =
                    storedPassword != null &&
                            storedPassword.equals(
                                    request.password()
                            );

            /*
             * Automatically upgrade old password
             * to BCrypt after successful login.
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
    // FORGOT PASSWORD
    // =========================================================

    @PostMapping("/forgot-password")
    public ResponseEntity<?> forgotPassword(
            @RequestBody ForgotPasswordRequest request) {

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

        /*
         * Do not reveal whether an email exists.
         */
        if (user == null) {

            return ResponseEntity.ok(
                    "If an account exists for that email, "
                    + "a password reset code has been sent."
            );
        }

        if (!user.isEmailVerified()) {

            return ResponseEntity
                    .badRequest()
                    .body(
                            "Please verify your email before "
                            + "resetting your password."
                    );
        }

        /*
         * Generate new reset code.
         */
        String code =
                generateSixDigitCode();

        /*
         * Remove previous reset code.
         */
        passwordResetCodeRepository
                .deleteByEmail(email);

        /*
         * Create new reset code.
         */
        PasswordResetCode resetCode =
                new PasswordResetCode();

        resetCode.setEmail(email);
        resetCode.setCode(code);

        resetCode.setExpiresAt(
                LocalDateTime.now()
                        .plusMinutes(10)
        );

        passwordResetCodeRepository.save(
                resetCode
        );

        /*
         * Send reset email.
         */
        try {

            emailService.sendPasswordResetCode(
                    email,
                    code
            );

        } catch (Exception e) {

            e.printStackTrace();

            return ResponseEntity
                    .internalServerError()
                    .body(
                            "Password reset email failed: "
                            + e.getMessage()
                    );
        }

        return ResponseEntity.ok(
                new ForgotPasswordResponse(
                        "If an account exists for that email, "
                        + "a password reset code has been sent.",
                        email
                )
        );
    }

    // =========================================================
    // RESET PASSWORD
    // =========================================================

    @PostMapping("/reset-password")
    public ResponseEntity<?> resetPassword(
            @RequestBody ResetPasswordRequest request) {

        if (request.email() == null ||
                request.code() == null ||
                request.newPassword() == null ||
                request.confirmPassword() == null) {

            return ResponseEntity
                    .badRequest()
                    .body(
                            "Email, code and password are required"
                    );
        }

        if (!isStrongPassword(
                request.newPassword())) {

            return ResponseEntity
                    .badRequest()
                    .body(
                            "Password must be at least 8 characters and include "
                            + "an uppercase letter, lowercase letter, number, "
                            + "and special character."
                    );
        }

        if (!request.newPassword()
                .equals(request.confirmPassword())) {

            return ResponseEntity
                    .badRequest()
                    .body("Passwords do not match");
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
                    .body(
                            "Unable to reset password"
                    );
        }

        PasswordResetCode resetCode =
                passwordResetCodeRepository
                        .findByEmail(email)
                        .orElse(null);

        if (resetCode == null) {

            return ResponseEntity
                    .badRequest()
                    .body(
                            "Reset code not found. "
                            + "Please request a new code."
                    );
        }

        if (resetCode
                .getExpiresAt()
                .isBefore(LocalDateTime.now())) {

            passwordResetCodeRepository
                    .deleteByEmail(email);

            return ResponseEntity
                    .badRequest()
                    .body(
                            "Reset code has expired. "
                            + "Please request a new code."
                    );
        }

        if (!resetCode
                .getCode()
                .equals(code)) {

            return ResponseEntity
                    .badRequest()
                    .body("Incorrect reset code");
        }

        /*
         * Save new password securely with BCrypt.
         */
        user.setPassword(
                passwordEncoder.encode(
                        request.newPassword()
                )
        );

        userRepository.save(user);

        /*
         * Reset code can no longer be reused.
         */
        passwordResetCodeRepository
                .deleteByEmail(email);

        return ResponseEntity.ok(
                "Password reset successfully. "
                + "You can now login."
        );
    }

    // =========================================================
    // HELPER METHODS
    // =========================================================

    private String generateSixDigitCode() {

        return String.format(
                "%06d",
                random.nextInt(1_000_000)
        );
    }

    private boolean isStrongPassword(
            String password) {

        if (password == null ||
                password.length() < 8) {

            return false;
        }

        return password.matches(".*[A-Z].*")
                && password.matches(".*[a-z].*")
                && password.matches(".*[0-9].*")
                && password.matches(".*[^A-Za-z0-9].*");
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

    public record ForgotPasswordRequest(
            String email
    ) {
    }

    public record ResetPasswordRequest(
            String email,
            String code,
            String newPassword,
            String confirmPassword
    ) {
    }

    public record RegisterResponse(
            String message,
            String email
    ) {
    }

    public record ForgotPasswordResponse(
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