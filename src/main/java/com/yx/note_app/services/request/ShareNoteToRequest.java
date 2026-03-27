package com.yx.note_app.services.request;

import com.yx.note_app.enums.Permission;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public class ShareNoteToRequest extends ApiRequest{
    @Schema(description = "ID of the note to share", example = "42")
    @NotNull(message = "Note ID is required")
    private Integer noteId;

    @Schema(description = "Username of the user to share with", example = "jane_doe")
    @NotBlank(message = "Username to share with is required")
    private String sharedToUsername;

    @Schema(description = "Access level granted to the recipient", example = "READ")
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
