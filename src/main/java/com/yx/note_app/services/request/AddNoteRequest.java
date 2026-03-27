package com.yx.note_app.services.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class AddNoteRequest extends ApiRequest{
    @Schema(description = "Title of the note (max 255 characters)", example = "Meeting notes")
    @NotBlank(message = "Note title is required")
    @Size(max = 255, message = "Note title must not exceed 255 characters")
    private String noteTitle;

    @Schema(description = "Body content of the note", example = "Discussed Q1 goals and assigned action items.")
    @Size(max = 65535, message = "Note content must not exceed 65535 characters")
    private String noteContent;

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
