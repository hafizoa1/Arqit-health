package com.ovah.arqithealth.model.DTO;

import lombok.Data;

import java.util.UUID;

@Data
public class RelationshipAcceptanceDTO {

    private UUID initiatingHospitalId;

    private UUID acceptingHospitalId;
}
