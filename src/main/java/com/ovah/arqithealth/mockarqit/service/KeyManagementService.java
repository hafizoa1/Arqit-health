package com.ovah.arqithealth.mockarqit.service;

import com.ovah.arqithealth.mockarqit.model.KeyResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.nio.ByteBuffer;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Service
public class

KeyManagementService {

    private final SecureRandom secureRandom = new SecureRandom();


    // In-memory storage for demo purposes (use database in production)
    private final Map<String, byte[]> sharedSecretStore = new ConcurrentHashMap<>();
    private final Map<String, KeyResponse> keyResponseStore = new ConcurrentHashMap<>();

    /**
     * Generates a shared secret and returns key reference for hospital relationship
     */
    public ResponseEntity<KeyResponse> generateKey(UUID localHospitalId, UUID remoteHospitalId) {
        // Create deterministic key reference for hospital pair
        String keyReference = createKeyReference(localHospitalId, remoteHospitalId);

        // Generate shared secret (simulating Arqit quantum key generation)
        byte[] sharedSecret = generateQuantumSharedSecret(localHospitalId, remoteHospitalId);

        // Store the shared secret for later retrieval
        sharedSecretStore.put(keyReference, sharedSecret);

        // Create response with key reference
        KeyResponse keyResponse = new KeyResponse();
        keyResponse.setKey(keyReference); // Return the reference, not the actual key

        // Store for later retrieval
        keyResponseStore.put(keyReference, keyResponse);

        log.info("Generated quantum key for hospitals: {} <-> {}, reference: {}",
                localHospitalId, remoteHospitalId, keyReference);

        return ResponseEntity.ok(keyResponse);
    }

    /**
     * Retrieves shared secret by key reference (used during encryption/decryption)
     */
    public ResponseEntity<KeyResponse> getSharedSecret(String keyReference) {
        byte[] sharedSecret = sharedSecretStore.get(keyReference);

        if (sharedSecret == null) {
            log.error("Shared secret not found for key reference: {}", keyReference);
            return ResponseEntity.notFound().build();
        }

        // For security, we return the actual shared secret only when specifically requested
        KeyResponse response = new KeyResponse();
        response.setKey(Base64.getEncoder().encodeToString(sharedSecret));

        log.info("Retrieved shared secret for key reference: {}", keyReference);
        return ResponseEntity.ok(response);
    }



    /**
     * Generates deterministic document parameters (non-deterministic as requested)
     */
    public byte[] generateDocumentParameters() {
        byte[] parameters = new byte[32];
        secureRandom.nextBytes(parameters); // Non-deterministic random bytes
        log.debug("Generated random document parameters");
        return parameters;
    }

    /**
     * Generates symmetric encryption key from shared secret and document parameters
     */
    public byte[] generateSymmetricKey(byte[] sharedSecret, byte[] documentParameters) {
        try {
            // Combine shared secret and document parameters
            ByteBuffer buffer = ByteBuffer.allocate(sharedSecret.length + documentParameters.length);
            buffer.put(sharedSecret);
            buffer.put(documentParameters);

            // Use SHA-256 for key derivation
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] symmetricKey = digest.digest(buffer.array());

            log.debug("Generated symmetric key from shared secret and parameters");
            return symmetricKey;

        } catch (Exception e) {
            log.error("Failed to generate symmetric key", e);
            throw new RuntimeException("Failed to generate symmetric key", e);
        }
    }

    /**
     * Retrieves shared secret bytes by key reference (internal method)
     */
    public byte[] getSharedSecretBytes(String keyReference) {
        byte[] sharedSecret = sharedSecretStore.get(keyReference);
        if (sharedSecret == null) {
            throw new RuntimeException("Shared secret not found for reference: " + keyReference);
        }
        return sharedSecret;
    }

    // PRIVATE HELPER METHODS

    /**
     * Creates a deterministic key reference for hospital pair
     */
    private String createKeyReference(UUID hospitalA, UUID hospitalB) {
        // Create consistent reference regardless of order (A->B same as B->A)
        String first = hospitalA.compareTo(hospitalB) < 0 ? hospitalA.toString() : hospitalB.toString();
        String second = hospitalA.compareTo(hospitalB) < 0 ? hospitalB.toString() : hospitalA.toString();

        String combined = first + ":" + second;
        return Base64.getEncoder().encodeToString(combined.getBytes());
    }

    /**
     * Simulates Arqit quantum key generation
     */
    private byte[] generateQuantumSharedSecret(UUID hospitalA, UUID hospitalB) {
        try {
            // Create deterministic but secure shared secret for hospital pair
            // In real Arqit implementation, this would be quantum-generated
            String keyMaterial = createKeyReference(hospitalA, hospitalB) + ":quantum-key";

            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] baseSecret = digest.digest(keyMaterial.getBytes());

            // Add some randomness while keeping it deterministic for the same hospital pair
            ByteBuffer buffer = ByteBuffer.allocate(baseSecret.length + 8);
            buffer.put(baseSecret);
            buffer.putLong(hospitalA.hashCode() ^ hospitalB.hashCode()); // Deterministic but unique

            return digest.digest(buffer.array());

        } catch (Exception e) {
            log.error("Failed to generate quantum shared secret", e);
            throw new RuntimeException("Failed to generate quantum shared secret", e);
        }
    }

    /**
     * For testing/demo purposes - clear all stored keys
     */
    public void clearAllKeys() {
        sharedSecretStore.clear();
        keyResponseStore.clear();
        log.info("Cleared all stored keys");
    }
}