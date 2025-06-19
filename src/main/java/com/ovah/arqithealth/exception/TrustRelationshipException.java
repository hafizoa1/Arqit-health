package com.ovah.arqithealth.exception;

import java.util.UUID;

public class TrustRelationshipException extends RuntimeException {

    public TrustRelationshipException(UUID sourceHospital, UUID targetHospital) {
        super(String.format("No trust relationship exists between hospital %s and %s", sourceHospital, targetHospital));
    }
}
