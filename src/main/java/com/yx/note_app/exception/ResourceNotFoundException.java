package com.yx.note_app.exception;

import com.yx.note_app.enums.ResponseOutcome;

public class ResourceNotFoundException extends ApiException {

    public ResourceNotFoundException(String message) {
        super(ResponseOutcome.NOTE_NOT_EXIST, message);
    }

    public ResourceNotFoundException(ResponseOutcome responseOutcome, String message) {
        super(responseOutcome, message);
    }

    public static ResourceNotFoundException noteNotFound(Integer noteId) {
        return new ResourceNotFoundException(ResponseOutcome.NOTE_NOT_EXIST, "Note not found with id: " + noteId);
    }

    public static ResourceNotFoundException userNotFound(String username) {
        return new ResourceNotFoundException(ResponseOutcome.USER_NOT_EXIST, "User not found: " + username);
    }

    public static ResourceNotFoundException sharedNoteNotFound(Integer sharedNoteId) {
        return new ResourceNotFoundException(ResponseOutcome.NOTE_NOT_EXIST, "Shared note not found with id: " + sharedNoteId);
    }

    public static ResourceNotFoundException noteNotSharedToUser(Integer noteId, String username) {
        return new ResourceNotFoundException(ResponseOutcome.NOTE_NOT_SHARED, "Note " + noteId + " is not shared to user: " + username);
    }
}