package com.yx.note_app.services.service;

import com.yx.note_app.enums.ResponseOutcome;
import com.yx.note_app.exception.ResourceNotFoundException;
import com.yx.note_app.exception.UnauthorizedException;
import com.yx.note_app.models.Note;
import com.yx.note_app.repositories.NoteRepository;
import com.yx.note_app.services.reponse.ApiResponse;
import com.yx.note_app.services.reponse.GetSingleNoteResponse;
import com.yx.note_app.services.request.GetSingleNoteRequest;
import com.yx.note_app.utils.mapper.Note2NoteDto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import java.util.Objects;

@org.springframework.stereotype.Service
public class GetSingleNoteService extends Service<GetSingleNoteRequest, ApiResponse>{

    private static final Logger logger = LoggerFactory.getLogger(GetSingleNoteService.class);

    @Autowired
    private NoteRepository noteRepository;

    @Override
    @Transactional(readOnly = true)
    public ApiResponse doService(GetSingleNoteRequest request) {
        int id = request.getNoteId();
        Note note = noteRepository.findById(id);

        if (Objects.isNull(note)){
            throw ResourceNotFoundException.noteNotFound(id);
        }

        if (!note.getAuthor().equals(getUserUsingTheService())){
            throw UnauthorizedException.notOwner(getUserUsingTheService().getUsername());
        }

        logger.info("User {} retrieved note id: {}", getUserUsingTheService().getUsername(), id);
        return buildGetSingleNoteResponse(note);
    }

    private GetSingleNoteResponse buildGetSingleNoteResponse(Note note){
        GetSingleNoteResponse getSingleNoteResponse = new GetSingleNoteResponse();
        Note2NoteDto note2NoteDto = new Note2NoteDto();
        getSingleNoteResponse.setNoteDto(note2NoteDto.toResponse(note));
        getSingleNoteResponse.setResponseOutcome(ResponseOutcome.SUCCESS);
        return getSingleNoteResponse;
    }
}
