package com.yx.note_app.services.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;

import java.util.List;

public class UnshareNoteRequest extends ApiRequest {
    @Schema(description = "List of note/user pairs to revoke access for in one request")
    @NotEmpty(message = "At least one unshare target is required")
    @Valid
    private List<UnshareNoteItem> unshares;

    public List<UnshareNoteItem> getUnshares() {
        return unshares;
    }

    public void setUnshares(List<UnshareNoteItem> unshares) {
        this.unshares = unshares;
    }
}
