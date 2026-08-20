package com.vehicle_qr_system.controller;

import com.vehicle_qr_system.model.Vehicle;
import com.vehicle_qr_system.repository.EmergencyContactRepository;
import com.vehicle_qr_system.repository.VehicleRepository;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/public/vehicles")
public class PublicVehicleController {

    private final VehicleRepository vehicleRepository;
    private final EmergencyContactRepository contactRepository;

    public PublicVehicleController(
            VehicleRepository vehicleRepository,
            EmergencyContactRepository contactRepository) {

        this.vehicleRepository = vehicleRepository;
        this.contactRepository = contactRepository;
    }

    // Normal information shown immediately after scanning QR
    @GetMapping("/{vehicleId}")
    public ResponseEntity<?> getPublicVehicle(
            @PathVariable Integer vehicleId) {

        Vehicle vehicle = vehicleRepository
                .findById(vehicleId)
                .orElse(null);

        if (vehicle == null) {
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.ok(
                new PublicVehicleResponse(

                        new OwnerResponse(
                                vehicle.getUser().getName(),
                                vehicle.getUser().getPhone(),
                                vehicle.getUser().getCity()
                        ),

                        new VehicleResponse(
                                vehicle.getRegistrationNumber(),
                                vehicle.getModel(),
                                vehicle.getColor()
                        )
                )
        );
    }

    // Emergency contacts are returned ONLY after pressing Emergency
    @GetMapping("/{vehicleId}/emergency")
    public ResponseEntity<?> getEmergencyContacts(
            @PathVariable Integer vehicleId) {

        Vehicle vehicle = vehicleRepository
                .findById(vehicleId)
                .orElse(null);

        if (vehicle == null) {
            return ResponseEntity.notFound().build();
        }

        List<EmergencyContactResponse> contacts =
                contactRepository
                        .findByUserIdOrderByPriorityAsc(
                                vehicle.getUser().getId()
                        )
                        .stream()
                        .map(contact -> new EmergencyContactResponse(
                                contact.getName(),
                                contact.getRelationship(),
                                contact.getPhone(),
                                contact.getPriority()
                        ))
                        .toList();

        return ResponseEntity.ok(contacts);
    }

    // Owner information
    public record OwnerResponse(
            String name,
            String phone,
            String city
    ) {
    }

    // Vehicle information
    public record VehicleResponse(
            String registrationNumber,
            String model,
            String color
    ) {
    }

    // Main public response
    public record PublicVehicleResponse(
            OwnerResponse owner,
            VehicleResponse vehicle
    ) {
    }

    // Emergency contact response
    public record EmergencyContactResponse(
            String name,
            String relationship,
            String phone,
            Integer priority
    ) {
    }
}