package com.yx.note_app.services.service;

import com.yx.note_app.enums.ResponseOutcome;
import com.yx.note_app.models.RefreshToken;
import com.yx.note_app.security.RefreshTokenService;
import com.yx.note_app.services.reponse.ApiResponse;
import com.yx.note_app.services.reponse.LoginResponse;
import com.yx.note_app.services.request.RefreshTokenRequest;
import com.yx.note_app.utils.jwt.JwtUtils;
import org.springframework.beans.factory.annotation.Autowired;

@org.springframework.stereotype.Service
public class RefreshTokenRequestService extends Service<RefreshTokenRequest, ApiResponse> {

    @Autowired
    private RefreshTokenService refreshTokenService;

    @Autowired
    private JwtUtils jwtUtils;

    @Override
    public ApiResponse doService(RefreshTokenRequest request) {
        RefreshToken validToken = refreshTokenService.validateRefreshToken(request.getRefreshToken());
        RefreshToken newToken = refreshTokenService.rotateRefreshToken(validToken);
        String newJwt = jwtUtils.generateToken(newToken.getUser());

        LoginResponse response = new LoginResponse();
        response.setResponseOutcome(ResponseOutcome.SUCCESS);
        response.setToken(newJwt);
        response.setRefreshToken(newToken.getToken());
        return response;
    }
}
