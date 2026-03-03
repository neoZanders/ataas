package com.chalmers.atas.common;

import lombok.Getter;

import java.util.Map;

@Getter
public class ValidationError extends Error {

    private final Map<String, String> validationErrors;

    public ValidationError(String details, Map<String, String> validationErrors) {
        super(ErrorCode.BAD_REQUEST, details);
        this.validationErrors = validationErrors;
    }
}
