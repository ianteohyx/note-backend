package com.yx.note_app.services.service;

import com.yx.note_app.exception.ResourceNotFoundException;
import com.yx.note_app.models.Note;
import com.yx.note_app.repositories.NoteRepository;
import com.yx.note_app.services.reponse.ApiResponse;
import com.yx.note_app.services.reponse.ResponseDirectory;
import com.yx.note_app.services.request.DeleteNoteRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import java.util.Objects;

@org.springframework.stereotype.Service
public class DeleteNoteService extends Service<DeleteNoteRequest, ApiResponse> {

    private static final Logger logger = LoggerFactory.getLogger(DeleteNoteService.class);

    @Autowired
    private NoteRepository noteRepository;

    @Override
    @Transactional
    public ApiResponse doService(DeleteNoteRequest request) {
        int id = request.getNoteId();
        Note note = noteRepository.findById(id);

        if (Objects.isNull(note)){
            throw ResourceNotFoundException.noteNotFound(id);
        }

        assertIsOwner(note);

        noteRepository.deleteById(id);
        logger.info("Deleted note id: {}", id);
        return ResponseDirectory.buildSuccessResponse();
    }
}
