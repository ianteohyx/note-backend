package com.yx.note_app.services.service;

import com.yx.note_app.enums.Permission;
import com.yx.note_app.enums.ResponseOutcome;
import com.yx.note_app.models.SharedNote;
import com.yx.note_app.repositories.NoteRepository;
import com.yx.note_app.repositories.ShareNoteRepository;
import com.yx.note_app.services.reponse.ApiResponse;
import com.yx.note_app.services.reponse.ResponseDirectory;
import com.yx.note_app.services.request.EditShareNoteRequest;
import com.yx.note_app.utils.log.DefaultLogger;
import org.springframework.beans.factory.annotation.Autowired;
import java.util.Objects;

@org.springframework.stereotype.Service
public class EditSharedNoteService extends Service <EditShareNoteRequest, ApiResponse>{
    @Autowired
    ShareNoteRepository shareNoteRepository;

    @Autowired
    NoteRepository noteRepository;

    private final DefaultLogger logger = new DefaultLogger(this.getClass());

    @Override
    public ApiResponse doService(EditShareNoteRequest request) {
        int sharedNoteId = request.getShareNoteId();
        SharedNote sharedNote =  shareNoteRepository.findById(sharedNoteId);

        if(Objects.isNull(sharedNote)){
            logger.log("editing share note that doesn't exist: " + request.getShareNoteId());
            return ResponseDirectory.buildFailResponse(ResponseOutcome.NOTE_NOT_EXIST);
        }

        if(!sharedNote.getSharedToUser().equals(getUserUsingTheService()) || sharedNote.getPermission().equals(Permission.READ)){
            logger.log("user : " + getUserUsingTheService().getUsername() +" editing non permitted shared notes: " + request.getShareNoteId());
            return ResponseDirectory.buildFailResponse(ResponseOutcome.ACTION_NOT_ALLOWED);
        }

        noteRepository.updateNote(sharedNote.getNote().getId(), request.getUpdatedShareNoteTitle(), request.getUpdatedShareNoteContent());
        logger.log("updated note id: " + sharedNote.getNote().getId() + "by user: " + getUserUsingTheService());
        return ResponseDirectory.buildSuccessResponse();
    }

    @Override
    public boolean paramCheck(EditShareNoteRequest request){
        return super.paramCheck(request)
                && Objects.nonNull(request.getShareNoteId())
                && Objects.nonNull(request.getUpdatedShareNoteTitle())
                && Objects.nonNull(request.getUpdatedShareNoteContent());
    }
}
