package com.yx.note_app.services.reponse;

import com.yx.note_app.enums.ResponseOutcome;
import io.swagger.v3.oas.annotations.media.Schema;

public class ApiResponse{
    @Schema(description = "Outcome code of the operation", example = "SUCCESS")
    private ResponseOutcome responseOutcome;

    public void setResponseOutcome(ResponseOutcome responseOutcome) {
        this.responseOutcome = responseOutcome;
    }

    public ResponseOutcome getResponseOutcome() {
        return responseOutcome;
    }
}
