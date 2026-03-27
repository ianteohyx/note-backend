package com.yx.note_app.enums;

import org.springframework.http.HttpStatus;

public enum ResponseOutcome {
    SUCCESS(true, "SUCCESS", "success", HttpStatus.OK),
    PROCESS_FAIL(false, "PROCESS_FAIL", "process fail", HttpStatus.INTERNAL_SERVER_ERROR),
    PARAM_ILLEGAL(false, "PARAM_ILLEGAL", "parameter in request causing error", HttpStatus.BAD_REQUEST),
    USERNAME_EXIST(false, "USERNAME_EXIST", "this username has been taken", HttpStatus.CONFLICT),
    USER_NOT_EXIST(false, "USER_NOT_EXIST", "this user does not exist", HttpStatus.NOT_FOUND),
    PASSWORD_INVALID(false, "INVALID_PASSWORD", "password format invalid", HttpStatus.BAD_REQUEST),
    LOGIN_FAIL(false, "LOGIN_FAIL", "login fail", HttpStatus.UNAUTHORIZED),
    TOKEN_INVALID(false, "TOKEN_INVALID", "token is invalid", HttpStatus.UNAUTHORIZED),
    NOTE_NOT_EXIST(false, "NOTE_NOT_EXIST", "note does not exist", HttpStatus.NOT_FOUND),
    NOTE_NOT_SHARED(false, "NOTE_NOT_SHARED", "note is note shared to this user", HttpStatus.FORBIDDEN),
    ACTION_NOT_ALLOWED(false, "ACTION_NOT_ALLOWED", "this action is not allowed", HttpStatus.FORBIDDEN),
    NOTE_ALREADY_SHARED(false, "NOTE_ALREADY_SHARED", "this note already shared to the same user", HttpStatus.CONFLICT),
    VALIDATION_ERROR(false, "VALIDATION_ERROR", "request validation failed", HttpStatus.BAD_REQUEST),
    RATE_LIMIT_EXCEEDED(false, "RATE_LIMIT_EXCEEDED", "too many requests, please try again later", HttpStatus.TOO_MANY_REQUESTS),
    REFRESH_TOKEN_INVALID(false, "REFRESH_TOKEN_INVALID", "refresh token is invalid or expired", HttpStatus.UNAUTHORIZED);

    private final boolean success;
    private final String code;
    private final String desc;
    private final HttpStatus httpStatus;

    ResponseOutcome(boolean success, String code, String desc, HttpStatus httpStatus) {
        this.success = success;
        this.code = code;
        this.desc = desc;
        this.httpStatus = httpStatus;
    }

    public String getCode() {
        return code;
    }

    public String getDesc() {
        return desc;
    }

    public boolean getSuccess(){return success;}

    public HttpStatus getHttpStatus(){return httpStatus;}
}
