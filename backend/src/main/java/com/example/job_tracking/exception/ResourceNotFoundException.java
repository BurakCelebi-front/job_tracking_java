package com.example.job_tracking.exception;

/** Thrown when a requested resource does not exist or does not belong to the current user. Mapped to 404. */
public class ResourceNotFoundException extends RuntimeException {
    public ResourceNotFoundException(String message) {
        super(message);
    }
}
