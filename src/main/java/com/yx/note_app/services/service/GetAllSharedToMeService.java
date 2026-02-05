package com.yx.note_app.services.service;

import com.yx.note_app.dto.SharedNoteDto;
import com.yx.note_app.enums.ResponseOutcome;
import com.yx.note_app.models.SharedNote;
import com.yx.note_app.repositories.ShareNoteRepository;
import com.yx.note_app.services.reponse.GetAllSharedToMeResponse;
import com.yx.note_app.services.request.ApiRequest;
import com.yx.note_app.utils.mapper.SharedNote2SharedNoteDto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.ArrayList;
import java.util.List;

@org.springframework.stereotype.Service
public class GetAllSharedToMeService extends Service<ApiRequest, GetAllSharedToMeResponse>{

    private static final Logger logger = LoggerFactory.getLogger(GetAllSharedToMeService.class);

    @Autowired
    private ShareNoteRepository shareNoteRepository;

    @Override
    public GetAllSharedToMeResponse doService(ApiRequest request) {
        List<SharedNote> sharedNotes = shareNoteRepository.findBySharedToUser(getUserUsingTheService());
        logger.info("User {} retrieved all shared notes", getUserUsingTheService().getUsername());
        return buildSuccessGetAllSharedNotes(sharedNotes);
    }

    private GetAllSharedToMeResponse buildSuccessGetAllSharedNotes(List<SharedNote> sharedNotes){
        SharedNote2SharedNoteDto sharedNote2SharedNoteDto = new SharedNote2SharedNoteDto();
        List<SharedNoteDto> sharedNoteDto = new ArrayList<>();
        sharedNotes.forEach(sharedNote -> sharedNoteDto.add(sharedNote2SharedNoteDto.toResponse(sharedNote)));

        GetAllSharedToMeResponse getAllSharedToMeResponse = new GetAllSharedToMeResponse();
        getAllSharedToMeResponse.setSharedNotes(sharedNoteDto);
        getAllSharedToMeResponse.setResponseOutcome(ResponseOutcome.SUCCESS);
        return getAllSharedToMeResponse;
    }
}
