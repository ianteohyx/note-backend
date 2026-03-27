package com.yx.note_app.services.service;

import com.yx.note_app.exception.ResourceNotFoundException;
import com.yx.note_app.models.SharedNote;
import com.yx.note_app.repositories.NoteRepository;
import com.yx.note_app.repositories.ShareNoteRepository;
import com.yx.note_app.services.reponse.ApiResponse;
import com.yx.note_app.services.reponse.ResponseDirectory;
import com.yx.note_app.services.request.EditShareNoteRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import java.util.Objects;

@org.springframework.stereotype.Service
public class EditSharedNoteService extends Service<EditShareNoteRequest, ApiResponse>{

    private static final Logger logger = LoggerFactory.getLogger(EditSharedNoteService.class);

    @Autowired
    private ShareNoteRepository shareNoteRepository;

    @Autowired
    private NoteRepository noteRepository;

    @Override
    @Transactional
    public ApiResponse doService(EditShareNoteRequest request) {
        int sharedNoteId = request.getShareNoteId();
        SharedNote sharedNote = shareNoteRepository.findById(sharedNoteId);

        if(Objects.isNull(sharedNote)){
            throw ResourceNotFoundException.sharedNoteNotFound(sharedNoteId);
        }

        assertHasWritePermission(sharedNote);

        noteRepository.updateNote(sharedNote.getNote().getId(), request.getUpdatedShareNoteTitle(), request.getUpdatedShareNoteContent());
        logger.info("User {} updated shared note id: {}", getUserUsingTheService().getUsername(), sharedNote.getNote().getId());
        return ResponseDirectory.buildSuccessResponse();
    }
}
