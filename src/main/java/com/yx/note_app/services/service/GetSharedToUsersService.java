package com.yx.note_app.services.service;

import com.yx.note_app.dto.SharedUserDto;
import com.yx.note_app.enums.ResponseOutcome;
import com.yx.note_app.exception.ResourceNotFoundException;
import com.yx.note_app.models.Note;
import com.yx.note_app.repositories.NoteRepository;
import com.yx.note_app.services.reponse.GetSharedToUsersResponse;
import com.yx.note_app.services.request.GetSharedToUsersRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@org.springframework.stereotype.Service
public class GetSharedToUsersService extends Service<GetSharedToUsersRequest, GetSharedToUsersResponse> {

    private static final Logger logger = LoggerFactory.getLogger(GetSharedToUsersService.class);

    @Autowired
    private NoteRepository noteRepository;

    @Override
    @Transactional(readOnly = true)
    public GetSharedToUsersResponse doService(GetSharedToUsersRequest request) {
        Note note = noteRepository.findByIdWithSharedUsers((int)request.getNoteId());

        if (Objects.isNull(note)){
            throw ResourceNotFoundException.noteNotFound(request.getNoteId());
        }

        assertIsOwner(note);

        List<SharedUserDto> sharedUsers = new ArrayList<>();
        note.getSharedNotes().forEach(sharedNote -> {
            if (Objects.nonNull(sharedNote.getSharedToUser())) {
                SharedUserDto sharedUserDto = new SharedUserDto();
                sharedUserDto.setUsername(sharedNote.getSharedToUser().getUsername());
                sharedUserDto.setPermission(sharedNote.getPermission());
                sharedUsers.add(sharedUserDto);
            }
        });

        logger.info("User {} retrieved shared users for note {}", getUserUsingTheService().getUsername(), request.getNoteId());
        return buildSuccessSharedToUserResponse(sharedUsers);
    }

    private GetSharedToUsersResponse buildSuccessSharedToUserResponse(List<SharedUserDto> sharedUsers){
        GetSharedToUsersResponse getSharedToUsersResponse = new GetSharedToUsersResponse();
        getSharedToUsersResponse.setSharedUsers(sharedUsers);
        getSharedToUsersResponse.setResponseOutcome(ResponseOutcome.SUCCESS);
        return getSharedToUsersResponse;
    }
}
