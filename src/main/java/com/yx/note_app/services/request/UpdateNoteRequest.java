package com.yx.note_app.services.request;

import jakarta.validation.constraints.Size;

public class UpdateNoteRequest extends ApiRequest{
    private Integer noteId;

    @Size(max = 255, message = "Note title must not exceed 255 characters")
    private String noteTitle;

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
