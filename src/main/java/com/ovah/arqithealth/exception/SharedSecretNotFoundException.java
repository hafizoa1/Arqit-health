package com.ovah.arqithealth.exception;

public class SharedSecretNotFoundException extends RuntimeException {
    public SharedSecretNotFoundException(String sourceHospital, String targetHospital) {
        super(String.format("No shared secret found for trust relationship between %s and %s", sourceHospital, targetHospital));
    }
}
