package com.yx.note_app.dto;

import com.yx.note_app.enums.Permission;
import io.swagger.v3.oas.annotations.media.Schema;

public class SharedNoteDto extends Dto{
    @Schema(description = "The shared note's details")
    private NoteDto note;

    @Schema(description = "Access level granted to the recipient", example = "READ")
    private Permission permission;

    public NoteDto getNote() {
        return note;
    }

    public void setNote(NoteDto note) {
        this.note = note;
    }

    public Permission getPermission() {
        return permission;
    }

    public void setPermission(Permission permission) {
        this.permission = permission;
    }
}
