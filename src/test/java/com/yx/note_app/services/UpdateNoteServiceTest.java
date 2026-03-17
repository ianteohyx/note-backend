package com.yx.note_app.services;

import com.yx.note_app.exception.ResourceNotFoundException;
import com.yx.note_app.exception.UnauthorizedException;
import com.yx.note_app.models.Note;
import com.yx.note_app.models.User;
import com.yx.note_app.repositories.NoteRepository;
import com.yx.note_app.security.AuthenticationService;
import com.yx.note_app.services.reponse.ApiResponse;
import com.yx.note_app.services.request.UpdateNoteRequest;
import com.yx.note_app.services.service.UpdateNoteService;
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
class UpdateNoteServiceTest {

    @InjectMocks
    private UpdateNoteService updateNoteService;

    @Mock
    private NoteRepository noteRepository;

    @Mock
    private AuthenticationService authenticationService;

    @Test
    void doService_ownerUpdatesNote_callsRepositoryAndReturnsSuccess() {
        User owner = buildUser(1, "ian");
        Note note = buildNote(10, owner);
        UpdateNoteRequest request = buildRequest(10, "New Title", "New Content");

        when(noteRepository.findById(10)).thenReturn(note);
        when(authenticationService.getCurrentUser()).thenReturn(owner);

        ApiResponse response = updateNoteService.doService(request);

        assertThat(response.getResponseOutcome().getSuccess()).isTrue();
        verify(noteRepository).updateNote(10, "New Title", "New Content");
    }

    @Test
    void doService_noteNotFound_throwsResourceNotFoundException() {
        UpdateNoteRequest request = buildRequest(99, "Title", "Content");

        when(noteRepository.findById(99)).thenReturn(null);

        assertThatThrownBy(() -> updateNoteService.doService(request))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void doService_nonOwnerUpdatesNote_throwsUnauthorizedException() {
        User owner = buildUser(1, "ian");
        User other = buildUser(2, "other");
        Note note = buildNote(10, owner);
        UpdateNoteRequest request = buildRequest(10, "Title", "Content");

        when(noteRepository.findById(10)).thenReturn(note);
        when(authenticationService.getCurrentUser()).thenReturn(other);

        assertThatThrownBy(() -> updateNoteService.doService(request))
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
        note.setTitle("Old Title");
        note.setAuthor(author);
        return note;
    }

    private UpdateNoteRequest buildRequest(int noteId, String title, String content) {
        UpdateNoteRequest request = new UpdateNoteRequest();
        request.setNoteId(noteId);
        request.setNoteTitle(title);
        request.setNoteContent(content);
        return request;
    }
}
