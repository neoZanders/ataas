package com.chalmers.atas.common;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public class Error {
    private final ErrorCode code;
    private final String details;

    public Error(ErrorCode code, String details) {
        this.code = code;
        this.details = details;
    }

    public HttpStatus getStatus() {
        return code.getStatus();
    }

    public String getMessage() {
        return code.getMessage();
    }

    public String getCode() {
        return code.getCode();
    }
}
