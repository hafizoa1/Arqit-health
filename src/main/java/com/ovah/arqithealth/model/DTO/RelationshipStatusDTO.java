package com.ovah.arqithealth.model.DTO;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;


import java.util.UUID;

import com.ovah.arqithealth.model.enums.RelationshipStatus;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class RelationshipStatusDTO {

    @NotNull(message = "Initiating Hospital ID is required")
    private UUID initiatingHospitalId;

    @NotNull(message = "Accepting Hospital ID is required")
    private UUID acceptingHospitalId;

    @NotNull(message = "New Status is required")
    private RelationshipStatus newStatus;
}
