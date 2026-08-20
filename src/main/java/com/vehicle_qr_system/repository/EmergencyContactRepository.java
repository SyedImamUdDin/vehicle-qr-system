package com.vehicle_qr_system.repository;

import com.vehicle_qr_system.model.EmergencyContact;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface EmergencyContactRepository
        extends JpaRepository<EmergencyContact, Integer> {

    // Used by owner dashboard
    List<EmergencyContact> findByUserId(Integer userId);

    // Used by public QR emergency system
    List<EmergencyContact> findByUserIdOrderByPriorityAsc(Integer userId);
}