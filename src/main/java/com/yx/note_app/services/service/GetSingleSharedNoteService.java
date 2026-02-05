package com.yx.note_app.services.service;

import com.yx.note_app.dto.SharedNoteDto;
import com.yx.note_app.enums.ResponseOutcome;
import com.yx.note_app.exception.ResourceNotFoundException;
import com.yx.note_app.exception.UnauthorizedException;
import com.yx.note_app.models.SharedNote;
import com.yx.note_app.repositories.ShareNoteRepository;
import com.yx.note_app.services.reponse.ApiResponse;
import com.yx.note_app.services.reponse.GetSingleSharedNoteResponse;
import com.yx.note_app.services.request.GetSingleSharedNoteRequest;
import com.yx.note_app.utils.mapper.SharedNote2SharedNoteDto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Objects;

@org.springframework.stereotype.Service
public class GetSingleSharedNoteService extends Service<GetSingleSharedNoteRequest, ApiResponse>{

    private static final Logger logger = LoggerFactory.getLogger(GetSingleSharedNoteService.class);

    @Autowired
    private ShareNoteRepository shareNoteRepository;

    @Override
    public ApiResponse doService(GetSingleSharedNoteRequest request) {
        int sharedNoteId = request.getSharedNoteId();
        SharedNote sharedNote = shareNoteRepository.findById(sharedNoteId);

        if (Objects.isNull(sharedNote)){
            throw ResourceNotFoundException.sharedNoteNotFound(sharedNoteId);
        }

        if (!sharedNote.getSharedToUser().equals(getUserUsingTheService())){
            throw UnauthorizedException.notOwner(getUserUsingTheService().getUsername());
        }

        logger.info("User {} retrieved shared note id: {}", getUserUsingTheService().getUsername(), sharedNoteId);
        return buildSuccessGetSingleSharedNote(sharedNote);
    }

    private GetSingleSharedNoteResponse buildSuccessGetSingleSharedNote(SharedNote sharedNote){
        SharedNote2SharedNoteDto sharedNote2SharedNoteDto = new SharedNote2SharedNoteDto();
        SharedNoteDto sharedNoteDto = sharedNote2SharedNoteDto.toResponse(sharedNote);

        GetSingleSharedNoteResponse getSingleSharedNoteResponse = new GetSingleSharedNoteResponse();
        getSingleSharedNoteResponse.setSharedNote(sharedNoteDto);
        getSingleSharedNoteResponse.setResponseOutcome(ResponseOutcome.SUCCESS);
        return getSingleSharedNoteResponse;
    }
}
