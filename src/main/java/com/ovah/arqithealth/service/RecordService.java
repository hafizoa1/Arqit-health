package com.ovah.arqithealth.service;

import com.ovah.arqithealth.exception.FileStorageException;
import com.ovah.arqithealth.exception.InvalidFileTypeException;
import com.ovah.arqithealth.exception.PatientNotFoundException;
import com.ovah.arqithealth.model.Patient;
import com.ovah.arqithealth.model.PatientRecord;
import com.ovah.arqithealth.repository.PatientRepository;
import com.ovah.arqithealth.repository.RecordRepository;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class RecordService {

    private final RecordRepository recordRepository;
    private final PatientRepository patientRepository;

    @Value("${app.file-storage.location}")
    private String fileStorageLocation;

    @PostConstruct
    public void init() {
        try {
            Files.createDirectories(Paths.get(fileStorageLocation));
        } catch (IOException e) {
            throw new RuntimeException("Could not create file upload directory at " + fileStorageLocation, e);
        }
    }

    //create a patient record before saving it

    public ResponseEntity<PatientRecord> uploadPatientRecord(UUID patientId, MultipartFile patientFile, String description) {

        if (!patientRepository.existsById(patientId)) {
            throw new PatientNotFoundException(String.valueOf(patientId));
        }

        // Fetch patient details
        Patient patient = patientRepository.findById(patientId)
                .orElseThrow(() -> new PatientNotFoundException(String.valueOf(patientId)));

        // Build folder name: firstName_lastName_shortId
        String shortId = patientId.toString().substring(0, 5);
        String patientFolderName = patient.getFirstName() + "_" + patient.getLastName() + "_" + shortId;

        // Create per-patient folder path
        Path patientFolderPath = Paths.get(fileStorageLocation).resolve(patientFolderName);

        try {
            Files.createDirectories(patientFolderPath); // Creates only if it doesn't exist
        } catch (IOException e) {
            throw new FileStorageException("create folder for patient: " + patientFolderName, e);
        }

        // Extract original filename and extension
        String originalFilename = patientFile.getOriginalFilename();
        String fileExtension = originalFilename.substring(originalFilename.lastIndexOf(".")).toLowerCase();

        // Validate allowed file types
        if (!fileExtension.equals(".pdf") && !fileExtension.equals(".docx")) {
            throw new InvalidFileTypeException(fileExtension, "PDF and DOCX");
        }

        // Generate sanitized file name and target location
        String newFilename = generateSanitisedFileName(patientId) + fileExtension;
        Path targetLocation = patientFolderPath.resolve(newFilename);

        // Copy the file
        try {
            Files.copy(patientFile.getInputStream(), targetLocation);
        } catch (IOException e) {
            throw new FileStorageException("copy file", targetLocation.toString(), e);
        }

        // Save file metadata to DB
        PatientRecord patientRecord = new PatientRecord();
        patientRecord.setPatientId(patientId);
        patientRecord.setContentType(patientFile.getContentType());
        patientRecord.setStorageLocation(targetLocation.toString());
        patientRecord.setDescription(description);
        patientRecord.setFilename(originalFilename);

        PatientRecord savedRecord = recordRepository.save(patientRecord);

        return ResponseEntity.ok(savedRecord);
    }

    @Transactional
    public void saveDecryptedPatientRecord(UUID patientId, byte[] fileData, String originalFilename, String description) {

        // Fetch patient details
        Patient patient = patientRepository.findById(patientId)
                .orElseThrow(() -> new PatientNotFoundException(String.valueOf(patientId)));

        // Build folder name: firstName_lastName_shortId
        String shortId = patientId.toString().substring(0, 5);
        String patientFolderName = patient.getFirstName() + "_" + patient.getLastName() + "_" + shortId;

        // Create per-patient folder path
        Path patientFolderPath = Paths.get(fileStorageLocation).resolve(patientFolderName);
        try {
            Files.createDirectories(patientFolderPath); // Creates only if it doesn't exist
        } catch (IOException e) {
            throw new FileStorageException("create folder for patient: " + patientFolderName, e);
        }

        // Extract file extension
        //String originalFilename = patientFile.getOriginalFilename();
        String fileExtension = originalFilename.substring(originalFilename.lastIndexOf(".")).toLowerCase();

        // Validate allowed file types
        if (!fileExtension.equals(".pdf") && !fileExtension.equals(".docx")) {
            throw new InvalidFileTypeException(fileExtension, "PDF and DOCX");
        }

        String contentType = originalFilename.endsWith(".pdf")
                ? "application/pdf" : "application/vnd.openxmlformats-officedocument.wordprocessingml.document";

        // Generate sanitized file name and target location
        String newFilename = generateSanitisedFileName(patientId) + fileExtension;
        Path targetLocation = patientFolderPath.resolve(newFilename);

        // Write the byte array to file
        try {
            Files.write(targetLocation, fileData);
        } catch (IOException e) {
            throw new FileStorageException("copy file", targetLocation.toString(), e);
        }

        // Save file metadata to DB
        PatientRecord patientRecord = new PatientRecord();
        patientRecord.setPatientId(patientId);
        patientRecord.setContentType(contentType);
        patientRecord.setStorageLocation(targetLocation.toString());
        patientRecord.setDescription(description);
        patientRecord.setFilename(originalFilename);

        PatientRecord savedRecord = recordRepository.save(patientRecord);
        ResponseEntity.ok(savedRecord);
    }


    public ResponseEntity<List<PatientRecord>> getPatientRecords (UUID patientId) {
       List<PatientRecord> record = recordRepository.findPatientRecordsByPatientId(patientId);

       if (record.isEmpty()) {
           return ResponseEntity.notFound().build();
       } else {
           return ResponseEntity.ok(record);
       }
   }

   private String generateSanitisedFileName(UUID patientID) {

        Patient patient = patientRepository.findById(patientID).orElseThrow(()
                -> new PatientNotFoundException(String.valueOf(patientID)));

        String sanitisedName = patient.getFirstName() + "_" + patient.getLastName()  + "_" + System.currentTimeMillis();;

        return sanitisedName;
   }

}
