package com.vehicle_qr_system.controller;

import com.vehicle_qr_system.service.QrCodeService;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/qr")
public class QrCodeController {

    private final QrCodeService qrCodeService;

    public QrCodeController(QrCodeService qrCodeService) {
        this.qrCodeService = qrCodeService;
    }

    @PostMapping("/generate/{vehicleId}")
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
}