package com.yx.note_app.services.reponse;

import com.yx.note_app.enums.ResponseOutcome;
import io.swagger.v3.oas.annotations.media.Schema;

import java.util.Map;

public class ErrorResponse extends ApiResponse {
    @Schema(description = "Human-readable error message", example = "Username already exists")
    private String message;

    @Schema(description = "Per-field validation errors (only present on 400 validation errors)", example = "{\"username\": \"Username is required\"}")
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
