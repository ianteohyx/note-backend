package com.yx.note_app.security;

import com.yx.note_app.exception.InvalidRefreshTokenException;
import com.yx.note_app.models.RefreshToken;
import com.yx.note_app.models.User;
import com.yx.note_app.repositories.RefreshTokenRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.Instant;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RefreshTokenServiceTest {

    @InjectMocks
    private RefreshTokenService refreshTokenService;

    @Mock
    private RefreshTokenRepository refreshTokenRepository;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(refreshTokenService, "refreshExpirationMs", 604800000L);
    }

    @Test
    void createRefreshToken_savesTokenWithCorrectFields() {
        User user = new User();
        user.setId(1);
        user.setUsername("ian");

        RefreshToken saved = new RefreshToken();
        saved.setToken("uuid-token");
        saved.setUser(user);
        when(refreshTokenRepository.save(any(RefreshToken.class))).thenAnswer(inv -> inv.getArgument(0));

        RefreshToken result = refreshTokenService.createRefreshToken(user);

        ArgumentCaptor<RefreshToken> captor = ArgumentCaptor.forClass(RefreshToken.class);
        verify(refreshTokenRepository).save(captor.capture());
        RefreshToken captured = captor.getValue();

        assertThat(captured.getUser()).isEqualTo(user);
        assertThat(captured.isRevoked()).isFalse();
        assertThat(captured.getToken()).isNotBlank();
        assertThat(captured.getExpiryDate()).isAfter(Instant.now());
    }

    @Test
    void validateRefreshToken_validToken_returnsToken() {
        RefreshToken token = buildValidToken("token-abc");

        when(refreshTokenRepository.findByToken("token-abc")).thenReturn(Optional.of(token));

        RefreshToken result = refreshTokenService.validateRefreshToken("token-abc");

        assertThat(result).isEqualTo(token);
    }

    @Test
    void validateRefreshToken_tokenNotFound_throwsInvalidRefreshTokenException() {
        when(refreshTokenRepository.findByToken("bad-token")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> refreshTokenService.validateRefreshToken("bad-token"))
                .isInstanceOf(InvalidRefreshTokenException.class);
    }

    @Test
    void validateRefreshToken_revokedToken_revokesAllUserTokensAndThrows() {
        User user = new User();
        user.setId(1);
        user.setUsername("ian");

        RefreshToken revokedToken = new RefreshToken();
        revokedToken.setToken("revoked-token");
        revokedToken.setUser(user);
        revokedToken.setRevoked(true);
        revokedToken.setExpiryDate(Instant.now().plusSeconds(3600));

        when(refreshTokenRepository.findByToken("revoked-token")).thenReturn(Optional.of(revokedToken));

        assertThatThrownBy(() -> refreshTokenService.validateRefreshToken("revoked-token"))
                .isInstanceOf(InvalidRefreshTokenException.class);

        verify(refreshTokenRepository).revokeAllByUser(user);
    }

    @Test
    void validateRefreshToken_expiredToken_throwsInvalidRefreshTokenException() {
        RefreshToken expiredToken = new RefreshToken();
        expiredToken.setToken("expired-token");
        expiredToken.setUser(new User());
        expiredToken.setRevoked(false);
        expiredToken.setExpiryDate(Instant.now().minusSeconds(3600));

        when(refreshTokenRepository.findByToken("expired-token")).thenReturn(Optional.of(expiredToken));

        assertThatThrownBy(() -> refreshTokenService.validateRefreshToken("expired-token"))
                .isInstanceOf(InvalidRefreshTokenException.class);
    }

    @Test
    void rotateRefreshToken_revokesOldAndCreatesNew() {
        User user = new User();
        user.setId(1);
        RefreshToken oldToken = buildValidToken("old-token");
        oldToken.setUser(user);

        when(refreshTokenRepository.save(any(RefreshToken.class))).thenAnswer(inv -> inv.getArgument(0));

        RefreshToken newToken = refreshTokenService.rotateRefreshToken(oldToken);

        assertThat(oldToken.isRevoked()).isTrue();
        assertThat(newToken.getToken()).isNotEqualTo("old-token");
        assertThat(newToken.isRevoked()).isFalse();
    }

    private RefreshToken buildValidToken(String tokenValue) {
        RefreshToken token = new RefreshToken();
        token.setToken(tokenValue);
        token.setRevoked(false);
        token.setExpiryDate(Instant.now().plusSeconds(3600));
        return token;
    }
}
