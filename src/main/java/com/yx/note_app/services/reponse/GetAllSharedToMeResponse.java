package com.yx.note_app.services.reponse;

import com.yx.note_app.dto.SharedNoteDto;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;

public class GetAllSharedToMeResponse extends ApiResponse{
    @Schema(description = "List of notes shared to the authenticated user")
    List<SharedNoteDto> sharedNotes;

    public List<SharedNoteDto> getSharedNotes() {
        return sharedNotes;
    }

    public void setSharedNotes(List<SharedNoteDto> sharedNotes) {
        this.sharedNotes = sharedNotes;
    }
}
