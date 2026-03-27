package com.yx.note_app.services.service;

import com.yx.note_app.dto.NoteDto;
import com.yx.note_app.enums.ResponseOutcome;
import com.yx.note_app.models.Note;
import com.yx.note_app.repositories.NoteRepository;
import com.yx.note_app.services.reponse.GetAllNoteResponse;
import com.yx.note_app.services.request.GetAllNotesRequest;
import com.yx.note_app.utils.mapper.Note2NoteDto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;

import java.util.List;

@org.springframework.stereotype.Service
public class GetAllNotesService extends Service<GetAllNotesRequest, GetAllNoteResponse> {

    private static final Logger logger = LoggerFactory.getLogger(GetAllNotesService.class);

    @Autowired
    private NoteRepository noteRepository;

    @Override
    @Transactional(readOnly = true)
    public GetAllNoteResponse doService(GetAllNotesRequest request) {
        PageRequest pageable = PageRequest.of(request.getPage(), request.getSize(), Sort.by("dateModified").descending());
        Page<Note> notePage = noteRepository.findByAuthor(getUserUsingTheService(), pageable);
        logger.info("User {} retrieved notes page {}/{}", getUserUsingTheService().getUsername(), request.getPage(), notePage.getTotalPages());
        return buildResponse(notePage, request);
    }

    private GetAllNoteResponse buildResponse(Page<Note> notePage, GetAllNotesRequest request) {
        Note2NoteDto note2NoteDto = new Note2NoteDto();
        List<NoteDto> noteDtos = notePage.getContent().stream()
                .map(note2NoteDto::toResponse)
                .toList();

        GetAllNoteResponse response = new GetAllNoteResponse();
        response.setNotes(noteDtos);
        response.setPage(request.getPage());
        response.setSize(request.getSize());
        response.setTotalElements(notePage.getTotalElements());
        response.setTotalPages(notePage.getTotalPages());
        response.setResponseOutcome(ResponseOutcome.SUCCESS);
        return response;
    }
}
