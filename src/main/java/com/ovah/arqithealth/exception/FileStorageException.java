package com.ovah.arqithealth.exception;

public class FileStorageException extends RuntimeException {
    public FileStorageException(String message) {
        super(message);
    }

    public FileStorageException(String message, Throwable cause) {
        super(message, cause);
    }

    public FileStorageException(String operation, String location, Throwable cause) {
        super(String.format("Failed to %s file at location: %s", operation, location), cause);
    }

    public FileStorageException(String operation, String location) {
        super(String.format("Failed to %s file at location: %s", operation, location));
    }
}
