package com.re.badmintonsystem.exception;

import org.springframework.http.HttpStatus;

public class ConflictException extends AppException {
    public ConflictException(String message) {
        super(HttpStatus.CONFLICT, message);
    }

    public ConflictException(String resourceName, String fieldName, Object fieldValue) {
        super(HttpStatus.CONFLICT,
                String.format("%s already exists with %s: '%s'", resourceName, fieldName, fieldValue));
    }
}
