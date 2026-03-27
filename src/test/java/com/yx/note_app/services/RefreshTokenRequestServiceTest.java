package com.yx.note_app.services;

import com.yx.note_app.models.RefreshToken;
import com.yx.note_app.models.User;
import com.yx.note_app.security.AuthenticationService;
import com.yx.note_app.security.RefreshTokenService;
import com.yx.note_app.services.reponse.ApiResponse;
import com.yx.note_app.services.reponse.LoginResponse;
import com.yx.note_app.services.request.RefreshTokenRequest;
import com.yx.note_app.services.service.RefreshTokenRequestService;
import com.yx.note_app.utils.jwt.JwtUtils;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RefreshTokenRequestServiceTest {

    @InjectMocks
    private RefreshTokenRequestService refreshTokenRequestService;

    @Mock
    private RefreshTokenService refreshTokenService;

    @Mock
    private JwtUtils jwtUtils;

    @Mock
    private AuthenticationService authenticationService;

    @Test
    void doService_validToken_rotatesTokenAndReturnsNewJwt() {
        User user = new User();
        user.setId(1);
        user.setUsername("ian");

        RefreshToken oldToken = new RefreshToken();
        oldToken.setToken("old-token");
        oldToken.setUser(user);
        oldToken.setExpiryDate(Instant.now().plusSeconds(3600));
        oldToken.setRevoked(false);

        RefreshToken newToken = new RefreshToken();
        newToken.setToken("new-token");
        newToken.setUser(user);
        newToken.setExpiryDate(Instant.now().plusSeconds(3600));
        newToken.setRevoked(false);

        RefreshTokenRequest request = new RefreshTokenRequest();
        request.setRefreshToken("old-token");

        when(refreshTokenService.validateRefreshToken("old-token")).thenReturn(oldToken);
        when(refreshTokenService.rotateRefreshToken(oldToken)).thenReturn(newToken);
        when(jwtUtils.generateToken(user)).thenReturn("new-jwt");

        ApiResponse response = refreshTokenRequestService.doService(request);

        assertThat(response).isInstanceOf(LoginResponse.class);
        LoginResponse loginResponse = (LoginResponse) response;
        assertThat(loginResponse.getToken()).isEqualTo("new-jwt");
        assertThat(loginResponse.getRefreshToken()).isEqualTo("new-token");
        assertThat(loginResponse.getResponseOutcome().getSuccess()).isTrue();
    }
}
