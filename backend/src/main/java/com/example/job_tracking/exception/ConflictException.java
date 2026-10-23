package com.example.job_tracking.exception;

/** Thrown when a request conflicts with existing data (e.g. duplicate email). Mapped to 409. */
public class ConflictException extends RuntimeException {
    public ConflictException(String message) {
        super(message);
    }
}
