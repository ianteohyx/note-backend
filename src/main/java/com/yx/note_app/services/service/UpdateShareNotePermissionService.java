package com.yx.note_app.services.service;

import com.yx.note_app.exception.ResourceNotFoundException;
import com.yx.note_app.models.Note;
import com.yx.note_app.models.SharedNote;
import com.yx.note_app.models.User;
import com.yx.note_app.repositories.NoteRepository;
import com.yx.note_app.repositories.ShareNoteRepository;
import com.yx.note_app.repositories.UserRepository;
import com.yx.note_app.services.reponse.ApiResponse;
import com.yx.note_app.services.reponse.ResponseDirectory;
import com.yx.note_app.services.request.UpdateShareNotePermissionRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import java.util.Objects;
import java.util.Optional;

@org.springframework.stereotype.Service
public class UpdateShareNotePermissionService extends Service<UpdateShareNotePermissionRequest, ApiResponse> {

    private static final Logger logger = LoggerFactory.getLogger(UpdateShareNotePermissionService.class);

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private NoteRepository noteRepository;

    @Autowired
    private ShareNoteRepository shareNoteRepository;

    @Override
    @Transactional
    public ApiResponse doService(UpdateShareNotePermissionRequest request) {
        Optional<User> user = userRepository.findByUsername(request.getSharedToUsername());

        if(user.isEmpty()){
            throw ResourceNotFoundException.userNotFound(request.getSharedToUsername());
        }

        Note note = noteRepository.findById((int)request.getNoteId());
        if (Objects.isNull(note)){
            throw ResourceNotFoundException.noteNotFound(request.getNoteId());
        }

        assertIsOwner(note);

        SharedNote sharedNote = shareNoteRepository.findByNoteIdAndSharedToUserId(note.getId(), user.get().getId());

        if (Objects.isNull(sharedNote)){
            throw ResourceNotFoundException.noteNotSharedToUser(note.getId(), user.get().getUsername());
        }

        shareNoteRepository.updateSharedNotePermission(sharedNote.getId(), request.getPermission());
        logger.info("User {} updated permission on shared note {} to {}", getUserUsingTheService().getUsername(), sharedNote.getId(), request.getPermission());
        return ResponseDirectory.buildSuccessResponse();
    }
}
