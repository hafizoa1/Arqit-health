package com.ovah.arqithealth.exception;

public class PatientNotFoundException extends RuntimeException {
    public PatientNotFoundException(Long id) {
        super("Patient not found with id: " + id);
    }

    public PatientNotFoundException(String message) {
        super(message);
    } // add in more exceptions and advice
}
