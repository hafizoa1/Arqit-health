package com.ovah.arqithealth.repository;

import com.ovah.arqithealth.model.HospitalRelationship;
import com.ovah.arqithealth.model.enums.RelationshipStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface RelationshipRepository extends JpaRepository<HospitalRelationship, UUID> {

    boolean existsByOriginHospitalIdAndTargetHospitalIdAndRelationshipStatus(UUID hospitalAId, UUID hospitalBId, RelationshipStatus relationshipStatus);

    boolean existsByOriginHospitalIdAndTargetHospitalIdAndRelationshipStatusIn(UUID originHospitalId, UUID targetHospitalId, List<RelationshipStatus> pending);

    Optional<HospitalRelationship> findByOriginHospitalIdAndTargetHospitalIdAndRelationshipStatus(UUID initiatingHospitalId, UUID acceptingHospitalId, RelationshipStatus relationshipStatus);

    Optional<HospitalRelationship> findByOriginHospitalIdAndTargetHospitalId(UUID acceptingHospitalId, UUID initiatingHospitalId);

    @Query(value = "SELECT hr.shared_secret_reference  FROM hospital_relationships hr " +
            "WHERE (hr.origin_hospital_id = :hospitalA AND hr.target_hospital_id = :hospitalB " +
            "OR hr.origin_hospital_id  = :hospitalB AND hr.target_hospital_id = :hospitalA) " +
            "AND hr.relationship_status = 'ACTIVE' " +
            "AND hr.shared_secret_reference IS NOT NULL LIMIT 1", nativeQuery = true)
    Optional<String> findSharedSecretByHospitals(
            @Param("hospitalA") UUID hospitalA,
            @Param("hospitalB") UUID hospitalB);

}
