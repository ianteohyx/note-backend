package com.yx.note_app.services;

import com.yx.note_app.exception.InvalidCredentialsException;
import com.yx.note_app.models.RefreshToken;
import com.yx.note_app.models.User;
import com.yx.note_app.repositories.UserRepository;
import com.yx.note_app.security.AuthenticationService;
import com.yx.note_app.security.RefreshTokenService;
import com.yx.note_app.services.reponse.ApiResponse;
import com.yx.note_app.services.reponse.LoginResponse;
import com.yx.note_app.services.request.LoginRequest;
import com.yx.note_app.services.service.LogInService;
import com.yx.note_app.utils.jwt.JwtUtils;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.Instant;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class LogInServiceTest {

    @InjectMocks
    private LogInService logInService;

    @Mock
    private UserRepository userRepository;

    @Mock
    private JwtUtils jwtUtils;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private RefreshTokenService refreshTokenService;

    @Mock
    private AuthenticationService authenticationService;

    @Test
    void doService_validCredentials_returnsLoginResponseWithTokens() {
        User user = buildUser("ian", "encoded");
        LoginRequest request = buildLoginRequest("ian", "Password1!");

        RefreshToken refreshToken = new RefreshToken();
        refreshToken.setToken("refresh-uuid");
        refreshToken.setUser(user);
        refreshToken.setExpiryDate(Instant.now().plusSeconds(3600));
        refreshToken.setRevoked(false);

        when(userRepository.findByUsername("ian")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("Password1!", "encoded")).thenReturn(true);
        when(jwtUtils.generateToken(user)).thenReturn("jwt-token");
        when(refreshTokenService.createRefreshToken(user)).thenReturn(refreshToken);

        ApiResponse response = logInService.doService(request);

        assertThat(response).isInstanceOf(LoginResponse.class);
        LoginResponse loginResponse = (LoginResponse) response;
        assertThat(loginResponse.getToken()).isEqualTo("jwt-token");
        assertThat(loginResponse.getRefreshToken()).isEqualTo("refresh-uuid");
        assertThat(loginResponse.getResponseOutcome().getSuccess()).isTrue();
    }

    @Test
    void doService_wrongPassword_throwsInvalidCredentialsException() {
        User user = buildUser("ian", "encoded");
        LoginRequest request = buildLoginRequest("ian", "WrongPass1!");

        when(userRepository.findByUsername("ian")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("WrongPass1!", "encoded")).thenReturn(false);

        assertThatThrownBy(() -> logInService.doService(request))
                .isInstanceOf(InvalidCredentialsException.class);
    }

    @Test
    void doService_userNotFound_throwsInvalidCredentialsException() {
        LoginRequest request = buildLoginRequest("unknown", "Password1!");

        when(userRepository.findByUsername("unknown")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> logInService.doService(request))
                .isInstanceOf(InvalidCredentialsException.class);
    }

    private User buildUser(String username, String encodedPassword) {
        User user = new User();
        user.setId(1);
        user.setUsername(username);
        user.setPassword(encodedPassword);
        return user;
    }

    private LoginRequest buildLoginRequest(String username, String password) {
        LoginRequest request = new LoginRequest();
        request.setUsername(username);
        request.setPassword(password);
        return request;
    }
}
