package com.yx.note_app.services.service;

import com.yx.note_app.dto.SharedNoteDto;
import com.yx.note_app.enums.ResponseOutcome;
import com.yx.note_app.models.SharedNote;
import com.yx.note_app.repositories.ShareNoteRepository;
import com.yx.note_app.services.reponse.ApiResponse;
import com.yx.note_app.services.reponse.GetSingleSharedNoteResponse;
import com.yx.note_app.services.reponse.ResponseDirectory;
import com.yx.note_app.services.request.GetSingleSharedNoteRequest;
import com.yx.note_app.utils.log.DefaultLogger;
import com.yx.note_app.utils.mapper.SharedNote2SharedNoteDto;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Objects;

@org.springframework.stereotype.Service
public class GetSingleSharedNoteService extends Service<GetSingleSharedNoteRequest, ApiResponse>{

    @Autowired
    ShareNoteRepository shareNoteRepository;

    private final DefaultLogger logger = new DefaultLogger(this.getClass());

    @Override
    public ApiResponse doService(GetSingleSharedNoteRequest request) {
        int sharedNoteId = request.getSharedNoteId();
        SharedNote sharedNote = shareNoteRepository.findById(sharedNoteId);

        // check if note exist
        if (Objects.isNull(sharedNote)){
            logger.log("getting share note id that does not exist: " + request.getSharedNoteId() +" by user: " + getUserUsingTheService().getUsername());
            return ResponseDirectory.buildFailResponse(ResponseOutcome.NOTE_NOT_EXIST);
        }

        // check if action is authorized
        if (!sharedNote.getSharedToUser().equals(getUserUsingTheService())){
            logger.log("unauthorized action by user: " + getUserUsingTheService().getUsername());
            return ResponseDirectory.buildFailResponse(ResponseOutcome.ACTION_NOT_ALLOWED);
        }

        logger.log("getting shared note id: " + request.getSharedNoteId() +" by user: " + getUserUsingTheService().getUsername());
        return buildSuccessGetSingleSharedNote(sharedNote);
    }

    @Override
    public boolean paramCheck(GetSingleSharedNoteRequest request){
        return super.paramCheck(request) && Objects.nonNull(request.getSharedNoteId());
    }

    public GetSingleSharedNoteResponse buildSuccessGetSingleSharedNote(SharedNote sharedNote){
        // map to DTO
        SharedNote2SharedNoteDto sharedNote2SharedNoteDto = new SharedNote2SharedNoteDto();
        SharedNoteDto sharedNoteDto = sharedNote2SharedNoteDto.toResponse(sharedNote);

        // build response
        GetSingleSharedNoteResponse getSingleSharedNoteResponse = new GetSingleSharedNoteResponse();
        getSingleSharedNoteResponse.setSharedNote(sharedNoteDto);
        getSingleSharedNoteResponse.setResponseOutcome(ResponseOutcome.SUCCESS);

        return getSingleSharedNoteResponse;
    }


}
