package com.yx.note_app.services.service;

import com.yx.note_app.enums.ResponseOutcome;
import com.yx.note_app.models.Note;
import com.yx.note_app.models.SharedNote;
import com.yx.note_app.models.User;
import com.yx.note_app.repositories.NoteRepository;
import com.yx.note_app.repositories.ShareNoteRepository;
import com.yx.note_app.repositories.UserRepository;
import com.yx.note_app.services.reponse.ApiResponse;
import com.yx.note_app.services.reponse.ResponseDirectory;
import com.yx.note_app.services.request.UnshareNoteRequest;
import com.yx.note_app.utils.log.DefaultLogger;
import org.springframework.beans.factory.annotation.Autowired;
import java.util.Objects;
import java.util.Optional;

@org.springframework.stereotype.Service
public class UnshareNoteService extends Service <UnshareNoteRequest, ApiResponse> {
    @Autowired
    UserRepository userRepository;

    @Autowired
    NoteRepository noteRepository;

    @Autowired
    ShareNoteRepository shareNoteRepository;

    private final DefaultLogger logger = new DefaultLogger(this.getClass());

    @Override
    public ApiResponse doService(UnshareNoteRequest request) {
        // get the user id
        Optional<User> user = userRepository.findByUsername(request.getSharedToUsername());

        if(user.isEmpty()){
            logger.log("removing a user that does not exist from a share note username: " + request.getSharedToUsername());
            return ResponseDirectory.buildFailResponse(ResponseOutcome.USER_NOT_EXIST);
        }

        // check if note exist
        Note note = noteRepository.findById((int)request.getNoteId());
        if (Objects.isNull(note)){
            logger.log("unsharing a note that does not exist: " + request.getNoteId() +" by user: " + getUserUsingTheService().getUsername());
            return ResponseDirectory.buildFailResponse(ResponseOutcome.NOTE_NOT_EXIST);
        }

        // check if action is authorized
        if (!note.getAuthor().equals(getUserUsingTheService())){
            logger.log("unauthorized action by user: " + getUserUsingTheService().getUsername());
            return ResponseDirectory.buildFailResponse(ResponseOutcome.ACTION_NOT_ALLOWED);
        }

        // get the share note to unshare
        SharedNote sharedNote = shareNoteRepository.findByNoteIdAndSharedToUserId(note.getId(), user.get().getId());

        if (Objects.isNull(sharedNote)){
            logger.log("note id: " + note.getId() + " is not shared to user: " + user.get().getUsername());
            return ResponseDirectory.buildFailResponse(ResponseOutcome.NOTE_NOT_SHARED);
        }

        // remove the share note
        shareNoteRepository.deleteById(sharedNote.getId());
        logger.log("unshare note id: " +  note.getId() + " to user: " + user.get().getUsername());
        return ResponseDirectory.buildSuccessResponse();
    }

    @Override
    public boolean paramCheck(UnshareNoteRequest request){
        return super.paramCheck(request)
                && Objects.nonNull(request.getSharedToUsername())
                && Objects.nonNull(request.getNoteId());
    }
}
