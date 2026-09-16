package com.yx.note_app.services.request;

import com.yx.note_app.enums.Permission;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public class UpdateShareNotePermissionItem {
    @Schema(description = "ID of the note whose share permission is being updated", example = "42")
    @NotNull(message = "Note ID is required")
    private Integer noteId;

    @Schema(description = "Username of the user whose permission to update", example = "jane_doe")
    @NotBlank(message = "Username to update permission for is required")
    private String sharedToUsername;

    @Schema(description = "New permission to assign", example = "WRITE")
    @NotNull(message = "Permission is required")
    private Permission permission;

    public Integer getNoteId() {
        return noteId;
    }

    public void setNoteId(Integer noteId) {
        this.noteId = noteId;
    }

    public String getSharedToUsername() {
        return sharedToUsername;
    }

    public void setSharedToUsername(String sharedToUsername) {
        this.sharedToUsername = sharedToUsername;
    }

    public Permission getPermission() {
        return permission;
    }

    public void setPermission(Permission permission) {
        this.permission = permission;
    }
}
