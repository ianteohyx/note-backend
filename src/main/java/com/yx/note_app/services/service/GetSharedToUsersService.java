package com.yx.note_app.services.service;

import com.yx.note_app.enums.ResponseOutcome;
import com.yx.note_app.models.Note;
import com.yx.note_app.models.User;
import com.yx.note_app.repositories.NoteRepository;
import com.yx.note_app.services.reponse.ApiResponse;
import com.yx.note_app.services.reponse.GetSharedToUsersResponse;
import com.yx.note_app.services.reponse.ResponseDirectory;
import com.yx.note_app.services.request.GetSharedToUsersRequest;
import com.yx.note_app.utils.log.DefaultLogger;
import org.springframework.beans.factory.annotation.Autowired;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

@org.springframework.stereotype.Service
public class GetSharedToUsersService extends Service<GetSharedToUsersRequest, ApiResponse> {

    @Autowired
    NoteRepository noteRepository;

    private final DefaultLogger logger = new DefaultLogger(this.getClass());

    @Override
    public ApiResponse doService(GetSharedToUsersRequest request) {
        Note note = noteRepository.findById((int)request.getNoteId());

        if (Objects.isNull(note)){
            logger.log("getting note id that does not exist: " + request.getNoteId() +" by user: " + getUserUsingTheService().getUsername());
            return ResponseDirectory.buildFailResponse(ResponseOutcome.NOTE_NOT_EXIST);
        }

        // check if action is authorized
        if (!note.getAuthor().equals(getUserUsingTheService())){
            logger.log("unauthorized action by user: " + getUserUsingTheService().getUsername());
            return ResponseDirectory.buildFailResponse(ResponseOutcome.ACTION_NOT_ALLOWED);
        }

        List<String> usernames = new ArrayList<>();
        note.getSharedNotes().forEach(sharedNote -> {
            Optional.ofNullable(sharedNote.getSharedToUser())
                    .map(User::getUsername)
                    .ifPresent(usernames::add);
        });

        return buildSuccessSharedToUserResponse(usernames);
    }

    @Override
    public boolean paramCheck(GetSharedToUsersRequest request){
        return super.paramCheck(request)
                && Objects.nonNull(request.getNoteId());
    }

    public GetSharedToUsersResponse buildSuccessSharedToUserResponse(List<String> usernames){
        GetSharedToUsersResponse getSharedToUsersResponse = new GetSharedToUsersResponse();
        getSharedToUsersResponse.setUsernames(usernames);
        getSharedToUsersResponse.setResponseOutcome(ResponseOutcome.SUCCESS);

        return getSharedToUsersResponse;
    }
}
