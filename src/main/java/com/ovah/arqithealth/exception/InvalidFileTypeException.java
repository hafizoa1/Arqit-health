package com.ovah.arqithealth.exception;

public class InvalidFileTypeException extends RuntimeException {
  public InvalidFileTypeException(String message) {
    super(message);
  }

  public InvalidFileTypeException(String fileType, String allowedTypes) {
    super(String.format("Invalid file type: %s. Only %s are allowed.", fileType, allowedTypes));
  }
}
