package com.ovah.arqithealth.service;

import com.ovah.arqithealth.exception.HospitalNotFoundException;
import com.ovah.arqithealth.model.Hospital;
import com.ovah.arqithealth.repository.HospitalRepository;
import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@AllArgsConstructor
public class HospitalService {

    @Autowired
    private HospitalRepository hospitalRepository;

    public ResponseEntity<List<Hospital>> getAllHospitals() {
        List<Hospital> patients = hospitalRepository.findAll();
        if (patients.isEmpty()) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.ok(patients);
    }

    public ResponseEntity<Hospital> getHospital(UUID id) {
        Hospital hospital = hospitalRepository.findById(id)
                .orElseThrow(() -> new HospitalNotFoundException(id.toString()));

        return ResponseEntity.ok(hospital);
    }

    public ResponseEntity<Hospital> createHospital(Hospital hospital) {
        Hospital createdHospital = hospitalRepository.save(hospital);
        return ResponseEntity.status(201).body(createdHospital);
    }
}