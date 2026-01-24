package com.yx.note_app.services.service;

import com.yx.note_app.enums.ResponseOutcome;
import com.yx.note_app.models.User;
import com.yx.note_app.repositories.UserRepository;
import com.yx.note_app.services.reponse.ApiResponse;
import com.yx.note_app.services.reponse.LoginResponse;
import com.yx.note_app.services.reponse.ResponseDirectory;
import com.yx.note_app.services.request.LoginRequest;
import com.yx.note_app.utils.jwt.JwtUtils;
import com.yx.note_app.utils.log.DefaultLogger;
import org.springframework.beans.factory.annotation.Autowired;
import java.util.Objects;
import java.util.Optional;

@org.springframework.stereotype.Service
public class LogInService extends Service<LoginRequest, ApiResponse>{
    @Autowired
    private UserRepository userRepository;

    @Autowired
    private JwtUtils jwtUtils;

    private final DefaultLogger logger = new DefaultLogger(this.getClass());

    @Override
    public boolean paramCheck(LoginRequest request) {
        return super.paramCheck(request) && Objects.nonNull(request.getUsername()) && Objects.nonNull(request.getPassword());
    }

    @Override
    public ApiResponse doService(LoginRequest request) {
            Optional<User> actualUser = userRepository.findByUsername(request.getUsername());

            if (actualUser.isPresent() && request.getPassword().equals(actualUser.get().getPassword())) {
                logger.log("login success: " + request.getUsername());
                return buildSuccessLoginResponse(jwtUtils.generateToken(actualUser.get()));
            }

            logger.log("login fail: " + request.getUsername());
            return ResponseDirectory.buildFailResponse(ResponseOutcome.LOGIN_FAIL);
    }

    public LoginResponse buildSuccessLoginResponse(String token){
        LoginResponse loginResponse = new LoginResponse();
        loginResponse.setResponseOutcome(ResponseOutcome.SUCCESS);
        loginResponse.setToken(token);
        return loginResponse;
    }
}
