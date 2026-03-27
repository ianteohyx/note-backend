package com.yx.note_app.services.reponse;

import com.yx.note_app.dto.NoteDto;
import io.swagger.v3.oas.annotations.media.Schema;

public class GetSingleNoteResponse extends ApiResponse{
    @Schema(description = "The requested note")
    private NoteDto noteDto;

    public NoteDto getNoteDto() {
        return noteDto;
    }

    public void setNoteDto(NoteDto noteDto) {
        this.noteDto = noteDto;
    }
}
