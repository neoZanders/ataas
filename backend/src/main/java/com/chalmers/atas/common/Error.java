package com.chalmers.atas.common;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public class Error {
    private final ErrorCode code;
    private final String details;

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
