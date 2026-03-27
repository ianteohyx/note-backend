package com.yx.note_app.exception;

import com.yx.note_app.enums.ResponseOutcome;

public class DuplicateResourceException extends ApiException {

    public DuplicateResourceException(ResponseOutcome responseOutcome, String message) {
        super(responseOutcome, message);
    }

    public static DuplicateResourceException usernameExists(String username) {
        return new DuplicateResourceException(ResponseOutcome.USERNAME_EXIST, "Username already exists: " + username);
    }

    public static DuplicateResourceException noteAlreadyShared(Integer noteId, String username) {
        return new DuplicateResourceException(ResponseOutcome.NOTE_ALREADY_SHARED,
                "Note " + noteId + " is already shared to user: " + username);
    }
}