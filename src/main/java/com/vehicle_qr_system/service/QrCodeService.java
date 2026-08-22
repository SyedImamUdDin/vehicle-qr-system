package com.vehicle_qr_system.service;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;

@Service
public class QrCodeService {

    @Value("${APP_BASE_URL:http://localhost:8080}")
    private String appBaseUrl;

    public String generateQrCode(Integer vehicleId) {

        // This is the URL that gets encoded inside the QR code.
        String qrContent =
                appBaseUrl + "/vehicle.html?id=" + vehicleId;

        // The browser-accessible URL stored in the database.
        return "/qrcodes/vehicle-" + vehicleId + ".png";
    }

    public byte[] generateQrCodeImage(Integer vehicleId) throws Exception {

        String qrContent =
                appBaseUrl + "/vehicle.html?id=" + vehicleId;

        int width = 300;
        int height = 300;

        BitMatrix matrix = new MultiFormatWriter().encode(
                qrContent,
                BarcodeFormat.QR_CODE,
                width,
                height
        );

        ByteArrayOutputStream outputStream =
                new ByteArrayOutputStream();

        MatrixToImageWriter.writeToStream(
                matrix,
                "PNG",
                outputStream
        );

        return outputStream.toByteArray();
    }
}