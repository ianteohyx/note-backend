package com.yx.note_app.services.request;

import com.yx.note_app.enums.Permission;
import io.swagger.v3.oas.annotations.media.Schema;

public class UpdateShareNotePermissionRequest extends ApiRequest{
    @Schema(hidden = true)
    private String sharedToUsername;

    @Schema(hidden = true)
    private Integer noteId;

    @Schema(description = "New permission to assign", example = "WRITE")
    private Permission permission;

    public String getSharedToUsername() {
        return sharedToUsername;
    }

    public void setSharedToUsername(String shareToUsername) {
        this.sharedToUsername = shareToUsername;
    }

    public Integer getNoteId() {
        return noteId;
    }

    public void setNoteId(Integer noteId) {
        this.noteId = noteId;
    }

    public Permission getPermission() {
        return permission;
    }

    public void setPermission(Permission permission) {
        this.permission = permission;
    }
}
