package com.yx.note_app.services.service;

import com.yx.note_app.models.Note;
import com.yx.note_app.repositories.NoteRepository;
import com.yx.note_app.services.reponse.ApiResponse;
import com.yx.note_app.services.reponse.ResponseDirectory;
import com.yx.note_app.services.request.AddNoteRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

@org.springframework.stereotype.Service
public class AddNoteService extends Service<AddNoteRequest, ApiResponse>{

    private static final Logger logger = LoggerFactory.getLogger(AddNoteService.class);

    @Autowired
    private NoteRepository noteRepository;

    @Override
    public ApiResponse doService(AddNoteRequest request) {
        noteRepository.save(buildNote(request));
        logger.info("Note added successfully");
        return ResponseDirectory.buildSuccessResponse();
    }

    private Note buildNote(AddNoteRequest request){
        Note newNote = new Note();
        newNote.setTitle(request.getNoteTitle());
        newNote.setContent(request.getNoteContent());
        newNote.setAuthor(getUserUsingTheService());
        return newNote;
    }
}
