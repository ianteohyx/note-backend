package com.yx.note_app.services.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

public class RefreshTokenRequest extends ApiRequest {

    @Schema(description = "Refresh token obtained from login or previous refresh", example = "550e8400-e29b-41d4-a716-446655440000")
    @NotBlank(message = "Refresh token is required")
    private String refreshToken;

    public String getRefreshToken() {
        return refreshToken;
    }

    public void setRefreshToken(String refreshToken) {
        this.refreshToken = refreshToken;
    }
}
