package com.yx.note_app.services.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class AddNoteRequest extends ApiRequest{
    @NotBlank(message = "Note title is required")
    @Size(max = 255, message = "Note title must not exceed 255 characters")
    private String noteTitle;

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
