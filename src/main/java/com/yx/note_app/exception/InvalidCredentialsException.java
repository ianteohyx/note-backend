package com.yx.note_app.exception;

import com.yx.note_app.enums.ResponseOutcome;

public class InvalidCredentialsException extends ApiException {

    public InvalidCredentialsException(String message) {
        super(ResponseOutcome.LOGIN_FAIL, message);
    }

    public static InvalidCredentialsException loginFailed() {
        return new InvalidCredentialsException("Invalid username or password");
    }
}