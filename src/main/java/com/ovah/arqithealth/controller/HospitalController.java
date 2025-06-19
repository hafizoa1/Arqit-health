package com.ovah.arqithealth.controller;

import com.ovah.arqithealth.model.Hospital;
import com.ovah.arqithealth.service.HospitalService;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("api/v1/ArqitHealth/hospitals")
@AllArgsConstructor
public class HospitalController {

    HospitalService hospitalService;


    @GetMapping
    public ResponseEntity<List<Hospital>> getAllHospitals() {
        return hospitalService.getAllHospitals();
    }

    @PostMapping
    public ResponseEntity<Hospital> createHospital(@RequestBody Hospital hospital) {
        return hospitalService.createHospital(hospital);
    }

    @GetMapping("/{hospitalId}")
    public ResponseEntity<Hospital> getHospitalById(@PathVariable UUID hospitalId) {
        return hospitalService.getHospital(hospitalId);
    }
}
