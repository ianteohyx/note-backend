package com.yx.note_app.services.reponse;

import io.swagger.v3.oas.annotations.media.Schema;

public class LoginResponse extends ApiResponse{
    @Schema(description = "JWT access token (valid for 15 minutes)", example = "eyJhbGciOiJIUzI1NiJ9...")
    private String token;

    @Schema(description = "Refresh token (valid for 7 days)", example = "550e8400-e29b-41d4-a716-446655440000")
    private String refreshToken;

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getRefreshToken() {
        return refreshToken;
    }

    public void setRefreshToken(String refreshToken) {
        this.refreshToken = refreshToken;
    }
}
