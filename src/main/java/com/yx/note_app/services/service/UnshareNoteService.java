package com.yx.note_app.services.service;

import com.yx.note_app.exception.ResourceNotFoundException;
import com.yx.note_app.exception.UnauthorizedException;
import com.yx.note_app.models.Note;
import com.yx.note_app.models.SharedNote;
import com.yx.note_app.models.User;
import com.yx.note_app.repositories.NoteRepository;
import com.yx.note_app.repositories.ShareNoteRepository;
import com.yx.note_app.repositories.UserRepository;
import com.yx.note_app.services.reponse.ApiResponse;
import com.yx.note_app.services.reponse.ResponseDirectory;
import com.yx.note_app.services.request.UnshareNoteRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Objects;
import java.util.Optional;

@org.springframework.stereotype.Service
public class UnshareNoteService extends Service<UnshareNoteRequest, ApiResponse> {

    private static final Logger logger = LoggerFactory.getLogger(UnshareNoteService.class);

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private NoteRepository noteRepository;

    @Autowired
    private ShareNoteRepository shareNoteRepository;

    @Override
    public ApiResponse doService(UnshareNoteRequest request) {
        Optional<User> user = userRepository.findByUsername(request.getSharedToUsername());

        if(user.isEmpty()){
            throw ResourceNotFoundException.userNotFound(request.getSharedToUsername());
        }

        Note note = noteRepository.findById((int)request.getNoteId());
        if (Objects.isNull(note)){
            throw ResourceNotFoundException.noteNotFound(request.getNoteId());
        }

        if (!note.getAuthor().equals(getUserUsingTheService())){
            throw UnauthorizedException.notOwner(getUserUsingTheService().getUsername());
        }

        SharedNote sharedNote = shareNoteRepository.findByNoteIdAndSharedToUserId(note.getId(), user.get().getId());

        if (Objects.isNull(sharedNote)){
            throw ResourceNotFoundException.noteNotSharedToUser(note.getId(), user.get().getUsername());
        }

        shareNoteRepository.deleteById(sharedNote.getId());
        logger.info("User {} unshared note {} from user {}", getUserUsingTheService().getUsername(), note.getId(), user.get().getUsername());
        return ResponseDirectory.buildSuccessResponse();
    }
}
