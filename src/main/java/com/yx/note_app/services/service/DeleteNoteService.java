package com.yx.note_app.services.service;

import com.yx.note_app.enums.ResponseOutcome;
import com.yx.note_app.models.Note;
import com.yx.note_app.repositories.NoteRepository;
import com.yx.note_app.services.reponse.ApiResponse;
import com.yx.note_app.services.reponse.ResponseDirectory;
import com.yx.note_app.services.request.DeleteNoteRequest;
import com.yx.note_app.utils.log.DefaultLogger;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import java.util.Objects;

@org.springframework.stereotype.Service
public class DeleteNoteService extends Service <DeleteNoteRequest, ApiResponse> {
    private static final Logger log = LoggerFactory.getLogger(DeleteNoteService.class);
    @Autowired
    NoteRepository noteRepository;

    private final DefaultLogger logger = new DefaultLogger(this.getClass());

    @Override
    public ApiResponse doService(DeleteNoteRequest request) {
        // check if note exist
        int id = request.getNoteId();
        Note note = noteRepository.findById(id);
        if (Objects.isNull(note)){
            logger.log("deleting unexisted note: " + request.getNoteId() +" by user: " + getUserUsingTheService().getUsername());
            return ResponseDirectory.buildFailResponse(ResponseOutcome.NOTE_NOT_EXIST);
        }

        // check if action is authorized
        if (!note.getAuthor().equals(getUserUsingTheService())){
            logger.log("unauthorized action by user: " + getUserUsingTheService().getUsername());
            return ResponseDirectory.buildFailResponse(ResponseOutcome.ACTION_NOT_ALLOWED);
        }

        noteRepository.deleteById(id);
        logger.log("deleted note id: " + request.getNoteId());
        return ResponseDirectory.buildSuccessResponse();
    }

    @Override
    public boolean paramCheck(DeleteNoteRequest request){
        return super.paramCheck(request) && Objects.nonNull(request.getNoteId());
    }
}
