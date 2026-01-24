package com.yx.note_app.services.service;

import com.yx.note_app.enums.ResponseOutcome;
import com.yx.note_app.models.Note;
import com.yx.note_app.repositories.NoteRepository;
import com.yx.note_app.services.reponse.ApiResponse;
import com.yx.note_app.services.reponse.GetSingleNoteResponse;
import com.yx.note_app.services.reponse.ResponseDirectory;
import com.yx.note_app.services.request.GetSingleNoteRequest;
import com.yx.note_app.utils.log.DefaultLogger;
import com.yx.note_app.utils.mapper.Note2NoteDto;
import org.springframework.beans.factory.annotation.Autowired;
import java.util.Objects;

@org.springframework.stereotype.Service
public class GetSingleNoteService extends Service <GetSingleNoteRequest, ApiResponse>{

    @Autowired
    private NoteRepository noteRepository;
    private final DefaultLogger logger = new DefaultLogger(this.getClass());

    @Override
    public boolean paramCheck(GetSingleNoteRequest getSingleNoteRequest){
        return super.paramCheck(getSingleNoteRequest) && Objects.nonNull(getSingleNoteRequest.getNoteId());
    }

    @Override
    public ApiResponse doService(GetSingleNoteRequest request) {
        // check if note exist
        int id = request.getNoteId();
        Note note = noteRepository.findById(id);
        if (Objects.isNull(note)){
            logger.log("getting note id that does not exist: " + request.getNoteId() +" by user: " + getUserUsingTheService().getUsername());
            return ResponseDirectory.buildFailResponse(ResponseOutcome.NOTE_NOT_EXIST);
        }

        // check if action is authorized
        if (!note.getAuthor().equals(getUserUsingTheService())){
            logger.log("unauthorized action by user: " + getUserUsingTheService().getUsername());
            return ResponseDirectory.buildFailResponse(ResponseOutcome.ACTION_NOT_ALLOWED);
        }

        logger.log("getting note id: " + request.getNoteId() +" by user: " + getUserUsingTheService().getUsername());
        return buildGetSingleNoteResponse(note);
    }

    public GetSingleNoteResponse buildGetSingleNoteResponse(Note note){
        GetSingleNoteResponse getSingleNoteResponse = new GetSingleNoteResponse();
        Note2NoteDto note2NoteDto = new Note2NoteDto();
        getSingleNoteResponse.setNoteDto(note2NoteDto.toResponse(note));
        getSingleNoteResponse.setResponseOutcome(ResponseOutcome.SUCCESS);
        return getSingleNoteResponse;
    }
}
