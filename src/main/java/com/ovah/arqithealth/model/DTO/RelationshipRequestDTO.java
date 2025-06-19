package com.ovah.arqithealth.model.DTO;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.util.UUID;
import com.ovah.arqithealth.model.enums.RelationshipType;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RelationshipRequestDTO {

    @NotNull(message = "Origin Hospital ID is required")
    private UUID originHospitalId;

    @NotNull(message = "Target Hospital ID is required")
    private UUID targetHospitalId;

    @NotNull(message = "Relationship Type is required")
    @Enumerated(EnumType.STRING)
    private RelationshipType relationshipType;
}