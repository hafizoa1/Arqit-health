package com.ovah.arqithealth.model;

import com.ovah.arqithealth.model.enums.DocumentStatus;
import jakarta.persistence.*;
import lombok.Data;


import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Data
@Table(name = "shared_documents")
public class SharedDocument {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false)
    private UUID sourceHospitalId;

    @Column(nullable = false)
    private UUID targetHospitalId;

    @Column(nullable = false, columnDefinition = "bytea")
    private byte[] encryptedData;

    @Column(nullable = false)
    private LocalDateTime sharedAt = LocalDateTime.now();

    @Column(nullable = false)
    private String fileName;

    @Column
    private DocumentStatus status;

    @Column (nullable = false)
    private UUID patientId;
}
