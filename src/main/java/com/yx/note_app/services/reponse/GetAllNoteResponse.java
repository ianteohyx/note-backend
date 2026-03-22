package com.yx.note_app.services.reponse;

import com.yx.note_app.dto.NoteDto;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;

public class GetAllNoteResponse extends ApiResponse {
    @Schema(description = "List of notes belonging to the authenticated user")
    private List<NoteDto> notes;

    public List<NoteDto> getNotes() {
        return notes;
    }

    public void setNotes(List<NoteDto> notes) {
        this.notes = notes;
    }
}
