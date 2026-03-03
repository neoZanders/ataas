package com.chalmers.atas.config.advice;

import com.chalmers.atas.common.Error;
import com.chalmers.atas.common.ErrorCode;
import com.chalmers.atas.common.HttpResponse;
import com.chalmers.atas.common.ValidationError;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import org.springframework.security.authentication.AuthenticationCredentialsNotFoundException;
import org.springframework.security.authentication.InsufficientAuthenticationException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.stream.Collectors;

@RestControllerAdvice
public class GlobalExceptionAdvice {

    @ExceptionHandler({
            AuthenticationCredentialsNotFoundException.class,
            InsufficientAuthenticationException.class,
            UsernameNotFoundException.class
    })
    public HttpResponse<Error> handleAuthenticationProblems(AuthenticationException ex) {
        return HttpResponse.ofError(
                ErrorCode.UNAUTHORIZED.toError(ex.getMessage())
        );
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public HttpResponse<ValidationError> handleConstraintViolation(ConstraintViolationException ex) {
        Map<String, String> violations = ex.getConstraintViolations().stream()
                .collect(Collectors.toMap(
                        constraintViolation ->
                                constraintViolation.getPropertyPath().toString(),
                        ConstraintViolation::getMessage,
                        (a, b) -> a,
                        LinkedHashMap::new
                ));
        ValidationError error = new ValidationError(
                "Validation failed",
                violations
        );

        return HttpResponse.ofError(error);
    }
}
