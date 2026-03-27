package com.yx.note_app.services;

import com.yx.note_app.enums.Permission;
import com.yx.note_app.exception.DuplicateResourceException;
import com.yx.note_app.exception.ResourceNotFoundException;
import com.yx.note_app.exception.UnauthorizedException;
import com.yx.note_app.models.Note;
import com.yx.note_app.models.User;
import com.yx.note_app.repositories.NoteRepository;
import com.yx.note_app.repositories.ShareNoteRepository;
import com.yx.note_app.repositories.UserRepository;
import com.yx.note_app.security.AuthenticationService;
import com.yx.note_app.services.reponse.ApiResponse;
import com.yx.note_app.services.request.ShareNoteToRequest;
import com.yx.note_app.services.service.ShareNoteToOthersService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ShareNoteToOthersServiceTest {

    @InjectMocks
    private ShareNoteToOthersService shareNoteToOthersService;

    @Mock
    private NoteRepository noteRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private ShareNoteRepository shareNoteRepository;

    @Mock
    private AuthenticationService authenticationService;

    @Test
    void doService_validShare_savesSharedNoteAndReturnsSuccess() {
        User owner = buildUser(1, "ian");
        User target = buildUser(2, "bob");
        Note note = buildNote(10, owner);
        ShareNoteToRequest request = buildRequest(10, "bob", Permission.READ);

        when(noteRepository.findById(10)).thenReturn(note);
        when(authenticationService.getCurrentUser()).thenReturn(owner);
        when(userRepository.findByUsername("bob")).thenReturn(Optional.of(target));
        when(shareNoteRepository.existsByNoteIdAndSharedToUserId(10, 2)).thenReturn(false);

        ApiResponse response = shareNoteToOthersService.doService(request);

        assertThat(response.getResponseOutcome().getSuccess()).isTrue();
        verify(shareNoteRepository).save(any());
    }

    @Test
    void doService_noteNotFound_throwsResourceNotFoundException() {
        ShareNoteToRequest request = buildRequest(99, "bob", Permission.READ);

        when(noteRepository.findById(99)).thenReturn(null);

        assertThatThrownBy(() -> shareNoteToOthersService.doService(request))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void doService_notOwner_throwsUnauthorizedException() {
        User owner = buildUser(1, "ian");
        User other = buildUser(2, "other");
        Note note = buildNote(10, owner);
        ShareNoteToRequest request = buildRequest(10, "bob", Permission.READ);

        when(noteRepository.findById(10)).thenReturn(note);
        when(authenticationService.getCurrentUser()).thenReturn(other);

        assertThatThrownBy(() -> shareNoteToOthersService.doService(request))
                .isInstanceOf(UnauthorizedException.class);
    }

    @Test
    void doService_targetUserNotFound_throwsResourceNotFoundException() {
        User owner = buildUser(1, "ian");
        Note note = buildNote(10, owner);
        ShareNoteToRequest request = buildRequest(10, "nobody", Permission.READ);

        when(noteRepository.findById(10)).thenReturn(note);
        when(authenticationService.getCurrentUser()).thenReturn(owner);
        when(userRepository.findByUsername("nobody")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> shareNoteToOthersService.doService(request))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void doService_shareToSelf_throwsResourceNotFoundException() {
        User owner = buildUser(1, "ian");
        Note note = buildNote(10, owner);
        ShareNoteToRequest request = buildRequest(10, "ian", Permission.READ);

        when(noteRepository.findById(10)).thenReturn(note);
        when(authenticationService.getCurrentUser()).thenReturn(owner);
        when(userRepository.findByUsername("ian")).thenReturn(Optional.of(owner));

        assertThatThrownBy(() -> shareNoteToOthersService.doService(request))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void doService_alreadyShared_throwsDuplicateResourceException() {
        User owner = buildUser(1, "ian");
        User target = buildUser(2, "bob");
        Note note = buildNote(10, owner);
        ShareNoteToRequest request = buildRequest(10, "bob", Permission.READ);

        when(noteRepository.findById(10)).thenReturn(note);
        when(authenticationService.getCurrentUser()).thenReturn(owner);
        when(userRepository.findByUsername("bob")).thenReturn(Optional.of(target));
        when(shareNoteRepository.existsByNoteIdAndSharedToUserId(10, 2)).thenReturn(true);

        assertThatThrownBy(() -> shareNoteToOthersService.doService(request))
                .isInstanceOf(DuplicateResourceException.class);

        verify(shareNoteRepository, never()).save(any());
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

    private ShareNoteToRequest buildRequest(int noteId, String targetUsername, Permission permission) {
        ShareNoteToRequest request = new ShareNoteToRequest();
        request.setNoteId(noteId);
        request.setSharedToUsername(targetUsername);
        request.setPermission(permission);
        return request;
    }
}
