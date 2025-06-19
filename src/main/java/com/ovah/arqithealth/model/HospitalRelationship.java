package com.ovah.arqithealth.model;

import com.ovah.arqithealth.model.enums.RelationshipStatus;
import com.ovah.arqithealth.model.enums.RelationshipType;
import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "hospital_relationships")
@Data
public class HospitalRelationship {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false)
    private UUID originHospitalId;

    @Column(nullable = false)
    private UUID targetHospitalId;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private RelationshipType relationshipType;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private RelationshipStatus relationshipStatus;

    @Column(nullable = false)
    private LocalDateTime establishedAt = LocalDateTime.now();

    @Column
    private LocalDateTime expiresAt;

    @Column(nullable = false)
    private String sharedSecretReference;
}
