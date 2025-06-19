package com.ovah.arqithealth.advice;

import com.ovah.arqithealth.exception.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.util.List;


@ControllerAdvice
public class GlobalExceptionHandler {

    @Data
    @AllArgsConstructor
    public static class ErrorResponse {
        private String message;
        private List<String> errors;
    }

    @Data
    @AllArgsConstructor
    public static class SimpleErrorResponse {
        private String message;
        private String error;
    }

    @ExceptionHandler(PatientNotFoundException.class)
    public ResponseEntity<SimpleErrorResponse> handlePatientNotFound(PatientNotFoundException ex) {
        SimpleErrorResponse error = new SimpleErrorResponse(
                "Patient not found",
                ex.getMessage()
        );
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
    }

    @ExceptionHandler(DocumentNotFoundException.class)
    public ResponseEntity<SimpleErrorResponse> handleDocumentNotFound(DocumentNotFoundException ex) {
        SimpleErrorResponse error = new SimpleErrorResponse(
                "Document not found",
                ex.getMessage()
        );
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
    }

    @ExceptionHandler(RecordNotFoundException.class)
    public ResponseEntity<SimpleErrorResponse> handleRecordNotFound(RecordNotFoundException ex) {
        SimpleErrorResponse error = new SimpleErrorResponse(
                "Record not found",
                ex.getMessage()
        );
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
    }

    @ExceptionHandler(RelationshipNotFoundException.class)
    public ResponseEntity<SimpleErrorResponse> handleRelationshipNotFound(RelationshipNotFoundException ex) {
        SimpleErrorResponse error = new SimpleErrorResponse(
                "Relationship not found",
                ex.getMessage()
        );
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
    }

    // Hospital relationship exceptions
    @ExceptionHandler(HospitalNotFoundException.class)
    public ResponseEntity<SimpleErrorResponse> handleHospitalNotFound(HospitalNotFoundException ex) {
        SimpleErrorResponse error = new SimpleErrorResponse(
                "Hospital not found",
                ex.getMessage()
        );
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
    }

    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<SimpleErrorResponse> handleRuntimeException(RuntimeException ex) {
        SimpleErrorResponse error = new SimpleErrorResponse(
                "Runtime error occurred",
                ex.getMessage()
        );
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
    }

    // Catch-all for unexpected exceptions
    @ExceptionHandler(Exception.class)
    public ResponseEntity<SimpleErrorResponse> handleGeneralException(Exception ex) {
        SimpleErrorResponse error = new SimpleErrorResponse(
                "An unexpected error occurred",
                "Internal server error: " + ex.getMessage()
        );
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
    }

    // Security-related exceptions
    @ExceptionHandler(SecurityException.class)
    public ResponseEntity<SimpleErrorResponse> handleSecurityException(SecurityException ex) {
        SimpleErrorResponse error = new SimpleErrorResponse(
                "Security violation",
                ex.getMessage()
        );
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(error);
    }

    @ExceptionHandler(InvalidFileTypeException.class)
    public ResponseEntity<SimpleErrorResponse> handleInvalidFileType(InvalidFileTypeException ex) {
        SimpleErrorResponse error = new SimpleErrorResponse("Invalid file type", ex.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    }

    @ExceptionHandler(FileStorageException.class)
    public ResponseEntity<SimpleErrorResponse> handleFileStorage(FileStorageException ex) {
        SimpleErrorResponse error = new SimpleErrorResponse("File storage error", ex.getMessage());
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
    }

    @ExceptionHandler(TrustRelationshipException.class)
    public ResponseEntity<SimpleErrorResponse> handleTrustRelationship(TrustRelationshipException ex) {
        SimpleErrorResponse error = new SimpleErrorResponse("Trust relationship error", ex.getMessage());
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(error);
    }

    @ExceptionHandler(SharedSecretNotFoundException.class)
    public ResponseEntity<SimpleErrorResponse> handleSharedSecretNotFound(SharedSecretNotFoundException ex) {
        SimpleErrorResponse error = new SimpleErrorResponse("Shared secret not found", ex.getMessage());
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(error);
    }

    @ExceptionHandler(EncryptionException.class)
    public ResponseEntity<SimpleErrorResponse> handleEncryption(EncryptionException ex) {
        SimpleErrorResponse error = new SimpleErrorResponse("Encryption error", ex.getMessage());
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
    }

    @ExceptionHandler(DecryptionException.class)
    public ResponseEntity<SimpleErrorResponse> handleDecryption(DecryptionException ex) {
        SimpleErrorResponse error = new SimpleErrorResponse("Decryption error", ex.getMessage());
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
    }

    @ExceptionHandler(DocumentTransmissionException.class)
    public ResponseEntity<SimpleErrorResponse> handleDocumentTransmission(DocumentTransmissionException ex) {
        SimpleErrorResponse error = new SimpleErrorResponse("Document transmission error", ex.getMessage());
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(error);
    }

    @ExceptionHandler(ExternalServiceException.class)
    public ResponseEntity<SimpleErrorResponse> handleExternalService(ExternalServiceException ex) {
        SimpleErrorResponse error = new SimpleErrorResponse("External service error", ex.getMessage());
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(error);
    }

    @ExceptionHandler(PatientCreationException.class)
    public ResponseEntity<SimpleErrorResponse> handlePatientCreation(PatientCreationException ex) {
        SimpleErrorResponse error = new SimpleErrorResponse("Patient creation error", ex.getMessage());
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
    }

    @ExceptionHandler(RecordSaveException.class)
    public ResponseEntity<SimpleErrorResponse> handleRecordSave(RecordSaveException ex) {
        SimpleErrorResponse error = new SimpleErrorResponse("Record save error", ex.getMessage());
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
    }

}
