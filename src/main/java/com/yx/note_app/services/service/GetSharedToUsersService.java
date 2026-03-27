package com.yx.note_app.services.service;

import com.yx.note_app.enums.ResponseOutcome;
import com.yx.note_app.exception.ResourceNotFoundException;
import com.yx.note_app.exception.UnauthorizedException;
import com.yx.note_app.models.Note;
import com.yx.note_app.models.User;
import com.yx.note_app.repositories.NoteRepository;
import com.yx.note_app.services.reponse.ApiResponse;
import com.yx.note_app.services.reponse.GetSharedToUsersResponse;
import com.yx.note_app.services.request.GetSharedToUsersRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

@org.springframework.stereotype.Service
public class GetSharedToUsersService extends Service<GetSharedToUsersRequest, ApiResponse> {

    private static final Logger logger = LoggerFactory.getLogger(GetSharedToUsersService.class);

    @Autowired
    private NoteRepository noteRepository;

    @Override
    @Transactional(readOnly = true)
    public ApiResponse doService(GetSharedToUsersRequest request) {
        Note note = noteRepository.findByIdWithSharedUsers((int)request.getNoteId());

        if (Objects.isNull(note)){
            throw ResourceNotFoundException.noteNotFound(request.getNoteId());
        }

        if (!note.getAuthor().equals(getUserUsingTheService())){
            throw UnauthorizedException.notOwner(getUserUsingTheService().getUsername());
        }

        List<String> usernames = new ArrayList<>();
        note.getSharedNotes().forEach(sharedNote -> {
            Optional.ofNullable(sharedNote.getSharedToUser())
                    .map(User::getUsername)
                    .ifPresent(usernames::add);
        });

        logger.info("User {} retrieved shared users for note {}", getUserUsingTheService().getUsername(), request.getNoteId());
        return buildSuccessSharedToUserResponse(usernames);
    }

    private GetSharedToUsersResponse buildSuccessSharedToUserResponse(List<String> usernames){
        GetSharedToUsersResponse getSharedToUsersResponse = new GetSharedToUsersResponse();
        getSharedToUsersResponse.setUsernames(usernames);
        getSharedToUsersResponse.setResponseOutcome(ResponseOutcome.SUCCESS);
        return getSharedToUsersResponse;
    }
}
