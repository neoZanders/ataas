package com.chalmers.atas.common;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum ErrorCode {

    // Alphabetical
    CANNOT_DELETE_COURSE_OWNER("CANNOT_DELETE_COURSE_OWNER", "Course owner cannot be deleted", HttpStatus.BAD_REQUEST),
    CHARACTER_LIMIT_EXCEEDED("CHACTACTER_LIMIT_EXCEEDED", "Character limit exceeded", HttpStatus.BAD_REQUEST),
    COURSE_INVITE_NOT_FOUND("COURSE_INVITE_NOT_FOUND", "Course invite not found", HttpStatus.NOT_FOUND),
    COURSE_NOT_FOUND("COURSE_NOT_FOUND", "Course not found", HttpStatus.NOT_FOUND),
    COURSE_SESSION_NOT_FOUND("COURSE_SESSION_NOT_FOUND", "Course session not found", HttpStatus.NOT_FOUND),
    DUPLICATE_SESSION_ID("DUPLICATE_SESSION_ID", "Duplicate session ID in request", HttpStatus.BAD_REQUEST),
    DUPLICATE_SESSION_TYPE_PREFERENCES("DUPLICATE_SESSION_TYPE_PREFERENCES", "Session type preferences must be unique", HttpStatus.BAD_REQUEST),
    DUPLICATE_TA_ASSIGNMENT_ID("DUPLICATE_TA_ASSIGNMENT_ID", "Duplicate TA assignment ID in request", HttpStatus.BAD_REQUEST),
    EMAIL_TAKEN("EMAIL_TAKEN", "Email already taken", HttpStatus.BAD_REQUEST),
    INVALID_COURSE_ASSIGNMENT_STATUS("INVALID_COURSE_ASSIGNMENT_STATUS", "Invalid course assignment status", HttpStatus.BAD_REQUEST),
    INVALID_COURSE_CODE("INVALID_COURSE_CODE", "Course code format invalid", HttpStatus.BAD_REQUEST),
    INVALID_EMAIL_FORMAT("INVALID_EMAIL_FORMAT","Email format invalid",HttpStatus.BAD_REQUEST),
    INVALID_HARD_CONSTRAINT_TA_REFERENCE("INVALID_HARD_CONSTRAINT_TA_REFERENCE", "Hard constraint references unknown TA assignment", HttpStatus.BAD_REQUEST),
    INVALID_MIN_MAX_HOURS("INVALID_MIN_MAX_HOURS", "Maximum hours cannot be less than minimum hours", HttpStatus.BAD_REQUEST),
    INVALID_MIN_MAX_TA("INVALID_MIN_MAX_TA", "Maximum TAs cannot be less than minimum TAs",HttpStatus.BAD_REQUEST),
    INVALID_PASSWORD_FORMAT("INVALID_PASSWORD_FORMAT", "Password format invalid",HttpStatus.BAD_REQUEST),
    INVALID_REFRESH_TOKEN("INVALID_REFRESH_TOKEN", "Invalid refresh token", HttpStatus.BAD_REQUEST),
    INVALID_SESSION_TA_RANGE("INVALID_SESSION_TA_RANGE", "Session minTAs cannot exceed maxTAs", HttpStatus.BAD_REQUEST),
    INVALID_SOFT_CONSTRAINT_TA_REFERENCE("INVALID_SOFT_CONSTRAINT_TA_REFERENCE", "Soft constraint references unknown TA assignment", HttpStatus.BAD_REQUEST),
    INVALID_TA_PREFERENCES("INVALID_TA_PREFERENCES", "TA does not have ranking for all session types", HttpStatus.BAD_REQUEST),
    REFRESH_TOKEN_EXPIRED("REFRESH_TOKEN_EXPIRED", "Refresh token expired", HttpStatus.BAD_REQUEST),
    REFRESH_TOKEN_NOT_FOUND("REFRESH_TOKEN_NOT_FOUND", "Refresh token not found", HttpStatus.NOT_FOUND),
    REFRESH_TOKEN_REVOKED("REFRESH_TOKEN_REVOKED", "Refresh token revoked", HttpStatus.BAD_REQUEST),
    REQUEST_TIMED_OUT("REQUEST_TIMED_OUT", "External API request timed out", HttpStatus.BAD_GATEWAY),
    SCHEDULE_GENERATION_TIMED_OUT("SCHEDULE_GENERATION_TIMED_OUT", "Schedule generation timed out before a feasible schedule could be found", HttpStatus.BAD_REQUEST),
    SCHEDULE_INFEASIBLE("SCHEDULE_INFEASIBLE", "Schedule for course under current constraints infeasible", HttpStatus.BAD_REQUEST),
    SCHEDULE_NOT_FOUND("SCHEDULE_NOT_FOUND", "Schedule not found", HttpStatus.NOT_FOUND),
    START_AFTER_END("START_AFTER_END", "Start date time is after end date time", HttpStatus.BAD_REQUEST),
    TA_CONSTRAINT_NOT_FOUND("TA_CONSTRAINT_NOT_FOUND", "TA constraint not found", HttpStatus.NOT_FOUND),
    TIMEEDIT_AUTHENTICATION_FAILED("TIMEEDIT_AUTHENTICATION_FAILED", "Failed to authenticate with TimeEdit", HttpStatus.INTERNAL_SERVER_ERROR),
    TIMEEDIT_JSON_PARSE_ERROR("TIMEEDIT_JSON_PARSE_ERROR", "Failed to parse TimeEdit response", HttpStatus.INTERNAL_SERVER_ERROR),
    USER_ALREADY_HAS_COURSE_ASSIGNMENT("USER_ALREADY_HAS_COURSE_ASSIGNMENT", "User already has an assignment to this course", HttpStatus.BAD_REQUEST),
    USER_HAS_NOT_JOINED_COURSE("USER_HAS_NOT_JOINED_COURSE", "User has not joined course", HttpStatus.BAD_REQUEST),
    USER_NOT_ALLOWED_FOR_COURSE_ACTION("USER_NOT_ALLOWED_FOR_COURSE_ACTION", "User is not allowed to perform this course action", HttpStatus.FORBIDDEN),
    USER_NOT_ALLOWED_TO_UPDATE_ASSIGNMENT("USER_NOT_ALLOWED_TO_UPDATE_ASSIGNMENT", "User is not allowed to update this assignment", HttpStatus.FORBIDDEN),
    USER_NOT_ALLOWED_TO_VIEW_COURSE("USER_NOT_ALLOWED_TO_VIEW_COURSE", "User is not allowed to view this course", HttpStatus.FORBIDDEN),
    USER_NOT_COURSE_RESPONSIBLE("USER_NOT_COURSE_RESPONSIBLE", "User is not course responsible for this course", HttpStatus.FORBIDDEN),
    USER_NOT_FOUND("USER_NOT_FOUND", "User not found", HttpStatus.NOT_FOUND),
    USER_NOT_TEACHING_ASSISTANT("USER_NOT_TEACHING_ASSISTANT", "User is not teaching assistant of this course", HttpStatus.FORBIDDEN),
    
    // Basic HTTP errors
    UNAUTHORIZED("UNAUTHORIZED", "Invalid credentials", HttpStatus.UNAUTHORIZED),
    NOT_FOUND("NOT_FOUND", "Resource not found", HttpStatus.NOT_FOUND),
    FORBIDDEN("FORBIDDEN", "Forbidden", HttpStatus.FORBIDDEN),
    BAD_REQUEST("BAD_REQUEST", "Bad request", HttpStatus.BAD_REQUEST),
    INTERNAL_SERVER_ERROR("INTERNAL_SERVER_ERROR", "Internal Server Error", HttpStatus.INTERNAL_SERVER_ERROR);

    private final String code;
    private final String message;
    private final HttpStatus status;

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
