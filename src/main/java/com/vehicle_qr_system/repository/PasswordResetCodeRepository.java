package com.vehicle_qr_system.repository;

import com.vehicle_qr_system.model.PasswordResetCode;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

public interface PasswordResetCodeRepository
        extends JpaRepository<PasswordResetCode, Integer> {

    Optional<PasswordResetCode> findByEmail(String email);

    @Modifying
    @Transactional
    @Query("delete from PasswordResetCode p where p.email = :email")
    void deleteByEmail(String email);
}