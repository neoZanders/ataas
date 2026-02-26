package com.chalmers.atas.common;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public enum ErrorCode {

    // Alphabetical
    EMAIL_TAKEN("EMAIL_TAKEN", "Email already taken", HttpStatus.BAD_REQUEST),
    INVALID_REFRESH_TOKEN("INVALID_REFRESH_TOKEN", "Invalid refresh token", HttpStatus.BAD_REQUEST),
    REFRESH_TOKEN_EXPIRED("REFRESH_TOKEN_EXPIRED", "Refresh token expired", HttpStatus.BAD_REQUEST),
    REFRESH_TOKEN_REVOKED("REFRESH_TOKEN_REVOKED", "Refresh token revoked", HttpStatus.BAD_REQUEST),
    REFRESH_TOKEN_NOT_FOUND("REFRESH_TOKEN_NOT_FOUND", "Refresh token not found", HttpStatus.NOT_FOUND),

    // Basic HTTP errors
    UNAUTHORIZED("UNAUTHORIZED", "Invalid credentials", HttpStatus.UNAUTHORIZED),
    NOT_FOUND("NOT_FOUND", "Resource not found", HttpStatus.NOT_FOUND),
    FORBIDDEN("FORBIDDEN", "Forbidden", HttpStatus.FORBIDDEN),
    BAD_REQUEST("BAD_REQUEST", "Bad request", HttpStatus.BAD_REQUEST),
    INTERNAL_SERVER_ERROR("INTERNAL_SERVER_ERROR", "Internal Server Error", HttpStatus.INTERNAL_SERVER_ERROR);

    private final String code;
    private final String message;
    private final HttpStatus status;

    ErrorCode(String code, String message, HttpStatus status) {
        this.code = code;
        this.message = message;
        this.status = status;
    }

    public static ErrorCode fromHttpStatus(HttpStatus status) {
        return switch (status) {
            case HttpStatus.FORBIDDEN -> FORBIDDEN;
            case HttpStatus.NOT_FOUND -> NOT_FOUND;
            case HttpStatus.UNAUTHORIZED -> UNAUTHORIZED;
            default -> BAD_REQUEST;
        };
    }

    public Error toError(String details) {
        return new Error(this, details);
    }

    public Error toError() {
        return new Error(this, null);
    }
}
