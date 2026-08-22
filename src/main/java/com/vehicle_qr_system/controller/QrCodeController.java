package com.vehicle_qr_system.controller;

import com.vehicle_qr_system.service.QrCodeService;

import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
public class QrCodeController {

    private final QrCodeService qrCodeService;

    public QrCodeController(QrCodeService qrCodeService) {
        this.qrCodeService = qrCodeService;
    }

    @PostMapping("/api/qr/generate/{vehicleId}")
    public ResponseEntity<?> generateQrCode(
            @PathVariable Integer vehicleId) {

        try {

            String filePath =
                    qrCodeService.generateQrCode(vehicleId);

            return ResponseEntity.ok(filePath);

        } catch (Exception e) {

            return ResponseEntity.internalServerError()
                    .body("QR generation failed: " + e.getMessage());
        }
    }

    @GetMapping(
            value = "/qrcodes/vehicle-{vehicleId}.png",
            produces = MediaType.IMAGE_PNG_VALUE
    )
    public ResponseEntity<byte[]> getQrCode(
            @PathVariable Integer vehicleId) {

        try {

            byte[] image =
                    qrCodeService.generateQrCodeImage(vehicleId);

            return ResponseEntity.ok(image);

        } catch (Exception e) {

            return ResponseEntity.notFound().build();
        }
    }
}