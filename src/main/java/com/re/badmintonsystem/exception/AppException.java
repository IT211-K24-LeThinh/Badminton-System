package com.re.badmintonsystem.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

public class AppException extends RuntimeException {
    @Getter
    private final HttpStatus status;
    private final String message;

    public AppException(HttpStatus status, String message) {
        super(message);
        this.status = status;
        this.message = message;
    }

    public AppException(HttpStatus status, String message, Throwable cause) {
        super(message, cause);
        this.status = status;
        this.message = message;
    }

    @Override
    public String getMessage() {
        return message;
    }
}
