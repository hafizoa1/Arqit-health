package com.ovah.arqithealth.repository;

import com.ovah.arqithealth.model.PatientRecord;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface RecordRepository extends JpaRepository<PatientRecord, UUID> {

    List<PatientRecord> findPatientRecordsByPatientId(UUID patientId);

}
