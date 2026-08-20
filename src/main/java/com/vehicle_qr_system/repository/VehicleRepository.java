package com.vehicle_qr_system.repository;

import com.vehicle_qr_system.model.Vehicle;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface VehicleRepository extends JpaRepository<Vehicle, Integer> {

    List<Vehicle> findByUserId(Integer userId);
}