package com.yx.note_app.controllers;

import com.yx.note_app.services.service.LogInService;
import com.yx.note_app.services.service.RefreshTokenRequestService;
import com.yx.note_app.services.service.SignUpService;
import com.yx.note_app.services.reponse.ApiResponse;
import com.yx.note_app.services.reponse.ErrorResponse;
import com.yx.note_app.services.reponse.LoginResponse;
import com.yx.note_app.services.request.LoginRequest;
import com.yx.note_app.services.request.RefreshTokenRequest;
import com.yx.note_app.services.request.SignUpRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users")
@Tag(name = "Auth", description = "User registration, login, and token refresh")
public class UserController {

    @Autowired
    private SignUpService signUpService;

    @Autowired
    private LogInService logInService;

    @Autowired
    private RefreshTokenRequestService refreshTokenRequestService;

    @Operation(summary = "Register a new user")
    @ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "201", description = "User created",
            content = @Content(schema = @Schema(implementation = ApiResponse.class))),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Validation error",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "409", description = "Username already exists",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @PostMapping("/signup")
    public ResponseEntity<ApiResponse> createUser(@Valid @RequestBody SignUpRequest signUpRequest) {
        ApiResponse response = signUpService.execute(signUpRequest);
        HttpStatus status = response.getResponseOutcome().getSuccess()
            ? HttpStatus.CREATED
            : response.getResponseOutcome().getHttpStatus();
        return ResponseEntity.status(status).body(response);
    }

    @Operation(summary = "Login and obtain JWT + refresh token")
    @ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Login successful, returns JWT and refresh token",
            content = @Content(schema = @Schema(implementation = LoginResponse.class))),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Invalid credentials",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "429", description = "Rate limit exceeded",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @PostMapping("/login")
    public ResponseEntity<LoginResponse> verifyUser(@Valid @RequestBody LoginRequest loginRequest) {
        LoginResponse response = logInService.execute(loginRequest);
        return ResponseEntity.status(response.getResponseOutcome().getHttpStatus()).body(response);
    }

    @Operation(summary = "Rotate refresh token and get a new JWT")
    @ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "New JWT and refresh token issued",
            content = @Content(schema = @Schema(implementation = LoginResponse.class))),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Refresh token invalid, expired, or revoked",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "429", description = "Rate limit exceeded",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @PostMapping("/refresh")
    public ResponseEntity<LoginResponse> refreshToken(@Valid @RequestBody RefreshTokenRequest refreshTokenRequest) {
        LoginResponse response = refreshTokenRequestService.execute(refreshTokenRequest);
        return ResponseEntity.status(response.getResponseOutcome().getHttpStatus()).body(response);
    }
}
