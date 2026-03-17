package com.yx.note_app.utils;

import com.yx.note_app.models.User;
import com.yx.note_app.utils.jwt.JwtUtils;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.MalformedJwtException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class JwtUtilsTest {

    // 64 bytes of 'a' encoded as Base64 — valid HS512 key (512 bits)
    private static final String TEST_SECRET =
            "YWFhYWFhYWFhYWFhYWFhYWFhYWFhYWFhYWFhYWFhYWFhYWFhYWFhYWFhYWFhYWFhYWFhYWFhYWFhYWFhYWFhYQ==";

    private JwtUtils jwtUtils;

    @BeforeEach
    void setUp() {
        jwtUtils = new JwtUtils();
        ReflectionTestUtils.setField(jwtUtils, "SECRET_KEY", TEST_SECRET);
        ReflectionTestUtils.setField(jwtUtils, "EXPIRATION_MS", 900000L);
    }

    @Test
    void generateToken_returnsNonBlankToken() {
        User user = buildUser("ian");

        String token = jwtUtils.generateToken(user);

        assertThat(token).isNotBlank();
    }

    @Test
    void getUsernameFromToken_returnsCorrectUsername() {
        User user = buildUser("ian");

        String token = jwtUtils.generateToken(user);
        String username = jwtUtils.getUsernameFromToken(token);

        assertThat(username).isEqualTo("ian");
    }

    @Test
    void validateToken_validToken_doesNotThrow() {
        User user = buildUser("ian");
        String token = jwtUtils.generateToken(user);

        // should not throw
        jwtUtils.validateToken(token);
    }

    @Test
    void validateToken_expiredToken_throwsExpiredJwtException() {
        ReflectionTestUtils.setField(jwtUtils, "EXPIRATION_MS", -1000L); // already expired
        User user = buildUser("ian");
        String expiredToken = jwtUtils.generateToken(user);

        assertThatThrownBy(() -> jwtUtils.validateToken(expiredToken))
                .isInstanceOf(ExpiredJwtException.class);
    }

    @Test
    void validateToken_tamperedToken_throwsException() {
        User user = buildUser("ian");
        String token = jwtUtils.generateToken(user);
        String tampered = token.substring(0, token.length() - 4) + "xxxx";

        assertThatThrownBy(() -> jwtUtils.validateToken(tampered))
                .isInstanceOf(Exception.class);
    }

    @Test
    void validateToken_malformedToken_throwsMalformedJwtException() {
        assertThatThrownBy(() -> jwtUtils.validateToken("not.a.valid.token"))
                .isInstanceOf(MalformedJwtException.class);
    }

    private User buildUser(String username) {
        User user = new User();
        user.setId(1);
        user.setUsername(username);
        return user;
    }
}
