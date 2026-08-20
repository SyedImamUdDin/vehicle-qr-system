package com.vehicle_qr_system.controller;

import com.vehicle_qr_system.model.EmergencyContact;
import com.vehicle_qr_system.model.User;
import com.vehicle_qr_system.repository.EmergencyContactRepository;
import com.vehicle_qr_system.repository.UserRepository;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/emergency-contacts")
public class EmergencyContactController {

    private final EmergencyContactRepository emergencyContactRepository;
    private final UserRepository userRepository;

    public EmergencyContactController(
            EmergencyContactRepository emergencyContactRepository,
            UserRepository userRepository) {

        this.emergencyContactRepository =
                emergencyContactRepository;

        this.userRepository =
                userRepository;
    }


    // ==========================================
    // GET ALL CONTACTS FOR A USER
    // ==========================================

    @GetMapping("/user/{userId}")
    public ResponseEntity<List<EmergencyContact>> getUserContacts(
            @PathVariable Integer userId) {

        return ResponseEntity.ok(
                emergencyContactRepository.findByUserId(userId)
        );
    }


    // ==========================================
    // ADD EMERGENCY CONTACT
    // ==========================================

    @PostMapping
    public ResponseEntity<?> addContact(
            @RequestBody EmergencyContactRequest request) {

        User user =
                userRepository.findById(request.userId())
                        .orElse(null);

        if (user == null) {

            return ResponseEntity
                    .badRequest()
                    .body("User not found");
        }


        EmergencyContact contact =
                new EmergencyContact();

        contact.setName(request.name());
        contact.setRelationship(request.relationship());
        contact.setPhone(request.phone());
        contact.setPriority(request.priority());
        contact.setUser(user);


        EmergencyContact saved =
                emergencyContactRepository.save(contact);


        return ResponseEntity.ok(saved);
    }


    // ==========================================
    // UPDATE EMERGENCY CONTACT
    // ==========================================

    @PutMapping("/{id}")
    public ResponseEntity<?> updateContact(
            @PathVariable Integer id,
            @RequestBody EmergencyContactRequest request) {

        EmergencyContact contact =
                emergencyContactRepository
                        .findById(id)
                        .orElse(null);


        if (contact == null) {

            return ResponseEntity
                    .notFound()
                    .build();
        }


        contact.setName(request.name());
        contact.setRelationship(request.relationship());
        contact.setPhone(request.phone());
        contact.setPriority(request.priority());


        EmergencyContact updated =
                emergencyContactRepository.save(contact);


        return ResponseEntity.ok(updated);
    }


    // ==========================================
    // DELETE CONTACT
    // ==========================================

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteContact(
            @PathVariable Integer id) {

        if (!emergencyContactRepository.existsById(id)) {

            return ResponseEntity
                    .notFound()
                    .build();
        }


        emergencyContactRepository.deleteById(id);


        return ResponseEntity.ok(
                "Emergency contact deleted successfully"
        );
    }


    // ==========================================
    // REQUEST RECORD
    // ==========================================

    public record EmergencyContactRequest(
            Integer userId,
            String name,
            String relationship,
            String phone,
            Integer priority
    ) {
    }

}