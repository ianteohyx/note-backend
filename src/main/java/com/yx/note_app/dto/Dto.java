package com.yx.note_app.dto;

import io.swagger.v3.oas.annotations.media.Schema;

public class Dto {
    @Schema(description = "Unique identifier", example = "42")
    private Integer id;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }
}
