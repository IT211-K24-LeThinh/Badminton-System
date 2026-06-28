package com.re.badmintonsystem.exception;

import org.springframework.http.HttpStatus;

public class ResourceNotFoundException extends AppException {
    public ResourceNotFoundException(String resourceName, String fieldName, Object fieldValue) {
        super(HttpStatus.NOT_FOUND,
                String.format("%s not found with %s: '%s'", resourceName, fieldName, fieldValue));
    }

    public ResourceNotFoundException(String message) {
        super(HttpStatus.NOT_FOUND, message);
    }
}
