package com.vehicle_qr_system.service;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;

import org.springframework.stereotype.Service;

import java.nio.file.Files;
import java.nio.file.Path;

@Service
public class QrCodeService {

    public String generateQrCode(Integer vehicleId) throws Exception {

        String qrContent =
                "http://192.168.1.3:8080/vehicle.html?id=" + vehicleId;

        int width = 300;
        int height = 300;

        BitMatrix matrix = new MultiFormatWriter().encode(
                qrContent,
                BarcodeFormat.QR_CODE,
                width,
                height
        );

        Path directory = Path.of("qrcodes");

        if (!Files.exists(directory)) {
            Files.createDirectories(directory);
        }

        Path filePath =
                directory.resolve("vehicle-" + vehicleId + ".png");

        MatrixToImageWriter.writeToPath(
                matrix,
                "PNG",
                filePath
        );

        // Return browser-accessible URL
        return "/qrcodes/vehicle-" + vehicleId + ".png";
    }
}