package com.yx.note_app.services.reponse;

import com.fasterxml.jackson.annotation.JsonIgnore;
import io.swagger.v3.oas.annotations.media.Schema;

public class LoginResponse extends ApiResponse{
    @Schema(description = "JWT access token (valid for 15 minutes)", example = "eyJhbGciOiJIUzI1NiJ9...")
    private String token;

    /**
     * Carried internally from the service to the controller only.
     * It is delivered to the client as an HttpOnly `refreshToken` cookie,
     * never serialized into the response body.
     */
    @Schema(hidden = true)
    @JsonIgnore
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
