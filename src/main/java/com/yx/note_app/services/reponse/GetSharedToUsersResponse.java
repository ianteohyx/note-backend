package com.yx.note_app.services.reponse;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;

public class GetSharedToUsersResponse extends ApiResponse{
    @Schema(description = "Usernames of all users the note is shared with", example = "[\"jane_doe\", \"bob123\"]")
    private List<String> usernames;

    public List<String> getUsername() {
        return usernames;
    }

    public void setUsernames(List<String> usernames) {
        this.usernames = usernames;
    }
}
