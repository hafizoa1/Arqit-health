package com.ovah.arqithealth.service;

import com.ovah.arqithealth.exception.HospitalNotFoundException;
import com.ovah.arqithealth.exception.RelationshipNotFoundException;
import com.ovah.arqithealth.mockarqit.exception.TrustNotEstablishedException;
import com.ovah.arqithealth.mockarqit.model.KeyResponse;
import com.ovah.arqithealth.model.DTO.RelationshipAcceptanceDTO;
import com.ovah.arqithealth.model.DTO.RelationshipStatusDTO;
import com.ovah.arqithealth.model.HospitalRelationship;
import com.ovah.arqithealth.model.DTO.RelationshipRequestDTO;
import com.ovah.arqithealth.model.enums.RelationshipStatus;
import com.ovah.arqithealth.model.enums.RelationshipType;
import com.ovah.arqithealth.repository.RelationshipRepository;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.util.*;

import static com.ovah.arqithealth.model.enums.RelationshipStatus.*;

@Slf4j
@Service
@AllArgsConstructor
public class HospitalRelationshipService {

    private final RelationshipRepository repository;
    private final RestTemplate restTemplate;

    private static final String ARQIT_KEY_MANAGEMENT_URL = "http://localhost:8082/keys/generate";
    private static final String RELATIONSHIP_ENDPOINT = "/api/v1/ArqitHealth/relationships";

    /**
     * Initiates a relationship request with another hospital.
     * Creates a PENDING relationship locally and notifies the target hospital.
     */
    public ResponseEntity<Void> initiateRelationship(RelationshipRequestDTO requestDto) {
        UUID originHospitalId = requestDto.getOriginHospitalId();
        UUID targetHospitalId = requestDto.getTargetHospitalId();
        RelationshipType relationshipType = requestDto.getRelationshipType();

        // Check if relationship already exists
        if (relationshipExists(originHospitalId, targetHospitalId)) {
            throw new RuntimeException("Relationship between hospitals already exists or is pending");
        }

        // Create PENDING relationship in local DB
        HospitalRelationship localRelationship = createPendingRelationship(
                originHospitalId, targetHospitalId, relationshipType);

        repository.save(localRelationship);
        log.info("Created PENDING relationship: {} -> {}", originHospitalId, targetHospitalId);

        // Notify target hospital
        notifyTargetHospital(targetHospitalId, requestDto);

        return ResponseEntity.ok().build();
    }

    /**
     * Handles incoming relationship initiation request from another hospital.
     */
    public ResponseEntity<Void> handleIncomingInitiation(RelationshipRequestDTO requestDto) {
        UUID originHospitalId = requestDto.getOriginHospitalId();
        UUID targetHospitalId = requestDto.getTargetHospitalId();
        RelationshipType relationshipType = requestDto.getRelationshipType();

        if (relationshipExists(originHospitalId, targetHospitalId)) {
            throw new RuntimeException("Incoming relationship request already exists or is pending");
        }

        HospitalRelationship newRelationship = createPendingRelationship(
                originHospitalId, targetHospitalId, relationshipType);

        repository.save(newRelationship);
        log.info("Received relationship request: {} -> {}", originHospitalId, targetHospitalId);

        return ResponseEntity.ok().build();
    }

    /**
     * Accepts a pending relationship and notifies the initiating hospital.
     */
    public ResponseEntity<Void> acceptRelationshipAndNotify(RelationshipAcceptanceDTO requestDto) {
        UUID acceptingHospitalId = requestDto.getAcceptingHospitalId();
        UUID initiatingHospitalId = requestDto.getInitiatingHospitalId();

        // Find and update pending relationship
        HospitalRelationship relationship = findPendingRelationship(
                initiatingHospitalId, acceptingHospitalId);

        RelationshipRequestDTO requestDTO = RelationshipRequestDTO.builder()
                .originHospitalId(relationship.getOriginHospitalId())
                .targetHospitalId(relationship.getTargetHospitalId())
                .relationshipType(relationship.getRelationshipType())
                .build();

        String sharedSecret = getSharedSecretFromArqit(requestDTO);


        relationship.setRelationshipStatus(ACTIVE);
        relationship.setSharedSecretReference(sharedSecret);
        repository.save(relationship);

        // Create reciprocal relationship
        createReciprocalRelationship(relationship, acceptingHospitalId, initiatingHospitalId);
        log.info("Accepted relationship: {} -> {}", initiatingHospitalId, acceptingHospitalId);

        // Notify initiating hospital
        RelationshipStatusDTO notification = new RelationshipStatusDTO(
                initiatingHospitalId, acceptingHospitalId, ACTIVE);
        notifyHospitalOfStatusChange(initiatingHospitalId, notification);

        return ResponseEntity.ok().build();
    }

    /**
     * Declines a pending relationship and notifies the initiating hospital.
     */
    public ResponseEntity<Void> declineRelationshipAndNotify(RelationshipAcceptanceDTO requestDto) {
        UUID decliningHospitalId = requestDto.getAcceptingHospitalId();
        UUID initiatingHospitalId = requestDto.getInitiatingHospitalId();

        // Find and update pending relationship
        HospitalRelationship relationship = findPendingRelationship(
                initiatingHospitalId, decliningHospitalId);

        relationship.setRelationshipStatus(REJECTED);
        repository.save(relationship);

        // Create reciprocal rejected relationship
        createReciprocalRejectedRelationship(relationship, decliningHospitalId, initiatingHospitalId);

        log.info("Declined relationship: {} -> {}", initiatingHospitalId, decliningHospitalId);

        // Notify initiating hospital
        RelationshipStatusDTO notification = new RelationshipStatusDTO(
                initiatingHospitalId, decliningHospitalId, REJECTED);
        notifyHospitalOfStatusChange(initiatingHospitalId, notification);

        return ResponseEntity.ok().build();
    }

    /**
     * Handles status notification from another hospital.
     */
    public ResponseEntity<Void> handleStatusNotification(RelationshipStatusDTO requestDto) {
        UUID initiatingHospitalId = requestDto.getInitiatingHospitalId();
        UUID acceptingHospitalId = requestDto.getAcceptingHospitalId();
        RelationshipStatus newStatus = requestDto.getNewStatus();

        // Find and update local relationship
        HospitalRelationship relationship = findPendingRelationship(
                initiatingHospitalId, acceptingHospitalId);

        relationship.setRelationshipStatus(newStatus);

        RelationshipRequestDTO requestDTO = RelationshipRequestDTO.builder()
                .originHospitalId(relationship.getOriginHospitalId())
                .targetHospitalId(relationship.getTargetHospitalId())
                .relationshipType(relationship.getRelationshipType())
                .build();

        if (newStatus == ACTIVE) {
            String sharedSecret = getSharedSecretFromArqit(requestDTO);
            relationship.setSharedSecretReference(sharedSecret);
        }

        repository.save(relationship);

        // Update or create reciprocal relationship
        updateReciprocalRelationship(relationship, acceptingHospitalId, initiatingHospitalId, newStatus);

        log.info("Updated relationship status to {}: {} -> {}",
                newStatus, initiatingHospitalId, acceptingHospitalId);

        return ResponseEntity.ok().build();
    }

    /**
     * Retrieves a relationship by ID.
     */
    public ResponseEntity<HospitalRelationship> getRelationship(UUID id) {
        HospitalRelationship relationship = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Relationship not found with ID: " + id));
        return ResponseEntity.ok(relationship);
    }

    /**
     * Retrieves all relationships for a hospital.
     */
    public ResponseEntity<List<HospitalRelationship>> getAllRelationships() {
        List<HospitalRelationship> relationships = repository.findAll();
        return ResponseEntity.ok(relationships);
    }

    /**
     * Verifies if an active trust relationship exists between two hospitals.
     */
    public boolean verifyTrustRelationship(UUID originHospitalId, UUID targetHospitalId) {
        return repository.existsByOriginHospitalIdAndTargetHospitalIdAndRelationshipStatus(
                originHospitalId, targetHospitalId, ACTIVE);
    }

    public String getHospitalBaseUrl(UUID hospitalId) {
        // TODO: Replace with actual hospital registry lookup in production

        String hospitalAUUID = "b29a665f-de3b-49a2-bd92-72206d00e5f1"; // Replace with actual UUID
        String hospitalBUUID = "fcf6110b-30a4-415a-b7bd-29a74bac87c1"; // Replace with actual UUID

        if (hospitalId.toString().equals(hospitalAUUID)) {
            return "http://localhost:8080";
        } else if (hospitalId.toString().equals(hospitalBUUID)) {
            return "http://localhost:8081";
        }

        throw new IllegalArgumentException("Unknown hospital ID: " + hospitalId +
                ". Please ensure it's registered and configured.");
    }

    public UUID getCurrentHospitalId(UUID id) {
        String hospitalAUUID = "b29a665f-de3b-49a2-bd92-72206d00e5f1";
        String hospitalBUUID = "fcf6110b-30a4-415a-b7bd-29a74bac87c1";

        if (id.toString().equals(hospitalAUUID)) {
            return UUID.fromString(hospitalBUUID);
        } else if (id.toString().equals(hospitalBUUID)) {
            return UUID.fromString(hospitalAUUID);
        } else {
            throw new HospitalNotFoundException("This Hospital does not exist");
        }
    }

    public String getSharedSecret(UUID originHospitalId, UUID targetHospitalId) {
        Optional<String> sharedSecret =  repository.findSharedSecretByHospitals(
                originHospitalId, targetHospitalId);

        return sharedSecret.orElse(null);
    }

    // PRIVATE HELPER METHODS

    private boolean relationshipExists(UUID originHospitalId, UUID targetHospitalId) {
        return repository.existsByOriginHospitalIdAndTargetHospitalIdAndRelationshipStatusIn(
                originHospitalId, targetHospitalId, List.of(PENDING, ACTIVE));
    }

    private String getSharedSecretFromArqit(RelationshipRequestDTO requestDto) {

        try {
            ResponseEntity<KeyResponse> keyResponse = restTemplate.postForEntity(
                    ARQIT_KEY_MANAGEMENT_URL, requestDto, KeyResponse.class);

            if (keyResponse.getStatusCode() != HttpStatus.OK ||
                    keyResponse.getBody() == null ||
                    keyResponse.getBody().getKey() == null) {
                throw new RuntimeException("Invalid response from Arqit Key Management Service");
            }

            return keyResponse.getBody().getKey();

        } catch (HttpClientErrorException e) {
            log.error("Failed to get key from Arqit: {} - {}", e.getStatusCode(), e.getResponseBodyAsString());
            throw new RuntimeException("Failed to retrieve key from Arqit Key Management Service", e);
        } catch (Exception e) {
            log.error("Unexpected error contacting Arqit: {}", e.getMessage());
            throw new RuntimeException("Unexpected error contacting Arqit Key Management Service", e);
        }
    }

    private HospitalRelationship createPendingRelationship(UUID originId, UUID targetId,
                                                           RelationshipType type) {
        HospitalRelationship relationship = new HospitalRelationship();
        relationship.setOriginHospitalId(originId);
        relationship.setTargetHospitalId(targetId);
        relationship.setRelationshipType(type);
        relationship.setRelationshipStatus(PENDING);
        return relationship;
    }

    private void notifyTargetHospital(UUID targetHospitalId, RelationshipRequestDTO requestDto) {
        String targetUrl = getHospitalBaseUrl(targetHospitalId) + RELATIONSHIP_ENDPOINT + "/request-acceptance";

        try {
            restTemplate.postForEntity(targetUrl, requestDto, Void.class);
            log.info("Successfully notified target hospital: {}", targetHospitalId);
        } catch (HttpClientErrorException e) {
            log.error("Failed to notify target hospital {}: {} - {}",
                    targetHospitalId, e.getStatusCode(), e.getResponseBodyAsString());
            throw new RuntimeException("Failed to notify target hospital", e);
        }
    }

    private void notifyHospitalOfStatusChange(UUID hospitalId, RelationshipStatusDTO notification) {
        String url = getHospitalBaseUrl(hospitalId) + RELATIONSHIP_ENDPOINT + "/notify-status";

        try {
            restTemplate.postForEntity(url, notification, Void.class);
            log.info("Successfully notified hospital {} of status change", hospitalId);
        } catch (HttpClientErrorException e) {
            log.error("Failed to notify hospital {} of status change: {} - {}",
                    hospitalId, e.getStatusCode(), e.getResponseBodyAsString());
            throw new RuntimeException("Failed to notify hospital of status change", e);
        }
    }

    private HospitalRelationship findPendingRelationship(UUID originId, UUID targetId) {
        return repository.findByOriginHospitalIdAndTargetHospitalIdAndRelationshipStatus(
                        originId, targetId, PENDING)
                .orElseThrow(() -> new RuntimeException(
                        "Pending relationship not found: " + originId + " -> " + targetId));
    }

    private void createReciprocalRelationship(HospitalRelationship original, UUID acceptingId, UUID initiatingId) {
        Optional<HospitalRelationship> existing = repository.findByOriginHospitalIdAndTargetHospitalId(
                acceptingId, initiatingId);

        HospitalRelationship reciprocal;
        if (existing.isPresent()) {
            reciprocal = existing.get();
            reciprocal.setRelationshipStatus(ACTIVE);
        } else {
            reciprocal = new HospitalRelationship();
            reciprocal.setOriginHospitalId(acceptingId);
            reciprocal.setTargetHospitalId(initiatingId);
            reciprocal.setRelationshipType(original.getRelationshipType());
            reciprocal.setRelationshipStatus(ACTIVE);
        }
        reciprocal.setSharedSecretReference(original.getSharedSecretReference());
        repository.save(reciprocal);
    }

    private void createReciprocalRejectedRelationship(HospitalRelationship original, UUID decliningId, UUID initiatingId) {
        Optional<HospitalRelationship> existing = repository.findByOriginHospitalIdAndTargetHospitalId(
                decliningId, initiatingId);

        HospitalRelationship reciprocal;
        if (existing.isPresent()) {
            reciprocal = existing.get();
        } else {
            reciprocal = new HospitalRelationship();
            reciprocal.setOriginHospitalId(decliningId);
            reciprocal.setTargetHospitalId(initiatingId);
            reciprocal.setRelationshipType(original.getRelationshipType());
        }
        reciprocal.setRelationshipStatus(REJECTED);
        reciprocal.setSharedSecretReference(null);
        repository.save(reciprocal);
    }

    private void updateReciprocalRelationship(HospitalRelationship original, UUID acceptingId,
                                              UUID initiatingId, RelationshipStatus status) {
        Optional<HospitalRelationship> existing = repository.findByOriginHospitalIdAndTargetHospitalId(
                acceptingId, initiatingId);

        HospitalRelationship reciprocal;
        if (existing.isPresent()) {
            reciprocal = existing.get();
        } else {
            reciprocal = new HospitalRelationship();
            reciprocal.setOriginHospitalId(acceptingId);
            reciprocal.setTargetHospitalId(initiatingId);
            reciprocal.setRelationshipType(original.getRelationshipType());
        }

        reciprocal.setRelationshipStatus(status);
        if (status == ACTIVE) {
            reciprocal.setSharedSecretReference(original.getSharedSecretReference());
        } else {
            reciprocal.setSharedSecretReference(null);
        }
        repository.save(reciprocal);
    }

}