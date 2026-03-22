package com.yx.note_app.services.request;

import io.swagger.v3.oas.annotations.media.Schema;

public class EditShareNoteRequest extends ApiRequest{
    @Schema(hidden = true)
    private Integer shareNoteId;

    @Schema(description = "New title for the shared note (omit to keep existing)", example = "Revised agenda")
    private String updatedShareNoteTitle;

    @Schema(description = "New content for the shared note (omit to keep existing)", example = "Updated with revised points.")
    private String updatedShareNoteContent;

    public Integer getShareNoteId() {
        return shareNoteId;
    }

    public void setShareNoteId(Integer shareNoteId) {
        this.shareNoteId = shareNoteId;
    }

    public String getUpdatedShareNoteTitle() {
        return updatedShareNoteTitle;
    }

    public void setUpdatedShareNoteTitle(String updatedShareNoteTitle) {
        this.updatedShareNoteTitle = updatedShareNoteTitle;
    }

    public String getUpdatedShareNoteContent() {
        return updatedShareNoteContent;
    }

    public void setGetUpdatedShareNoteContent(String getUpdatedShareNoteContent) {
        this.updatedShareNoteContent = getUpdatedShareNoteContent;
    }
}
