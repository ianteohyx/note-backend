package com.yx.note_app.services;

import com.yx.note_app.models.Note;
import com.yx.note_app.models.User;
import com.yx.note_app.repositories.NoteRepository;
import com.yx.note_app.security.AuthenticationService;
import com.yx.note_app.services.reponse.ApiResponse;
import com.yx.note_app.services.request.AddNoteRequest;
import com.yx.note_app.services.service.AddNoteService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AddNoteServiceTest {

    @InjectMocks
    private AddNoteService addNoteService;

    @Mock
    private NoteRepository noteRepository;

    @Mock
    private AuthenticationService authenticationService;

    @Test
    void doService_validRequest_savesNoteWithAuthorAndReturnsSuccess() {
        User currentUser = new User();
        currentUser.setId(1);
        currentUser.setUsername("ian");

        AddNoteRequest request = new AddNoteRequest();
        request.setNoteTitle("My Note");
        request.setNoteContent("Some content");

        when(authenticationService.getCurrentUser()).thenReturn(currentUser);
        when(noteRepository.save(any(Note.class))).thenAnswer(inv -> inv.getArgument(0));

        ApiResponse response = addNoteService.doService(request);

        assertThat(response.getResponseOutcome().getSuccess()).isTrue();

        ArgumentCaptor<Note> noteCaptor = ArgumentCaptor.forClass(Note.class);
        verify(noteRepository).save(noteCaptor.capture());
        Note savedNote = noteCaptor.getValue();
        assertThat(savedNote.getTitle()).isEqualTo("My Note");
        assertThat(savedNote.getContent()).isEqualTo("Some content");
        assertThat(savedNote.getAuthor()).isEqualTo(currentUser);
    }
}
