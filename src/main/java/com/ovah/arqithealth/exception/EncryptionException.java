package com.ovah.arqithealth.exception;

public class EncryptionException extends RuntimeException {

    public EncryptionException(String message) {
        super(message);
    }

    public EncryptionException(String encrypt, Exception e) {
        //super(encrypt, e);
    }
}
