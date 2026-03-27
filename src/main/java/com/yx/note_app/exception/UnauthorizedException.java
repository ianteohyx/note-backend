package com.yx.note_app.exception;

import com.yx.note_app.enums.ResponseOutcome;

public class UnauthorizedException extends ApiException {

    public UnauthorizedException(String message) {
        super(ResponseOutcome.ACTION_NOT_ALLOWED, message);
    }

    public static UnauthorizedException notOwner(String username) {
        return new UnauthorizedException("User " + username + " is not authorized to perform this action");
    }

    public static UnauthorizedException noEditPermission(String username) {
        return new UnauthorizedException("User " + username + " does not have edit permission");
    }
}