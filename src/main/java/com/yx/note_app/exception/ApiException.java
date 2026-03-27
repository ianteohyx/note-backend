package com.yx.note_app.exception;

import com.yx.note_app.enums.ResponseOutcome;

public abstract class ApiException extends RuntimeException {
    private final ResponseOutcome responseOutcome;

    public ApiException(ResponseOutcome responseOutcome, String message) {
        super(message);
        this.responseOutcome = responseOutcome;
    }

    public ResponseOutcome getResponseOutcome() {
        return responseOutcome;
    }
}