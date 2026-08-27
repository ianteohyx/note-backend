package com.yx.note_app.controllers;

import com.yx.note_app.exception.InvalidRefreshTokenException;
import com.yx.note_app.security.RefreshTokenCookieFactory;
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
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;
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

    @Autowired
    private RefreshTokenCookieFactory refreshTokenCookieFactory;

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

    @Operation(summary = "Login and obtain a JWT; refresh token is set as an HttpOnly cookie")
    @ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Login successful; JWT in body, refresh token in Set-Cookie header",
            content = @Content(schema = @Schema(implementation = LoginResponse.class))),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Invalid credentials",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "429", description = "Rate limit exceeded",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @PostMapping("/login")
    public ResponseEntity<LoginResponse> verifyUser(@Valid @RequestBody LoginRequest loginRequest) {
        LoginResponse response = logInService.execute(loginRequest);
        return buildAuthResponse(response);
    }

    @Operation(summary = "Rotate the refresh token cookie and get a new JWT")
    @ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "New JWT in body, rotated refresh token in Set-Cookie header",
            content = @Content(schema = @Schema(implementation = LoginResponse.class))),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Refresh token cookie missing, invalid, expired, or revoked",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "429", description = "Rate limit exceeded",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @PostMapping("/refresh")
    public ResponseEntity<LoginResponse> refreshToken(
            @CookieValue(name = RefreshTokenCookieFactory.COOKIE_NAME, required = false) String refreshTokenCookie) {
        if (!StringUtils.hasText(refreshTokenCookie)) {
            throw InvalidRefreshTokenException.invalid();
        }
        RefreshTokenRequest refreshTokenRequest = new RefreshTokenRequest();
        refreshTokenRequest.setRefreshToken(refreshTokenCookie);
        LoginResponse response = refreshTokenRequestService.execute(refreshTokenRequest);
        return buildAuthResponse(response);
    }

    /**
     * Puts the rotated refresh token into an HttpOnly Set-Cookie header and
     * leaves only the JWT in the response body.
     */
    private ResponseEntity<LoginResponse> buildAuthResponse(LoginResponse response) {
        ResponseEntity.BodyBuilder builder =
            ResponseEntity.status(response.getResponseOutcome().getHttpStatus());
        if (response.getResponseOutcome().getSuccess() && response.getRefreshToken() != null) {
            builder.header(HttpHeaders.SET_COOKIE,
                refreshTokenCookieFactory.create(response.getRefreshToken()).toString());
        }
        return builder.body(response);
    }
}
