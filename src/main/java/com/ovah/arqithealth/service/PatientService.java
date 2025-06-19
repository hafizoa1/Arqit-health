package com.ovah.arqithealth.service;

import com.ovah.arqithealth.exception.PatientNotFoundException;
import com.ovah.arqithealth.model.Patient;
import com.ovah.arqithealth.repository.PatientRepository;
import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@AllArgsConstructor
public class PatientService {

    @Autowired
    private PatientRepository patientRepository;

    public ResponseEntity<List<Patient>> getAllPatients() {
        List<Patient> patients = patientRepository.findAll();
        if (patients.isEmpty()) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.ok(patients);
    }

    @Transactional
    public ResponseEntity<Patient> createPatient(Patient patient) {
        Patient createdPatient = patientRepository.save(patient);
        return ResponseEntity.status(201).body(createdPatient);
    }

    public ResponseEntity<Patient> getPatient(UUID patientId) {
         Patient patient = patientRepository.findById(patientId)
                .orElseThrow(() -> new PatientNotFoundException("Patient with id " + patientId + " not available"));
         return ResponseEntity.ok(patient);
    }
}
