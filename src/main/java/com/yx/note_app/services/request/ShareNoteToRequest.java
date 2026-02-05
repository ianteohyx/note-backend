package com.yx.note_app.services.request;

import com.yx.note_app.enums.Permission;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public class ShareNoteToRequest extends ApiRequest{
    @NotNull(message = "Note ID is required")
    private Integer noteId;

    @NotBlank(message = "Username to share with is required")
    private String sharedToUsername;

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
