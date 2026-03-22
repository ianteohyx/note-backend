package com.yx.note_app.services.reponse;

import com.yx.note_app.dto.SharedNoteDto;
import io.swagger.v3.oas.annotations.media.Schema;

public class GetSingleSharedNoteResponse extends ApiResponse{
    @Schema(description = "The requested shared note")
    private SharedNoteDto sharedNote;

    public SharedNoteDto getSharedNote() {
        return sharedNote;
    }

    public void setSharedNote(SharedNoteDto sharedNote) {
        this.sharedNote = sharedNote;
    }
}
