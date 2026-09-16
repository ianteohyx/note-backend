package com.yx.note_app.services.reponse;

import com.yx.note_app.dto.SharedUserDto;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;

public class GetSharedToUsersResponse extends ApiResponse{
    @Schema(description = "Users the note is shared with, along with their permission")
    private List<SharedUserDto> sharedUsers;

    public List<SharedUserDto> getSharedUsers() {
        return sharedUsers;
    }

    public void setSharedUsers(List<SharedUserDto> sharedUsers) {
        this.sharedUsers = sharedUsers;
    }
}
