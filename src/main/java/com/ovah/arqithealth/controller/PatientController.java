package com.ovah.arqithealth.controller;

import com.ovah.arqithealth.model.Patient;
import com.ovah.arqithealth.service.PatientService;
import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@RestController
@RequestMapping("api/v1/ArqitHealth/patients")
@AllArgsConstructor
public class PatientController {

    PatientService patientService;

    @GetMapping
    public ResponseEntity<List<Patient>> getAllPatients() {
        return patientService.getAllPatients();
    }

    @GetMapping("/{patientId}")
    public ResponseEntity<Patient> getPatient(@PathVariable UUID id) {
        return patientService.getPatient(id);
    }


    @PostMapping
    public ResponseEntity<Patient> createPatient(@RequestBody Patient patient) {
        return patientService.createPatient(patient);
    }

}
