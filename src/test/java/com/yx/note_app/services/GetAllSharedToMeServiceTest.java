package com.yx.note_app.services;

import com.yx.note_app.models.User;
import com.yx.note_app.repositories.ShareNoteRepository;
import com.yx.note_app.security.AuthenticationService;
import com.yx.note_app.services.reponse.GetAllSharedToMeResponse;
import com.yx.note_app.services.request.GetAllSharedToMeRequest;
import com.yx.note_app.services.service.GetAllSharedToMeService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import java.util.Collections;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GetAllSharedToMeServiceTest {

    @InjectMocks
    private GetAllSharedToMeService getAllSharedToMeService;

    @Mock
    private ShareNoteRepository shareNoteRepository;

    @Mock
    private AuthenticationService authenticationService;

    @Test
    void doService_sortsByNoteDateModifiedDescending_soLatestSharedNotesComeFirst() {
        User recipient = new User();
        recipient.setId(1);
        recipient.setUsername("bob");

        when(authenticationService.getCurrentUser()).thenReturn(recipient);
        Page<com.yx.note_app.models.SharedNote> emptyPage = new PageImpl<>(Collections.emptyList());
        when(shareNoteRepository.findBySharedToUser(any(), any())).thenReturn(emptyPage);

        GetAllSharedToMeRequest request = new GetAllSharedToMeRequest();
        request.setPage(0);
        request.setSize(10);

        GetAllSharedToMeResponse response = getAllSharedToMeService.doService(request);

        ArgumentCaptor<Pageable> pageableCaptor = ArgumentCaptor.forClass(Pageable.class);
        org.mockito.Mockito.verify(shareNoteRepository).findBySharedToUser(any(), pageableCaptor.capture());

        Sort.Order order = pageableCaptor.getValue().getSort().getOrderFor("note.dateModified");
        assertThat(order).isNotNull();
        assertThat(order.getDirection()).isEqualTo(Sort.Direction.DESC);
        assertThat(response.getResponseOutcome().getSuccess()).isTrue();
    }
}
