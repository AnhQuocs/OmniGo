package com.trung.userdriverservice.exception;

import org.springframework.http.HttpStatus;

/** Runtime exception ensures image failures roll back the surrounding DB transaction. */
public class ImageUploadException extends RuntimeException {
    private final HttpStatus status;

    public ImageUploadException(String message, HttpStatus status) {
        super(message);
        this.status = status;
    }

    public HttpStatus getStatus() { return status; }
}