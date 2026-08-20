package com.vehicle_qr_system.controller;

import com.vehicle_qr_system.model.User;
import com.vehicle_qr_system.model.Vehicle;
import com.vehicle_qr_system.repository.UserRepository;
import com.vehicle_qr_system.repository.VehicleRepository;
import com.vehicle_qr_system.service.QrCodeService;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/vehicles")
public class VehicleController {

    private final VehicleRepository vehicleRepository;
    private final UserRepository userRepository;
    private final QrCodeService qrCodeService;

    public VehicleController(
            VehicleRepository vehicleRepository,
            UserRepository userRepository,
            QrCodeService qrCodeService) {

        this.vehicleRepository = vehicleRepository;
        this.userRepository = userRepository;
        this.qrCodeService = qrCodeService;
    }


    // ==========================================
    // ADD VEHICLE
    // ==========================================

    @PostMapping
    public ResponseEntity<?> addVehicle(
            @RequestBody VehicleRequest request) {

        try {

            User user =
                    userRepository.findById(request.userId())
                            .orElseThrow(
                                    () -> new RuntimeException(
                                            "User not found"
                                    )
                            );


            Vehicle vehicle = new Vehicle();

            vehicle.setUser(user);
            vehicle.setRegistrationNumber(
                    request.registrationNumber()
            );
            vehicle.setModel(request.model());
            vehicle.setColor(request.color());


            Vehicle savedVehicle =
                    vehicleRepository.save(vehicle);


            // Generate QR after vehicle gets its ID

            String qrCode =
                    qrCodeService.generateQrCode(
                            savedVehicle.getId()
                    );


            savedVehicle.setQrCode(qrCode);

            savedVehicle =
                    vehicleRepository.save(savedVehicle);


            return ResponseEntity.ok(savedVehicle);

        }

        catch (Exception e) {

            return ResponseEntity
                    .badRequest()
                    .body(e.getMessage());

        }

    }


    // ==========================================
    // GET USER VEHICLES
    // ==========================================

    @GetMapping("/user/{userId}")
    public ResponseEntity<List<Vehicle>> getUserVehicles(
            @PathVariable Integer userId) {

        return ResponseEntity.ok(
                vehicleRepository.findByUserId(userId)
        );

    }


    // ==========================================
    // GET ONE VEHICLE
    // ==========================================

    @GetMapping("/{id}")
    public ResponseEntity<?> getVehicle(
            @PathVariable Integer id) {

        return vehicleRepository.findById(id)
                .map(ResponseEntity::ok)
                .orElse(
                        ResponseEntity.notFound().build()
                );

    }


    // ==========================================
    // UPDATE VEHICLE
    // ==========================================

    @PutMapping("/{id}")
    public ResponseEntity<?> updateVehicle(
            @PathVariable Integer id,
            @RequestBody UpdateVehicleRequest request) {

        try {

            Vehicle vehicle =
                    vehicleRepository.findById(id)
                            .orElseThrow(
                                    () -> new RuntimeException(
                                            "Vehicle not found"
                                    )
                            );


            vehicle.setRegistrationNumber(
                    request.registrationNumber()
            );

            vehicle.setModel(
                    request.model()
            );

            vehicle.setColor(
                    request.color()
            );


            Vehicle updatedVehicle =
                    vehicleRepository.save(vehicle);


            return ResponseEntity.ok(updatedVehicle);

        }

        catch (Exception e) {

            return ResponseEntity
                    .badRequest()
                    .body(e.getMessage());

        }

    }


    // ==========================================
    // DELETE VEHICLE
    // ==========================================

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteVehicle(
            @PathVariable Integer id) {

        try {

            if (!vehicleRepository.existsById(id)) {

                return ResponseEntity
                        .notFound()
                        .build();

            }


            vehicleRepository.deleteById(id);


            return ResponseEntity.ok(
                    "Vehicle deleted successfully"
            );

        }

        catch (Exception e) {

            return ResponseEntity
                    .badRequest()
                    .body(e.getMessage());

        }

    }


    // ==========================================
    // REQUEST RECORDS
    // ==========================================

    public record VehicleRequest(
            Integer userId,
            String registrationNumber,
            String model,
            String color
    ) {}


    public record UpdateVehicleRequest(
            String registrationNumber,
            String model,
            String color
    ) {}

}