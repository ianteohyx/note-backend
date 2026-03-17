package com.yx.note_app.services;

import com.yx.note_app.exception.ResourceNotFoundException;
import com.yx.note_app.exception.UnauthorizedException;
import com.yx.note_app.models.Note;
import com.yx.note_app.models.User;
import com.yx.note_app.repositories.NoteRepository;
import com.yx.note_app.security.AuthenticationService;
import com.yx.note_app.services.reponse.ApiResponse;
import com.yx.note_app.services.reponse.GetSingleNoteResponse;
import com.yx.note_app.services.request.GetSingleNoteRequest;
import com.yx.note_app.services.service.GetSingleNoteService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GetSingleNoteServiceTest {

    @InjectMocks
    private GetSingleNoteService getSingleNoteService;

    @Mock
    private NoteRepository noteRepository;

    @Mock
    private AuthenticationService authenticationService;

    @Test
    void doService_ownerRequestsNote_returnsNoteDto() {
        User owner = buildUser(1, "ian");
        Note note = buildNote(10, "Title", owner);
        GetSingleNoteRequest request = buildRequest(10);

        when(noteRepository.findById(10)).thenReturn(note);
        when(authenticationService.getCurrentUser()).thenReturn(owner);

        ApiResponse response = getSingleNoteService.doService(request);

        assertThat(response).isInstanceOf(GetSingleNoteResponse.class);
        assertThat(response.getResponseOutcome().getSuccess()).isTrue();
        GetSingleNoteResponse noteResponse = (GetSingleNoteResponse) response;
        assertThat(noteResponse.getNoteDto().getTitle()).isEqualTo("Title");
    }

    @Test
    void doService_noteNotFound_throwsResourceNotFoundException() {
        GetSingleNoteRequest request = buildRequest(99);

        when(noteRepository.findById(99)).thenReturn(null);

        assertThatThrownBy(() -> getSingleNoteService.doService(request))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void doService_nonOwnerRequestsNote_throwsUnauthorizedException() {
        User owner = buildUser(1, "ian");
        User other = buildUser(2, "other");
        Note note = buildNote(10, "Title", owner);
        GetSingleNoteRequest request = buildRequest(10);

        when(noteRepository.findById(10)).thenReturn(note);
        when(authenticationService.getCurrentUser()).thenReturn(other);

        assertThatThrownBy(() -> getSingleNoteService.doService(request))
                .isInstanceOf(UnauthorizedException.class);
    }

    private User buildUser(int id, String username) {
        User user = new User();
        user.setId(id);
        user.setUsername(username);
        return user;
    }

    private Note buildNote(int id, String title, User author) {
        Note note = new Note();
        note.setId(id);
        note.setTitle(title);
        note.setAuthor(author);
        return note;
    }

    private GetSingleNoteRequest buildRequest(int noteId) {
        GetSingleNoteRequest request = new GetSingleNoteRequest();
        request.setNoteId(noteId);
        return request;
    }
}
