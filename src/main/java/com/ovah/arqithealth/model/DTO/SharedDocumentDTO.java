package com.ovah.arqithealth.model.DTO;

import lombok.*;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.NotBlank;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SharedDocumentDTO {

    @NotNull(message = "Shared Document ID is required")
    private UUID sharedDocumentId;

    @NotNull(message = "Sender Hospital ID is required")
    private UUID senderHospitalId;

    @NotNull(message = "Receiver Hospital ID is required")
    private UUID receiverHospitalId;

    @NotBlank(message = "Encrypted content is required")
    private byte[] encryptedContent;

    @NotBlank(message = "File name is required")
    private String fileName;
}
