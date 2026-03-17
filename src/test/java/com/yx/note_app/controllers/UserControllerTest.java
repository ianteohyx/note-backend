package com.yx.note_app.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.yx.note_app.enums.ResponseOutcome;
import com.yx.note_app.exception.GlobalExceptionHandler;
import com.yx.note_app.exception.InvalidCredentialsException;
import com.yx.note_app.exception.InvalidRefreshTokenException;
import com.yx.note_app.services.reponse.LoginResponse;
import com.yx.note_app.services.reponse.ResponseDirectory;
import com.yx.note_app.services.request.LoginRequest;
import com.yx.note_app.services.request.RefreshTokenRequest;
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
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
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
    void login_validCredentials_returns200WithTokens() throws Exception {
        LoginResponse loginResponse = new LoginResponse();
        loginResponse.setResponseOutcome(ResponseOutcome.SUCCESS);
        loginResponse.setToken("jwt-token");
        loginResponse.setRefreshToken("refresh-token");

        when(logInService.execute(any())).thenReturn(loginResponse);

        LoginRequest request = new LoginRequest();
        request.setUsername("ian");
        request.setPassword("Password1!");

        mockMvc.perform(post("/api/users/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").value("jwt-token"))
                .andExpect(jsonPath("$.refreshToken").value("refresh-token"));
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
    void refresh_validToken_returns200WithNewTokens() throws Exception {
        LoginResponse loginResponse = new LoginResponse();
        loginResponse.setResponseOutcome(ResponseOutcome.SUCCESS);
        loginResponse.setToken("new-jwt");
        loginResponse.setRefreshToken("new-refresh");

        when(refreshTokenRequestService.execute(any())).thenReturn(loginResponse);

        RefreshTokenRequest request = new RefreshTokenRequest();
        request.setRefreshToken("old-refresh-token");

        mockMvc.perform(post("/api/users/refresh")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").value("new-jwt"))
                .andExpect(jsonPath("$.refreshToken").value("new-refresh"));
    }

    @Test
    void refresh_invalidToken_returns401() throws Exception {
        when(refreshTokenRequestService.execute(any())).thenThrow(InvalidRefreshTokenException.invalid());

        RefreshTokenRequest request = new RefreshTokenRequest();
        request.setRefreshToken("bad-token");

        mockMvc.perform(post("/api/users/refresh")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.responseOutcome").value("REFRESH_TOKEN_INVALID"));
    }
}
