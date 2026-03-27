package com.yx.note_app.services.service;

import com.yx.note_app.enums.ResponseOutcome;
import com.yx.note_app.exception.InvalidCredentialsException;
import com.yx.note_app.models.RefreshToken;
import com.yx.note_app.models.User;
import com.yx.note_app.repositories.UserRepository;
import com.yx.note_app.security.RefreshTokenService;
import com.yx.note_app.services.reponse.LoginResponse;
import com.yx.note_app.services.request.LoginRequest;
import com.yx.note_app.utils.jwt.JwtUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@org.springframework.stereotype.Service
public class LogInService extends Service<LoginRequest, LoginResponse>{

    private static final Logger logger = LoggerFactory.getLogger(LogInService.class);

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private JwtUtils jwtUtils;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private RefreshTokenService refreshTokenService;

    @Override
    @Transactional
    public LoginResponse doService(LoginRequest request) {
        Optional<User> actualUser = userRepository.findByUsername(request.getUsername());

        if (actualUser.isPresent() && passwordEncoder.matches(request.getPassword(), actualUser.get().getPassword())) {
            logger.info("Login successful");
            User user = actualUser.get();
            String jwt = jwtUtils.generateToken(user);
            RefreshToken refreshToken = refreshTokenService.createRefreshToken(user);
            return buildSuccessLoginResponse(jwt, refreshToken.getToken());
        }

        logger.warn("Login attempt failed - invalid credentials");
        throw InvalidCredentialsException.loginFailed();
    }

    private LoginResponse buildSuccessLoginResponse(String token, String refreshToken){
        LoginResponse loginResponse = new LoginResponse();
        loginResponse.setResponseOutcome(ResponseOutcome.SUCCESS);
        loginResponse.setToken(token);
        loginResponse.setRefreshToken(refreshToken);
        return loginResponse;
    }
}
