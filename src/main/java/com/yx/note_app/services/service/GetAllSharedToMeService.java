package com.yx.note_app.services.service;

import com.yx.note_app.dto.SharedNoteDto;
import com.yx.note_app.enums.ResponseOutcome;
import com.yx.note_app.models.SharedNote;
import com.yx.note_app.repositories.ShareNoteRepository;
import com.yx.note_app.services.reponse.GetAllSharedToMeResponse;
import com.yx.note_app.services.request.GetAllSharedToMeRequest;
import com.yx.note_app.utils.mapper.SharedNote2SharedNoteDto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;

import java.util.List;

@org.springframework.stereotype.Service
public class GetAllSharedToMeService extends Service<GetAllSharedToMeRequest, GetAllSharedToMeResponse>{

    private static final Logger logger = LoggerFactory.getLogger(GetAllSharedToMeService.class);

    @Autowired
    private ShareNoteRepository shareNoteRepository;

    @Override
    @Transactional(readOnly = true)
    public GetAllSharedToMeResponse doService(GetAllSharedToMeRequest request) {
        PageRequest pageable = PageRequest.of(request.getPage(), request.getSize(), Sort.by("id").descending());
        Page<SharedNote> sharedNotePage = shareNoteRepository.findBySharedToUser(getUserUsingTheService(), pageable);
        logger.info("User {} retrieved shared notes page {}/{}", getUserUsingTheService().getUsername(), request.getPage(), sharedNotePage.getTotalPages());
        return buildResponse(sharedNotePage, request);
    }

    private GetAllSharedToMeResponse buildResponse(Page<SharedNote> sharedNotePage, GetAllSharedToMeRequest request) {
        SharedNote2SharedNoteDto mapper = new SharedNote2SharedNoteDto();
        List<SharedNoteDto> sharedNoteDtos = sharedNotePage.getContent().stream()
                .map(mapper::toResponse)
                .toList();

        GetAllSharedToMeResponse response = new GetAllSharedToMeResponse();
        response.setSharedNotes(sharedNoteDtos);
        response.setPage(request.getPage());
        response.setSize(request.getSize());
        response.setTotalElements(sharedNotePage.getTotalElements());
        response.setTotalPages(sharedNotePage.getTotalPages());
        response.setResponseOutcome(ResponseOutcome.SUCCESS);
        return response;
    }
}
