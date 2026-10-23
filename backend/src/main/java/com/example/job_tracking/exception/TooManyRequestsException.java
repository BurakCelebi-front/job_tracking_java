package com.example.job_tracking.exception;

/** Thrown when a client exceeds a rate limit (e.g. too many failed logins). Mapped to 429. */
public class TooManyRequestsException extends RuntimeException {
    public TooManyRequestsException(String message) {
        super(message);
    }
}
