package com.ovah.arqithealth.service;

import com.ovah.arqithealth.exception.*;
import com.ovah.arqithealth.model.DTO.PatientDTO;
import com.ovah.arqithealth.model.DTO.ReceiveDocumentDTO;
import com.ovah.arqithealth.model.DTO.SharedDocumentDTO;
import com.ovah.arqithealth.model.Patient;
import com.ovah.arqithealth.model.PatientRecord;
import com.ovah.arqithealth.model.SharedDocument;
import com.ovah.arqithealth.model.enums.DocumentStatus;
import com.ovah.arqithealth.repository.DocumentsRepository;
import com.ovah.arqithealth.repository.PatientRepository;
import com.ovah.arqithealth.repository.RecordRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Optional;
import java.util.UUID;



@Service
@Slf4j
public class DocumentSharingService {

    @Autowired
    private DocumentsRepository sharedDocumentRepository;

    @Autowired
    private HospitalRelationshipService hospitalRelationshipService;

    @Autowired
    private EncryptionService encryptionService;

    @Autowired
    private RestTemplate restTemplate;

    private static final String DOCUMENTS_ENDPOINT = "/api/v1/ArqitHealth/documents";

    @Autowired
    private PatientService patientService;

    @Autowired
    private RecordService recordService;

    @Autowired
    private RecordRepository recordRepository;

    @Autowired
    private PatientRepository patientRepository;

    /**
     * Share a document with another hospital - use record Id to get patient details
     */
    public ResponseEntity<String> shareDocument(UUID recordId, UUID targetHospitalId) {

        UUID sourceHospitalId = hospitalRelationshipService.getCurrentHospitalId(targetHospitalId);

        // 1. Check trust relationship
        if (!hospitalRelationshipService.verifyTrustRelationship(sourceHospitalId, targetHospitalId)) {
            throw new TrustRelationshipException(sourceHospitalId, targetHospitalId);
        }

        // 2. Get shared secret from trust relationship
        String sharedSecret = hospitalRelationshipService.getSharedSecret(sourceHospitalId, targetHospitalId);
        if (sharedSecret == null) {
            throw new SharedSecretNotFoundException(sourceHospitalId.toString(), targetHospitalId.toString());
        }

        // 3. Get patient record details
        PatientRecord record = recordRepository.findById(recordId)
                .orElseThrow(() -> new RecordNotFoundException(recordId.toString()));

        UUID patientId = record.getPatientId();
        Patient patient = patientRepository.getPatientByPatientId(patientId);

        // 4. Get document content from storage
        byte[] file;
        try {
            file = getDocumentContent(record.getStorageLocation());
        } catch (Exception e) {
            throw new FileStorageException("read document from storage location: " + record.getStorageLocation(), e);
        }

        // 5. Encrypt document
        byte[] encryptedData;
        try {
            encryptedData = encryptionService.encrypt(file, sharedSecret);
        } catch (Exception e) {
            throw new EncryptionException("encrypt", e);
        }

        // 6. Save shared document to database
        SharedDocument document = new SharedDocument();
        document.setSourceHospitalId(sourceHospitalId);
        document.setTargetHospitalId(targetHospitalId);
        document.setEncryptedData(encryptedData);
        document.setFileName(record.getFilename());
        document.setStatus(DocumentStatus.SHARED);

        SharedDocument savedDocument = sharedDocumentRepository.save(document);

        // 7. Create Document DTO for transmission
        PatientDTO sharedPatient = PatientDTO.builder()
                .firstName(patient.getFirstName())
                .lastName(patient.getLastName())
                .age(patient.getAge())
                .build();

        SharedDocumentDTO sharedDocument = SharedDocumentDTO.builder()
                .sharedDocumentId(savedDocument.getId())
                .senderHospitalId(sourceHospitalId)
                .receiverHospitalId(targetHospitalId)
                .encryptedContent(encryptedData)
                .fileName(record.getFilename())
                .build();

        ReceiveDocumentDTO payload = new ReceiveDocumentDTO();
        payload.setSharedPatient(sharedPatient);
        payload.setDocumentDTO(sharedDocument);
        payload.setDescription(record.getDescription());

        // 8. Send to target hospital
        try {
            sendToTargetHospital(payload);
        } catch (Exception e) {
            throw new DocumentTransmissionException(targetHospitalId.toString(), e);
        }

        log.info("Successfully shared document {} with hospital {}", document.getId(), targetHospitalId);
        return ResponseEntity.ok("Document shared successfully");
    }
    /**
     * Receive a document from another hospital (auto-accept if trust exists)
     */
    public ResponseEntity<String> receiveDocument(ReceiveDocumentDTO payload) {

        PatientDTO sharedPatientDTO = payload.getSharedPatient();
        SharedDocumentDTO dto = payload.getDocumentDTO();
        String description = payload.getDescription();

        // --- 1. Check trust relationship (Pre-transactional check for quick failure) ---
        if (!hospitalRelationshipService.verifyTrustRelationship(dto.getReceiverHospitalId(), dto.getSenderHospitalId())) {
            log.warn("Security violation: No trust relationship with sender hospital {}", dto.getSenderHospitalId());
            throw new TrustRelationshipException(dto.getSenderHospitalId(), dto.getReceiverHospitalId());
        }

        // --- 2. Atomic check for duplicates - load the actual entity if it exists ---
        Optional<SharedDocument> existingDocumentOptional = sharedDocumentRepository.findById(dto.getSharedDocumentId());
        if (existingDocumentOptional.isPresent()) {
            SharedDocument existingDocument = existingDocumentOptional.get();
            log.warn("Document with ID {} already exists with status {}. Acknowledging receipt.",
                    dto.getSharedDocumentId(), existingDocument.getStatus());
            return ResponseEntity.ok("Document already received (previously existing).");
        }

        // --- 3. Get shared secret from trust relationship ---
        String sharedSecret = hospitalRelationshipService.getSharedSecret(dto.getSenderHospitalId(), dto.getReceiverHospitalId());
        if (sharedSecret == null) {
            log.error("Missing shared secret for established trust relationship between {} and {}",
                    dto.getSenderHospitalId(), dto.getReceiverHospitalId());
            throw new SharedSecretNotFoundException(dto.getSenderHospitalId().toString(), dto.getReceiverHospitalId().toString());
        }

        // --- 4. Create patient (within the transaction) ---
        Patient newPatient = new Patient();
        newPatient.setAge(sharedPatientDTO.getAge());
        newPatient.setFirstName(sharedPatientDTO.getFirstName());
        newPatient.setLastName(sharedPatientDTO.getLastName());
        newPatient.setHospitalId(dto.getReceiverHospitalId());

        Patient createdPatient;
        try {
            ResponseEntity<Patient> patientResponse = patientService.createPatient(newPatient);
            if (!patientResponse.getStatusCode().is2xxSuccessful() || patientResponse.getBody() == null) {
                log.error("Patient creation failed or returned empty body. Status: {}, Body: {}",
                        patientResponse.getStatusCode(), patientResponse.getBody());
                throw new PatientCreationException("Patient creation failed during document reception due to external service issue.");
            }
            createdPatient = patientResponse.getBody();
            log.info("Successfully created Patient with ID: {}", createdPatient.getPatientId());
        } catch (Exception e) {
            log.error("Error during patientService.createPatient: {}", e.getMessage(), e);
            throw new PatientCreationException(newPatient.getFirstName(), newPatient.getLastName());
        }
        UUID patientId = createdPatient.getPatientId();

        // --- 5. Decrypt and save patient record (within the transaction) ---
        byte[] decryptedPatientFile;
        try {
            decryptedPatientFile = encryptionService.decrypt(dto.getEncryptedContent(), sharedSecret);
        } catch (Exception e) {
            log.error("Failed to decrypt patient document for ID {}: {}", dto.getSharedDocumentId(), e.getMessage(), e);
            throw new DecryptionException("Failed to decrypt document content." + e);
        }

        String fileName = dto.getFileName();
        try {
            recordService.saveDecryptedPatientRecord(patientId, decryptedPatientFile, fileName, description);
            log.info("Successfully saved decrypted patient record for patient ID {}", patientId);
        } catch (Exception e) {
            log.error("Failed to save decrypted patient record for patient ID {}: {}", patientId, e.getMessage(), e);
            throw new RecordSaveException("Failed to save patient record." + e);
        }

        // --- 6. Save SharedDocument (potential concurrent insert/update handling) ---
        SharedDocument newSharedDocument = new SharedDocument();
        newSharedDocument.setSourceHospitalId(dto.getSenderHospitalId());
        newSharedDocument.setTargetHospitalId(dto.getReceiverHospitalId());
        newSharedDocument.setEncryptedData(dto.getEncryptedContent());
        newSharedDocument.setFileName(dto.getFileName());
        newSharedDocument.setPatientId(patientId);
        newSharedDocument.setStatus(DocumentStatus.ACCEPTED);

        try {
            sharedDocumentRepository.save(newSharedDocument);
            log.info("Successfully persisted new SharedDocument with ID {}", dto.getSharedDocumentId());

        } catch (DataIntegrityViolationException | OptimisticLockingFailureException e) {
            log.warn("Concurrent operation detected for document ID {}. It was likely inserted by another process. Error: {}",
                    dto.getSharedDocumentId(), e.getMessage());
            return ResponseEntity.ok("Document received concurrently by another process. Please verify status.");
        }

        log.info("Document reception and processing completed successfully for ID {}", dto.getSharedDocumentId());
        return ResponseEntity.ok("Document received and accepted successfully.");
    }
    /**
     * Get a specific shared document by ID
     */
    public ResponseEntity<SharedDocument> getSharedDocument(UUID sharedDocumentId) {
        SharedDocument document = sharedDocumentRepository.findById(sharedDocumentId)
                .orElseThrow(() -> new DocumentNotFoundException("Document not found: " + sharedDocumentId));

        return ResponseEntity.ok(document);
    }

    private void sendToTargetHospital(ReceiveDocumentDTO payload) {

        UUID receivingHospital = payload.getDocumentDTO().getReceiverHospitalId();

        try {
            String targetUrl = hospitalRelationshipService.getHospitalBaseUrl(receivingHospital)
                    + DOCUMENTS_ENDPOINT + "/receive-document";

            restTemplate.postForEntity(targetUrl, payload, String.class);
            log.info("Successfully notified target hospital {}", receivingHospital);

        } catch (Exception e) {
            log.error("Failed to notify target hospital: {}", e.getMessage());
            throw new ExternalServiceException("Failed to notify target hospital" +  e);
        }
    }

    private byte[] getDocumentContent(String storageLocation) {
        try {
            Path filePath = Paths.get(storageLocation);

            // Check file exists
            if (!Files.exists(filePath)) {
                throw new FileStorageException("find", storageLocation);
            }

            return Files.readAllBytes(filePath);
        } catch (IOException e) {
            throw new FileStorageException("read", storageLocation, e);
        }
    }
}