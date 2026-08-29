package com.yx.note_app.services.request;

import io.swagger.v3.oas.annotations.media.Schema;

public class RefreshTokenRequest extends ApiRequest {

    // Populated by the controller from the HttpOnly `refreshToken` cookie, not from the request body.
    @Schema(hidden = true)
    private String refreshToken;

    public String getRefreshToken() {
        return refreshToken;
    }

    public void setRefreshToken(String refreshToken) {
        this.refreshToken = refreshToken;
    }
}
