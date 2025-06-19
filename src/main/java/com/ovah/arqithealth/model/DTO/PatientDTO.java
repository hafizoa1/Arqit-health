package com.ovah.arqithealth.model.DTO;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class PatientDTO {

    private String firstName;

    private String lastName;

    private Integer age;
}
