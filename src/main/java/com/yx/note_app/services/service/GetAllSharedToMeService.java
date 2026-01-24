package com.yx.note_app.services.service;

import com.yx.note_app.dto.SharedNoteDto;
import com.yx.note_app.enums.ResponseOutcome;
import com.yx.note_app.models.SharedNote;
import com.yx.note_app.repositories.ShareNoteRepository;
import com.yx.note_app.services.reponse.GetAllSharedToMeResponse;
import com.yx.note_app.services.request.ApiRequest;
import com.yx.note_app.utils.log.DefaultLogger;
import com.yx.note_app.utils.mapper.SharedNote2SharedNoteDto;
import org.springframework.beans.factory.annotation.Autowired;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@org.springframework.stereotype.Service
public class GetAllSharedToMeService extends Service <ApiRequest, GetAllSharedToMeResponse>{

    @Autowired
    ShareNoteRepository shareNoteRepository;

    private final DefaultLogger logger = new DefaultLogger(this.getClass());

    @Override
    public GetAllSharedToMeResponse doService(ApiRequest request) {
        List<SharedNote> sharedNotes = shareNoteRepository.findBySharedToUser(getUserUsingTheService());
        GetAllSharedToMeResponse response = buildSuccessGetAllSharedNotes(sharedNotes);
        logger.log("getting all shared note by user: " + getUserUsingTheService().getUsername());
        return response;
    }

    @Override
    public boolean paramCheck(ApiRequest request){
        return super.paramCheck(request);
    }

    public GetAllSharedToMeResponse buildSuccessGetAllSharedNotes(List<SharedNote> sharedNotes){
        // mapping shared notes to respective dto
        SharedNote2SharedNoteDto sharedNote2SharedNoteDto = new SharedNote2SharedNoteDto();
        List<SharedNoteDto> sharedNoteDto = new ArrayList<SharedNoteDto>();
        sharedNotes.forEach(sharedNote -> {
            sharedNoteDto.add(sharedNote2SharedNoteDto.toResponse(sharedNote));
        });

        // build response
        GetAllSharedToMeResponse getAllSharedToMeResponse = new GetAllSharedToMeResponse();
        getAllSharedToMeResponse.setSharedNotes(sharedNoteDto);
        getAllSharedToMeResponse.setResponseOutcome(ResponseOutcome.SUCCESS);

        return getAllSharedToMeResponse;
    }
}
