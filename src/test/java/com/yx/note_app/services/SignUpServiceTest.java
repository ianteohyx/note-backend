package com.yx.note_app.services;

import com.yx.note_app.exception.DuplicateResourceException;
import com.yx.note_app.repositories.UserRepository;
import com.yx.note_app.security.AuthenticationService;
import com.yx.note_app.services.reponse.ApiResponse;
import com.yx.note_app.services.request.SignUpRequest;
import com.yx.note_app.services.service.SignUpService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SignUpServiceTest {

    @InjectMocks
    private SignUpService signUpService;

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private AuthenticationService authenticationService;

    @Test
    void doService_newUsername_savesUserAndReturnsSuccess() {
        SignUpRequest request = new SignUpRequest();
        request.setUsername("newuser");
        request.setPassword("Password1!");

        when(userRepository.existsByUsername("newuser")).thenReturn(false);
        when(passwordEncoder.encode("Password1!")).thenReturn("encodedPassword");

        ApiResponse response = signUpService.doService(request);

        assertThat(response.getResponseOutcome().getSuccess()).isTrue();
        verify(userRepository).save(any());
    }

    @Test
    void doService_duplicateUsername_throwsDuplicateResourceException() {
        SignUpRequest request = new SignUpRequest();
        request.setUsername("existing");
        request.setPassword("Password1!");

        when(userRepository.existsByUsername("existing")).thenReturn(true);

        assertThatThrownBy(() -> signUpService.doService(request))
                .isInstanceOf(DuplicateResourceException.class);

        verify(userRepository, never()).save(any());
    }
}
