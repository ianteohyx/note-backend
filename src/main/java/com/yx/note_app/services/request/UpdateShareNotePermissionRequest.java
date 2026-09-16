package com.yx.note_app.services.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;

import java.util.List;

public class UpdateShareNotePermissionRequest extends ApiRequest {
    @Schema(description = "List of note/user permission updates to apply in one request")
    @NotEmpty(message = "At least one permission update is required")
    @Valid
    private List<UpdateShareNotePermissionItem> updates;

    public List<UpdateShareNotePermissionItem> getUpdates() {
        return updates;
    }

    public void setUpdates(List<UpdateShareNotePermissionItem> updates) {
        this.updates = updates;
    }
}
