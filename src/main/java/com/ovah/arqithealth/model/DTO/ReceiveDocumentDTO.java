package com.ovah.arqithealth.model.DTO;

import lombok.Data;

@Data
public class ReceiveDocumentDTO {

    private SharedDocumentDTO documentDTO;

    private PatientDTO sharedPatient;

    private String description;
}
