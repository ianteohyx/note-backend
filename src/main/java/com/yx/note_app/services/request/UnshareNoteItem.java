package com.yx.note_app.services.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public class UnshareNoteItem {
    @Schema(description = "ID of the note to revoke access to", example = "42")
    @NotNull(message = "Note ID is required")
    private Integer noteId;

    @Schema(description = "Username of the user to revoke access from", example = "jane_doe")
    @NotBlank(message = "Username to revoke access from is required")
    private String sharedToUsername;

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
}
