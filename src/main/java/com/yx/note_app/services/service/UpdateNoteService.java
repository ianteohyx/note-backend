package com.yx.note_app.services.service;

import com.yx.note_app.exception.ResourceNotFoundException;
import com.yx.note_app.exception.UnauthorizedException;
import com.yx.note_app.models.Note;
import com.yx.note_app.repositories.NoteRepository;
import com.yx.note_app.services.reponse.ApiResponse;
import com.yx.note_app.services.reponse.ResponseDirectory;
import com.yx.note_app.services.request.UpdateNoteRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Objects;

@org.springframework.stereotype.Service
public class UpdateNoteService extends Service<UpdateNoteRequest, ApiResponse>{

    private static final Logger logger = LoggerFactory.getLogger(UpdateNoteService.class);

    @Autowired
    private NoteRepository noteRepository;

    @Override
    public ApiResponse doService(UpdateNoteRequest request) {
        int id = request.getNoteId();
        Note note = noteRepository.findById(id);

        if (Objects.isNull(note)){
            throw ResourceNotFoundException.noteNotFound(id);
        }

        if (!note.getAuthor().equals(getUserUsingTheService())){
            throw UnauthorizedException.notOwner(getUserUsingTheService().getUsername());
        }

        noteRepository.updateNote(request.getNoteId(), request.getNoteTitle(), request.getNoteContent());
        logger.info("Updated note id: {} by user: {}", request.getNoteId(), getUserUsingTheService().getUsername());
        return ResponseDirectory.buildSuccessResponse();
    }
}
