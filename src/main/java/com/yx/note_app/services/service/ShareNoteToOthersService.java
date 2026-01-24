package com.yx.note_app.services.service;

import com.yx.note_app.enums.Permission;
import com.yx.note_app.enums.ResponseOutcome;
import com.yx.note_app.models.Note;
import com.yx.note_app.models.SharedNote;
import com.yx.note_app.models.User;
import com.yx.note_app.repositories.NoteRepository;
import com.yx.note_app.repositories.ShareNoteRepository;
import com.yx.note_app.repositories.UserRepository;
import com.yx.note_app.services.reponse.ApiResponse;
import com.yx.note_app.services.reponse.ResponseDirectory;
import com.yx.note_app.services.request.ShareNoteToRequest;
import com.yx.note_app.utils.log.DefaultLogger;
import org.springframework.beans.factory.annotation.Autowired;
import java.util.Objects;
import java.util.Optional;

@org.springframework.stereotype.Service
public class ShareNoteToOthersService extends Service<ShareNoteToRequest, ApiResponse>{
    @Autowired
    NoteRepository noteRepository;

    @Autowired
    UserRepository userRepository;

    @Autowired
    ShareNoteRepository shareNoteRepository;

    private final DefaultLogger logger = new DefaultLogger(this.getClass());

    @Override
    public ApiResponse doService(ShareNoteToRequest request) {
        // get the note
        int id = request.getNoteId();
        Note note = noteRepository.findById(id);
        if (Objects.isNull(note)){
            logger.log("sharing unexisted note: " + request.getNoteId() +" by user: " + getUserUsingTheService().getUsername());
            return ResponseDirectory.buildFailResponse(ResponseOutcome.NOTE_NOT_EXIST);
        }

        // check if action is authorized
        if (!note.getAuthor().equals(getUserUsingTheService())){
            logger.log("unauthorized action by user: " + getUserUsingTheService().getUsername());
            return ResponseDirectory.buildFailResponse(ResponseOutcome.ACTION_NOT_ALLOWED);
        }

        //get the shared to user
        Optional<User> shareToUser = userRepository.findByUsername(request.getSharedToUsername());
        if (shareToUser.isEmpty() || shareToUser.get().equals(getUserUsingTheService())){
            logger.log("sharing from user: " + getUserUsingTheService().getUsername() + " to non existed user: "+ request.getSharedToUsername());
            return ResponseDirectory.buildFailResponse(ResponseOutcome.USER_NOT_EXIST);
        }

        // check for duplicates
        if (shareNoteRepository.existsByNoteIdAndSharedToUserId(request.getNoteId(), shareToUser.get().getId())){
            logger.log("note has already shared to this user: " + getUserUsingTheService().getUsername());
            return ResponseDirectory.buildFailResponse(ResponseOutcome.NOTE_ALREADY_SHARED);
        }

        // save the shared note
        shareNoteRepository.save(buildSharedNote(note, shareToUser.get(), request.getPermission()));
        logger.log("shared a note from user: " + getUserUsingTheService().getUsername() + " to user: " + shareToUser.get().getUsername());
        return ResponseDirectory.buildSuccessResponse();
    }

    @Override
    public boolean paramCheck(ShareNoteToRequest shareNoteToRequest){
        return super.paramCheck(shareNoteToRequest)
                && Objects.nonNull(shareNoteToRequest.getNoteId())
                && Objects.nonNull(shareNoteToRequest.getSharedToUsername())
                && Objects.nonNull(shareNoteToRequest.getPermission());
    }

    public SharedNote buildSharedNote(Note sharedNote, User shareToUser, Permission permission){
        SharedNote newSharedNote = new SharedNote();
        newSharedNote.setNote(sharedNote);
        newSharedNote.setSharedToUser(shareToUser);
        newSharedNote.setPermission(permission);
        return newSharedNote;
    }
}
