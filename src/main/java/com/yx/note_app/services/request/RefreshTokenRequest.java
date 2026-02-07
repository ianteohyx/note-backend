package com.yx.note_app.services.request;

import jakarta.validation.constraints.NotBlank;

public class RefreshTokenRequest extends ApiRequest {

    @NotBlank(message = "Refresh token is required")
    private String refreshToken;

    public String getRefreshToken() {
        return refreshToken;
    }

    public void setRefreshToken(String refreshToken) {
        this.refreshToken = refreshToken;
    }
}
