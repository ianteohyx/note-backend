package com.yx.note_app.exception;

import com.yx.note_app.enums.ResponseOutcome;

public class InvalidRefreshTokenException extends ApiException {

    public InvalidRefreshTokenException(String message) {
        super(ResponseOutcome.REFRESH_TOKEN_INVALID, message);
    }

    public static InvalidRefreshTokenException expired() {
        return new InvalidRefreshTokenException("Refresh token has expired");
    }

    public static InvalidRefreshTokenException invalid() {
        return new InvalidRefreshTokenException("Refresh token is invalid");
    }

    public static InvalidRefreshTokenException revoked() {
        return new InvalidRefreshTokenException("Refresh token has been revoked");
    }
}
