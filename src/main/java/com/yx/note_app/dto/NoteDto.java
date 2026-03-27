package com.yx.note_app.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;

public class NoteDto extends Dto{
    @Schema(description = "Note title", example = "Meeting notes")
    private String title;

    @Schema(description = "Note body content", example = "Discussed Q1 goals and assigned action items.")
    private String content;

    @Schema(description = "Username of the note author", example = "john_doe")
    private String  authorName;

    @Schema(description = "Timestamp when the note was created", example = "2024-01-15T10:30:00")
    private LocalDateTime dateCreated;

    @Schema(description = "Timestamp when the note was last modified", example = "2024-01-16T14:00:00")
    private LocalDateTime dateModified;

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getAuthorName() {
        return authorName;
    }

    public void setAuthorName(String authorName) {
        this.authorName = authorName;
    }

    public LocalDateTime getDateModified() {
        return dateModified;
    }

    public void setDateModified(LocalDateTime dateModified) {
        this.dateModified = dateModified;
    }

    public LocalDateTime getDateCreated() {
        return dateCreated;
    }

    public void setDateCreated(LocalDateTime dateCreated) {
        this.dateCreated = dateCreated;
    }
}
