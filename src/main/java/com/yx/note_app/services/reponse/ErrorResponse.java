package com.yx.note_app.services.reponse;

import com.yx.note_app.enums.ResponseOutcome;

import java.util.Map;

public class ErrorResponse extends ApiResponse {
    private String message;
    private Map<String, String> fieldErrors;

    public ErrorResponse(ResponseOutcome responseOutcome, String message) {
        this.setResponseOutcome(responseOutcome);
        this.message = message;
    }

    public ErrorResponse(ResponseOutcome responseOutcome, String message, Map<String, String> fieldErrors) {
        this.setResponseOutcome(responseOutcome);
        this.message = message;
        this.fieldErrors = fieldErrors;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public Map<String, String> getFieldErrors() {
        return fieldErrors;
    }

    public void setFieldErrors(Map<String, String> fieldErrors) {
        this.fieldErrors = fieldErrors;
    }
}
