package com.yx.note_app.security;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Component;

import java.time.Duration;

/**
 * Builds the HttpOnly cookie that carries the refresh token to the browser.
 * The refresh token is never returned in the response body anymore — the
 * frontend relies on the browser storing and replaying this cookie.
 */
@Component
public class RefreshTokenCookieFactory {

    public static final String COOKIE_NAME = "refreshToken";

    @Value("${jwt.refresh-cookie.path:/api/users}")
    private String cookiePath;

    @Value("${jwt.refresh-cookie.secure:true}")
    private boolean secure;

    @Value("${jwt.refresh-cookie.same-site:None}")
    private String sameSite;

    @Value("${jwt.refresh-expiration-ms:604800000}")
    private long refreshExpirationMs;

    /** Cookie that persists the refresh token for its full lifetime. */
    public ResponseCookie create(String token) {
        return ResponseCookie.from(COOKIE_NAME, token)
                .httpOnly(true)
                .secure(secure)
                .path(cookiePath)
                .sameSite(sameSite)
                .maxAge(Duration.ofMillis(refreshExpirationMs))
                .build();
    }
}
