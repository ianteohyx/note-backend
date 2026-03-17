package com.yx.note_app.exception;

import com.yx.note_app.enums.ResponseOutcome;
import com.yx.note_app.services.reponse.ApiResponse;
import com.yx.note_app.services.reponse.ErrorResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger logger = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(InvalidCredentialsException.class)
    public ResponseEntity<ApiResponse> handleInvalidCredentialsException(InvalidCredentialsException ex) {
        logger.warn("Invalid credentials attempt");

        ErrorResponse response = new ErrorResponse(ex.getResponseOutcome(), ex.getMessage());
        return ResponseEntity.status(ex.getResponseOutcome().getHttpStatus()).body(response);
    }

    @ExceptionHandler(ApiException.class)
    public ResponseEntity<ApiResponse> handleApiException(ApiException ex) {
        logger.warn("API exception: {} - {}", ex.getResponseOutcome().getCode(), ex.getMessage());

        ErrorResponse response = new ErrorResponse(ex.getResponseOutcome(), ex.getMessage());
        return ResponseEntity.status(ex.getResponseOutcome().getHttpStatus()).body(response);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse> handleValidationExceptions(MethodArgumentNotValidException ex) {
        Map<String, String> fieldErrors = new HashMap<>();
        for (FieldError error : ex.getBindingResult().getFieldErrors()) {
            fieldErrors.put(error.getField(), error.getDefaultMessage());
        }

        logger.warn("Validation failed: {}", fieldErrors);

        ErrorResponse response = new ErrorResponse(
                ResponseOutcome.VALIDATION_ERROR,
                "Validation failed",
                fieldErrors
        );
        return ResponseEntity.badRequest().body(response);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse> handleGenericException(Exception ex) {
        logger.error("Unexpected error occurred", ex);

        ErrorResponse response = new ErrorResponse(
                ResponseOutcome.PROCESS_FAIL,
                "An unexpected error occurred"
        );
        return ResponseEntity.internalServerError().body(response);
    }
}