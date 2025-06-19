package com.ovah.arqithealth.exception;

public class PatientCreationException extends RuntimeException {

    public PatientCreationException(String firstName, String lastName) {
        super("Failed to initiate patient creation for received document for "
        + firstName + " " + lastName);
    }

    public PatientCreationException(String message){
        super(message);
    }




}
