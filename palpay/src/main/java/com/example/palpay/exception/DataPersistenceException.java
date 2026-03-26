package com.example.palpay.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Thrown when a DataAccessExcpetion is raised.
 * Mapped to HTTP 503 Service Unavailable for controller responses.
 */
@ResponseStatus(value = HttpStatus.SERVICE_UNAVAILABLE)
public class DataPersistenceException extends RuntimeException {
    public DataPersistenceException(Throwable cause) {
        super(cause);
    }
}
