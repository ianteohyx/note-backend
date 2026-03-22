package com.yx.note_app.services.request;

import io.swagger.v3.oas.annotations.media.Schema;

public class GetAllSharedToMeRequest extends ApiRequest {

    @Schema(description = "Page number (0-indexed)", example = "0")
    private int page = 0;

    @Schema(description = "Number of shared notes per page", example = "10")
    private int size = 10;

    public int getPage() {
        return page;
    }

    public void setPage(int page) {
        this.page = page;
    }

    public int getSize() {
        return size;
    }

    public void setSize(int size) {
        this.size = size;
    }
}
