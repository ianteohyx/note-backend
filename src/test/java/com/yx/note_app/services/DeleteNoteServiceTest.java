package com.yx.note_app.services;

import com.yx.note_app.exception.ResourceNotFoundException;
import com.yx.note_app.exception.UnauthorizedException;
import com.yx.note_app.models.Note;
import com.yx.note_app.models.User;
import com.yx.note_app.repositories.NoteRepository;
import com.yx.note_app.security.AuthenticationService;
import com.yx.note_app.services.reponse.ApiResponse;
import com.yx.note_app.services.request.DeleteNoteRequest;
import com.yx.note_app.services.service.DeleteNoteService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DeleteNoteServiceTest {

    @InjectMocks
    private DeleteNoteService deleteNoteService;

    @Mock
    private NoteRepository noteRepository;

    @Mock
    private AuthenticationService authenticationService;

    @Test
    void doService_ownerDeletesNote_deletesAndReturnsSuccess() {
        User owner = buildUser(1, "ian");
        Note note = buildNote(10, owner);
        DeleteNoteRequest request = buildRequest(10);

        when(noteRepository.findById(10)).thenReturn(note);
        when(authenticationService.getCurrentUser()).thenReturn(owner);

        ApiResponse response = deleteNoteService.doService(request);

        assertThat(response.getResponseOutcome().getSuccess()).isTrue();
        verify(noteRepository).deleteById(10);
    }

    @Test
    void doService_noteNotFound_throwsResourceNotFoundException() {
        DeleteNoteRequest request = buildRequest(99);

        when(noteRepository.findById(99)).thenReturn(null);

        assertThatThrownBy(() -> deleteNoteService.doService(request))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void doService_nonOwnerDeletesNote_throwsUnauthorizedException() {
        User owner = buildUser(1, "ian");
        User other = buildUser(2, "other");
        Note note = buildNote(10, owner);
        DeleteNoteRequest request = buildRequest(10);

        when(noteRepository.findById(10)).thenReturn(note);
        when(authenticationService.getCurrentUser()).thenReturn(other);

        assertThatThrownBy(() -> deleteNoteService.doService(request))
                .isInstanceOf(UnauthorizedException.class);
    }

    private User buildUser(int id, String username) {
        User user = new User();
        user.setId(id);
        user.setUsername(username);
        return user;
    }

    private Note buildNote(int id, User author) {
        Note note = new Note();
        note.setId(id);
        note.setTitle("Test Note");
        note.setAuthor(author);
        return note;
    }

    private DeleteNoteRequest buildRequest(int noteId) {
        DeleteNoteRequest request = new DeleteNoteRequest();
        request.setId(noteId);
        return request;
    }
}
