package com.yx.note_app.services.service;

import com.yx.note_app.enums.Permission;
import com.yx.note_app.exception.DuplicateResourceException;
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
import com.yx.note_app.services.request.ShareNoteToRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import java.util.Objects;
import java.util.Optional;

@org.springframework.stereotype.Service
public class ShareNoteToOthersService extends Service<ShareNoteToRequest, ApiResponse>{

    private static final Logger logger = LoggerFactory.getLogger(ShareNoteToOthersService.class);

    @Autowired
    private NoteRepository noteRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ShareNoteRepository shareNoteRepository;

    @Override
    @Transactional
    public ApiResponse doService(ShareNoteToRequest request) {
        int id = request.getNoteId();
        Note note = noteRepository.findById(id);

        if (Objects.isNull(note)){
            throw ResourceNotFoundException.noteNotFound(id);
        }

        if (!note.getAuthor().equals(getUserUsingTheService())){
            throw UnauthorizedException.notOwner(getUserUsingTheService().getUsername());
        }

        Optional<User> shareToUser = userRepository.findByUsername(request.getSharedToUsername());
        if (shareToUser.isEmpty() || shareToUser.get().equals(getUserUsingTheService())){
            throw ResourceNotFoundException.userNotFound(request.getSharedToUsername());
        }

        if (shareNoteRepository.existsByNoteIdAndSharedToUserId(request.getNoteId(), shareToUser.get().getId())){
            throw DuplicateResourceException.noteAlreadyShared(request.getNoteId(), request.getSharedToUsername());
        }

        shareNoteRepository.save(buildSharedNote(note, shareToUser.get(), request.getPermission()));
        logger.info("User {} shared note {} to user {}", getUserUsingTheService().getUsername(), id, shareToUser.get().getUsername());
        return ResponseDirectory.buildSuccessResponse();
    }

    private SharedNote buildSharedNote(Note sharedNote, User shareToUser, Permission permission){
        SharedNote newSharedNote = new SharedNote();
        newSharedNote.setNote(sharedNote);
        newSharedNote.setSharedToUser(shareToUser);
        newSharedNote.setPermission(permission);
        return newSharedNote;
    }
}
