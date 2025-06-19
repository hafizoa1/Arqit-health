package com.ovah.arqithealth.mockarqit.controller;

import com.ovah.arqithealth.mockarqit.model.KeyResponse;
import com.ovah.arqithealth.mockarqit.service.KeyManagementService;
import com.ovah.arqithealth.model.DTO.RelationshipRequestDTO;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/keys")
@AllArgsConstructor
public class KeyManagementController {

    private final KeyManagementService keyManagementService;

    /**
     * Generates a quantum shared secret for hospital relationship establishment
     * Called during hospital relationship initiation
     */
    @PostMapping("/generate")
    public ResponseEntity<KeyResponse> generateKey(@Valid @RequestBody RelationshipRequestDTO request) {
        log.info("Generating quantum key for hospitals: {} -> {}",
                request.getOriginHospitalId(), request.getTargetHospitalId());

        return keyManagementService.generateKey(
                request.getOriginHospitalId(),
                request.getTargetHospitalId()
        );
    }

    /**
     * Retrieves shared secret by key reference
     * Called during document encryption/decryption operations
     */
    @GetMapping("/{keyReference}")
    public ResponseEntity<KeyResponse> getSharedSecret(@PathVariable String keyReference) {
        log.info("Retrieving shared secret for key reference: {}", keyReference);
        return keyManagementService.getSharedSecret(keyReference);
    }

    /**
     * Demo helper endpoint to clear all stored keys (for testing purposes)
     */
    @DeleteMapping("/clear")
    public ResponseEntity<Void> clearAllKeys() {
        log.info("Clearing all stored quantum keys");
        keyManagementService.clearAllKeys();
        return ResponseEntity.ok().build();
    }
}
