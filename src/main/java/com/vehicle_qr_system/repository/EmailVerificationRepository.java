package com.vehicle_qr_system.repository;

import com.vehicle_qr_system.model.EmailVerification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

public interface EmailVerificationRepository
        extends JpaRepository<EmailVerification, Integer> {

    Optional<EmailVerification> findByEmail(String email);

    @Modifying
    @Transactional
    @Query("DELETE FROM EmailVerification e WHERE e.email = :email")
    void deleteByEmail(String email);
}