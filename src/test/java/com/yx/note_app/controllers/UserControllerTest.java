package com.yx.note_app.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.yx.note_app.enums.ResponseOutcome;
import com.yx.note_app.exception.GlobalExceptionHandler;
import com.yx.note_app.exception.InvalidCredentialsException;
import com.yx.note_app.exception.InvalidRefreshTokenException;
import com.yx.note_app.services.reponse.LoginResponse;
import com.yx.note_app.services.reponse.ResponseDirectory;
import com.yx.note_app.services.request.LoginRequest;
import com.yx.note_app.services.request.SignUpRequest;
import com.yx.note_app.services.service.LogInService;
import com.yx.note_app.services.service.RefreshTokenRequestService;
import com.yx.note_app.services.service.SignUpService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseCookie;
import com.yx.note_app.security.RefreshTokenCookieFactory;
import com.yx.note_app.security.RefreshTokenService;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;

import jakarta.servlet.http.Cookie;

import static org.hamcrest.Matchers.containsString;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class UserControllerTest {

    @InjectMocks
    private UserController userController;

    @Mock
    private SignUpService signUpService;

    @Mock
    private LogInService logInService;

    @Mock
    private RefreshTokenRequestService refreshTokenRequestService;

    @Mock
    private RefreshTokenCookieFactory refreshTokenCookieFactory;

    @Mock
    private RefreshTokenService refreshTokenService;

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        LocalValidatorFactoryBean validator = new LocalValidatorFactoryBean();
        validator.afterPropertiesSet();

        mockMvc = MockMvcBuilders.standaloneSetup(userController)
                .setValidator(validator)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
        objectMapper = new ObjectMapper();
    }

    @Test
    void signup_validRequest_returns201WithSuccess() throws Exception {
        when(signUpService.execute(any())).thenReturn(ResponseDirectory.buildSuccessResponse());

        SignUpRequest request = new SignUpRequest();
        request.setUsername("newuser");
        request.setPassword("Password1!");

        mockMvc.perform(post("/api/users/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.responseOutcome").value("SUCCESS"));
    }

    @Test
    void signup_blankUsername_returns400WithValidationError() throws Exception {
        SignUpRequest request = new SignUpRequest();
        request.setUsername("");
        request.setPassword("Password1!");

        mockMvc.perform(post("/api/users/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.responseOutcome").value("VALIDATION_ERROR"));
    }

    @Test
    void signup_weakPassword_returns400WithValidationError() throws Exception {
        SignUpRequest request = new SignUpRequest();
        request.setUsername("newuser");
        request.setPassword("weakpassword");

        mockMvc.perform(post("/api/users/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.responseOutcome").value("VALIDATION_ERROR"));
    }

    @Test
    void login_validCredentials_returnsJwtInBodyAndRefreshTokenCookie() throws Exception {
        LoginResponse loginResponse = new LoginResponse();
        loginResponse.setResponseOutcome(ResponseOutcome.SUCCESS);
        loginResponse.setToken("jwt-token");
        loginResponse.setRefreshToken("refresh-token");

        when(logInService.execute(any())).thenReturn(loginResponse);
        when(refreshTokenCookieFactory.create("refresh-token"))
                .thenReturn(ResponseCookie.from("refreshToken", "refresh-token")
                        .httpOnly(true).path("/api/users").build());

        LoginRequest request = new LoginRequest();
        request.setUsername("ian");
        request.setPassword("Password1!");

        mockMvc.perform(post("/api/users/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").value("jwt-token"))
                .andExpect(jsonPath("$.refreshToken").doesNotExist())
                .andExpect(header().string("Set-Cookie", containsString("refreshToken=refresh-token")))
                .andExpect(header().string("Set-Cookie", containsString("HttpOnly")));
    }

    @Test
    void login_invalidCredentials_returns401() throws Exception {
        when(logInService.execute(any())).thenThrow(InvalidCredentialsException.loginFailed());

        LoginRequest request = new LoginRequest();
        request.setUsername("ian");
        request.setPassword("WrongPass1!");

        mockMvc.perform(post("/api/users/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.responseOutcome").value("LOGIN_FAIL"));
    }

    @Test
    void refresh_validCookie_returnsNewJwtAndRotatedCookie() throws Exception {
        LoginResponse loginResponse = new LoginResponse();
        loginResponse.setResponseOutcome(ResponseOutcome.SUCCESS);
        loginResponse.setToken("new-jwt");
        loginResponse.setRefreshToken("new-refresh");

        when(refreshTokenRequestService.execute(any())).thenReturn(loginResponse);
        when(refreshTokenCookieFactory.create("new-refresh"))
                .thenReturn(ResponseCookie.from("refreshToken", "new-refresh")
                        .httpOnly(true).path("/api/users").build());

        mockMvc.perform(post("/api/users/refresh")
                        .cookie(new Cookie("refreshToken", "old-refresh-token")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").value("new-jwt"))
                .andExpect(jsonPath("$.refreshToken").doesNotExist())
                .andExpect(header().string("Set-Cookie", containsString("refreshToken=new-refresh")));
    }

    @Test
    void refresh_invalidCookie_returns401() throws Exception {
        when(refreshTokenRequestService.execute(any())).thenThrow(InvalidRefreshTokenException.invalid());

        mockMvc.perform(post("/api/users/refresh")
                        .cookie(new Cookie("refreshToken", "bad-token")))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.responseOutcome").value("REFRESH_TOKEN_INVALID"));
    }

    @Test
    void refresh_missingCookie_returns401() throws Exception {
        mockMvc.perform(post("/api/users/refresh"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.responseOutcome").value("REFRESH_TOKEN_INVALID"));
    }

    @Test
    void logout_withCookie_revokesTokenAndClearsCookie() throws Exception {
        when(refreshTokenCookieFactory.clear())
                .thenReturn(ResponseCookie.from("refreshToken", "")
                        .httpOnly(true).path("/api/users").maxAge(0).build());

        mockMvc.perform(post("/api/users/logout")
                        .cookie(new Cookie("refreshToken", "some-refresh-token")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.responseOutcome").value("SUCCESS"))
                .andExpect(header().string("Set-Cookie", containsString("refreshToken=;")))
                .andExpect(header().string("Set-Cookie", containsString("Max-Age=0")));

        verify(refreshTokenService).revokeRefreshToken("some-refresh-token");
    }

    @Test
    void logout_withoutCookie_returns200AndClearsCookieWithoutRevoking() throws Exception {
        when(refreshTokenCookieFactory.clear())
                .thenReturn(ResponseCookie.from("refreshToken", "")
                        .httpOnly(true).path("/api/users").maxAge(0).build());

        mockMvc.perform(post("/api/users/logout"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.responseOutcome").value("SUCCESS"))
                .andExpect(header().string("Set-Cookie", containsString("Max-Age=0")));

        verify(refreshTokenService, never()).revokeRefreshToken(any());
    }
}
