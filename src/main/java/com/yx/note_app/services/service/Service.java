package com.yx.note_app.services.service;

import com.yx.note_app.enums.Permission;
import com.yx.note_app.exception.UnauthorizedException;
import com.yx.note_app.models.Note;
import com.yx.note_app.models.SharedNote;
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

    protected void assertIsOwner(Note note) {
        if (!note.getAuthor().equals(getUserUsingTheService())) {
            throw UnauthorizedException.notOwner(getUserUsingTheService().getUsername());
        }
    }

    protected void assertIsRecipient(SharedNote sharedNote) {
        if (!sharedNote.getSharedToUser().equals(getUserUsingTheService())) {
            throw UnauthorizedException.notOwner(getUserUsingTheService().getUsername());
        }
    }

    protected void assertHasWritePermission(SharedNote sharedNote) {
        if (!sharedNote.getSharedToUser().equals(getUserUsingTheService()) || sharedNote.getPermission().equals(Permission.READ)) {
            throw UnauthorizedException.noEditPermission(getUserUsingTheService().getUsername());
        }
    }
}
