package com.yx.note_app.services.service;

import com.yx.note_app.models.User;
import com.yx.note_app.security.AuthenticationService;
import com.yx.note_app.services.reponse.ApiResponse;
import com.yx.note_app.services.request.ApiRequest;
import org.springframework.beans.factory.annotation.Autowired;

public abstract class Service<Request extends ApiRequest, Response extends ApiResponse> {

    @Autowired
    protected AuthenticationService authenticationService;

    public abstract Response doService(Request request);

    public User getUserUsingTheService() {
        return authenticationService.getCurrentUser();
    }

    public Response execute(Request request) {
        // Let exceptions bubble up to GlobalExceptionHandler
        // Validation is now handled by @Valid annotations in controllers
        return doService(request);
    }
}
