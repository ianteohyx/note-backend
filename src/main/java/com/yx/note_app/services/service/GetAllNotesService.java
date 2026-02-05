package com.yx.note_app.services.service;

import com.yx.note_app.dto.NoteDto;
import com.yx.note_app.enums.ResponseOutcome;
import com.yx.note_app.models.Note;
import com.yx.note_app.repositories.NoteRepository;
import com.yx.note_app.services.reponse.GetAllNoteResponse;
import com.yx.note_app.services.request.ApiRequest;
import com.yx.note_app.utils.mapper.Note2NoteDto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.ArrayList;
import java.util.List;

@org.springframework.stereotype.Service
public class GetAllNotesService extends Service<ApiRequest, GetAllNoteResponse> {

    private static final Logger logger = LoggerFactory.getLogger(GetAllNotesService.class);

    @Autowired
    private NoteRepository noteRepository;

    @Override
    public GetAllNoteResponse doService(ApiRequest request) {
        List<Note> notes = noteRepository.findByAuthor(getUserUsingTheService());
        logger.info("User {} retrieved all notes", getUserUsingTheService().getUsername());
        return buildSuccessGetAllNotesResponse(notes);
    }

    private GetAllNoteResponse buildSuccessGetAllNotesResponse(List<Note> notes){
        GetAllNoteResponse getAllNoteResponse = new GetAllNoteResponse();
        Note2NoteDto note2NoteDto = new Note2NoteDto();
        List<NoteDto> noteDto = new ArrayList<>();
        notes.forEach(note -> noteDto.add(note2NoteDto.toResponse(note)));
        getAllNoteResponse.setNotes(noteDto);
        getAllNoteResponse.setResponseOutcome(ResponseOutcome.SUCCESS);
        return getAllNoteResponse;
    }
}
