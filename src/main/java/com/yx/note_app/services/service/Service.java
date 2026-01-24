package com.yx.note_app.services.service;

import com.yx.note_app.enums.ResponseOutcome;
import com.yx.note_app.models.User;
import com.yx.note_app.security.AuthenticationService;
import com.yx.note_app.services.reponse.ApiResponse;
import com.yx.note_app.services.reponse.ResponseDirectory;
import com.yx.note_app.services.request.ApiRequest;
import com.yx.note_app.utils.log.DefaultLogger;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Objects;

public abstract class Service<Request extends ApiRequest, Response extends ApiResponse> {
    private final DefaultLogger logger = new DefaultLogger(this.getClass());

    @Autowired
    protected AuthenticationService authenticationService;

    public abstract Response doService(Request request);

    public boolean paramCheck(Request request) {
        return Objects.nonNull(request);
    }

    public User getUserUsingTheService() {
        return authenticationService.getCurrentUser();
    }

    public ApiResponse execute(Request request) {
        if (!paramCheck(request)) {
            return ResponseDirectory.buildFailResponse(ResponseOutcome.PARAM_ILLEGAL);
        }

        try {
            return doService(request);
        } catch (Exception e) {
            System.out.println(e.getMessage());
            logger.log(e.getMessage());
            return ResponseDirectory.buildFailResponse(ResponseOutcome.PROCESS_FAIL);
        }
    }
}
