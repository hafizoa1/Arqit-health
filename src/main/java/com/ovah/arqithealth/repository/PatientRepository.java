package com.ovah.arqithealth.repository;

import com.ovah.arqithealth.model.Patient;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface PatientRepository extends JpaRepository<Patient, UUID> {

    Patient getPatientByPatientId(UUID patientId);
}
