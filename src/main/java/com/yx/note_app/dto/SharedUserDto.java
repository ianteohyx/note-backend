package com.yx.note_app.dto;

import com.yx.note_app.enums.Permission;
import io.swagger.v3.oas.annotations.media.Schema;

public class SharedUserDto {
    @Schema(description = "Username of the user the note is shared with", example = "jane_doe")
    private String username;

    @Schema(description = "Access level granted to this user", example = "READ")
    private Permission permission;

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public Permission getPermission() {
        return permission;
    }

    public void setPermission(Permission permission) {
        this.permission = permission;
    }
}
