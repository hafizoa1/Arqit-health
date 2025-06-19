package com.ovah.arqithealth.controller;

import com.ovah.arqithealth.model.DTO.PatientDTO;
import com.ovah.arqithealth.model.DTO.ReceiveDocumentDTO;
import com.ovah.arqithealth.model.DTO.SharedDocumentDTO;
import com.ovah.arqithealth.model.SharedDocument;
import com.ovah.arqithealth.service.DocumentSharingService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.UUID;

@RestController
@AllArgsConstructor
@RequestMapping("api/v1/ArqitHealth/documents")
@Slf4j
public class DocumentController {

    @Autowired
    private DocumentSharingService documentSharingService;

    /**
     * Share a document with another hospital
     */
    @PostMapping("/share/{recordId}")
    public ResponseEntity<String> shareDocument(
            @PathVariable("recordId") UUID recordId,
            @RequestParam("targetHospitalId") UUID targetHospitalId) {

        try {
            return documentSharingService.shareDocument(recordId, targetHospitalId);
        } catch (Exception e) {
            log.error("Failed to share document: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Failed to share document: " + e.getMessage());
        }
    }

    /**
     * Receive a document from another hospital (auto-accept if trust exists)
     */
    @PostMapping("/receive-document")
    public ResponseEntity<String> receiveDocument(@RequestBody ReceiveDocumentDTO requestPayload) {
        try {
            return documentSharingService.receiveDocument(requestPayload);
        } catch (SecurityException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(e.getMessage());
        } catch (Exception e) {
            log.error("Failed to receive document: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Failed to receive document: " + e.getMessage());
        }
    }

    /**
     * Get a specific shared document by ID
     */
    @GetMapping("/{sharedDocumentId}")
    public ResponseEntity<SharedDocument> getSharedDocument(@PathVariable UUID sharedDocumentId) {
        return documentSharingService.getSharedDocument(sharedDocumentId);
    }
}

