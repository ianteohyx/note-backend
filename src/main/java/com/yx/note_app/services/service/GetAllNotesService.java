package com.yx.note_app.services.service;

import com.yx.note_app.dto.NoteDto;
import com.yx.note_app.enums.ResponseOutcome;
import com.yx.note_app.models.Note;
import com.yx.note_app.repositories.NoteRepository;
import com.yx.note_app.services.reponse.GetAllNoteResponse;
import com.yx.note_app.services.request.ApiRequest;
import com.yx.note_app.utils.log.DefaultLogger;
import com.yx.note_app.utils.mapper.Note2NoteDto;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@org.springframework.stereotype.Service
public class GetAllNotesService extends Service <ApiRequest, GetAllNoteResponse> {
    @Autowired
    private NoteRepository noteRepository;
    private final DefaultLogger logger = new DefaultLogger(this.getClass());

    @Override
    public GetAllNoteResponse doService(ApiRequest request) {
        List<Note> notes = noteRepository.findByAuthor(getUserUsingTheService());
        GetAllNoteResponse response = buildSuccessGetAllNotesResponse(notes);
        logger.log(getUserUsingTheService().getUsername() + " getting all notes");
        return response;
    }

    @Override
    public boolean paramCheck(ApiRequest request) {
        return super.paramCheck(request);
    }

    public GetAllNoteResponse buildSuccessGetAllNotesResponse(List<Note> notes){
        GetAllNoteResponse getAllNoteResponse = new GetAllNoteResponse();
        Note2NoteDto note2NoteDto = new Note2NoteDto();
        List<NoteDto> noteDto = new ArrayList<NoteDto>();
        notes.forEach(note -> noteDto.add(note2NoteDto.toResponse(note)));
        getAllNoteResponse.setNotes(noteDto);
        getAllNoteResponse.setResponseOutcome(ResponseOutcome.SUCCESS);
        return getAllNoteResponse;
    }
}
