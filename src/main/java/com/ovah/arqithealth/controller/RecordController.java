package com.ovah.arqithealth.controller;


import com.ovah.arqithealth.model.PatientRecord;
import com.ovah.arqithealth.service.RecordService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("api/v1/ArqitHealth/records")
@AllArgsConstructor
@Slf4j
public class RecordController {

    private final RecordService recordService;

    @PostMapping("/patient/{patientId}/upload")
    public ResponseEntity<PatientRecord> uploadPatientRecord(
            @RequestParam("file") MultipartFile file,
            @PathVariable UUID patientId,
            @RequestParam(value = "description", required = false) String description) {

        log.info("Uploading record for patient: {}", patientId);
        return recordService.uploadPatientRecord(patientId, file, description);
    }

    @GetMapping("/patient/{patientId}")
    public ResponseEntity<List<PatientRecord>> getPatientRecords(
            @PathVariable UUID patientId) {

        log.info("Fetching records for patient: {}", patientId);
        return recordService.getPatientRecords(patientId);
    }
}
