package com.yx.note_app.services.service;

import com.yx.note_app.models.Note;
import com.yx.note_app.repositories.NoteRepository;
import com.yx.note_app.services.reponse.ApiResponse;
import com.yx.note_app.services.reponse.ResponseDirectory;
import com.yx.note_app.services.request.AddNoteRequest;
import com.yx.note_app.utils.log.DefaultLogger;
import org.springframework.beans.factory.annotation.Autowired;
import java.util.Objects;

@org.springframework.stereotype.Service
public class AddNoteService extends Service<AddNoteRequest, ApiResponse>{
    @Autowired
    private NoteRepository noteRepository;

    private final DefaultLogger logger = new DefaultLogger(this.getClass());

    @Override
    public ApiResponse doService(AddNoteRequest request) {
        noteRepository.save(buildNote(request));
        logger.log(getUserUsingTheService().getUsername() + " added a note with title: " + request.getNoteTitle());
        return ResponseDirectory.buildSuccessResponse();
    }

    @Override
    public boolean paramCheck(AddNoteRequest request) {
        return super.paramCheck(request)
                && Objects.nonNull(request.getNoteTitle())
                && Objects.nonNull(request.getNoteContent());
    }

    public Note buildNote(AddNoteRequest request){
        Note newNote = new Note();
        newNote.setTitle(request.getNoteTitle());
        newNote.setContent(request.getNoteContent());
        newNote.setAuthor(getUserUsingTheService());
        return newNote;
    }
}
