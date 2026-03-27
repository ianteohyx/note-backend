package com.yx.note_app.services.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

public class LoginRequest extends ApiRequest{
    @Schema(description = "Registered username", example = "john_doe")
    @NotBlank(message = "Username is required")
    private String username;

    @Schema(description = "Account password", example = "Secret@123")
    @NotBlank(message = "Password is required")
    private String password;

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}
