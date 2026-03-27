package com.yx.note_app.services.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Size;

public class UpdateNoteRequest extends ApiRequest{
    @Schema(hidden = true)
    private Integer noteId;

    @Schema(description = "New title for the note (omit to keep existing)", example = "Updated meeting notes")
    @Size(max = 255, message = "Note title must not exceed 255 characters")
    private String noteTitle;

    @Schema(description = "New content for the note (omit to keep existing)", example = "Added follow-up items from the Q1 review.")
    @Size(max = 65535, message = "Note content must not exceed 65535 characters")
    private String noteContent;

    public Integer getNoteId() {
        return noteId;
    }

    public void setNoteId(Integer notId) {
        this.noteId = notId;
    }

    public String getNoteTitle() {
        return noteTitle;
    }

    public void setNoteTitle(String noteTitle) {
        this.noteTitle = noteTitle;
    }

    public String getNoteContent() {
        return noteContent;
    }

    public void setNoteContent(String noteContent) {
        this.noteContent = noteContent;
    }
}
