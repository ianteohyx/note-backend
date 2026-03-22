package com.yx.note_app.controllers;

import com.yx.note_app.config.OpenApiConfig;
import com.yx.note_app.services.request.*;
import com.yx.note_app.services.service.*;
import com.yx.note_app.services.request.GetAllNotesRequest;
import com.yx.note_app.services.reponse.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/notes")
@Tag(name = "Notes", description = "Create, read, update, and delete personal notes")
@SecurityRequirement(name = OpenApiConfig.BEARER_AUTH)
public class NoteController {
    @Autowired
    private AddNoteService addNoteService;

    @Autowired
    private GetAllNotesService getAllNotesService;

    @Autowired
    private GetSingleNoteService getSingleNoteService;

    @Autowired
    private UpdateNoteService updateNoteService;

    @Autowired
    private DeleteNoteService deleteNoteService;

    @Operation(summary = "Create a new note")
    @ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "201", description = "Note created",
            content = @Content(schema = @Schema(implementation = ApiResponse.class))),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Validation error",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Unauthorized",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @PostMapping
    public ResponseEntity<ApiResponse> createNote(@Valid @RequestBody AddNoteRequest addNoteRequest) {
        ApiResponse response = addNoteService.execute(addNoteRequest);
        HttpStatus status = response.getResponseOutcome().getSuccess()
            ? HttpStatus.CREATED
            : response.getResponseOutcome().getHttpStatus();
        return ResponseEntity.status(status).body(response);
    }

    @Operation(summary = "Get all notes belonging to the authenticated user (paginated)")
    @ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Paginated list of notes",
            content = @Content(schema = @Schema(implementation = GetAllNoteResponse.class))),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Unauthorized",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @GetMapping
    public ResponseEntity<ApiResponse> getAllNotes(
            @Parameter(description = "Page number (0-indexed)", example = "0") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Number of notes per page", example = "10") @RequestParam(defaultValue = "10") int size) {
        GetAllNotesRequest request = new GetAllNotesRequest();
        request.setPage(page);
        request.setSize(size);
        ApiResponse response = getAllNotesService.execute(request);
        return ResponseEntity.status(response.getResponseOutcome().getHttpStatus()).body(response);
    }

    @Operation(summary = "Get a single note by ID (owner only)")
    @ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Note found",
            content = @Content(schema = @Schema(implementation = GetSingleNoteResponse.class))),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Unauthorized",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Not the owner",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Note not found",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse> getNoteById(@Parameter(description = "Note ID", example = "42") @PathVariable Integer id) {
        GetSingleNoteRequest request = new GetSingleNoteRequest();
        request.setNoteId(id);
        ApiResponse response = getSingleNoteService.execute(request);
        return ResponseEntity.status(response.getResponseOutcome().getHttpStatus()).body(response);
    }

    @Operation(summary = "Update a note's title and/or content (owner only)")
    @ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Note updated",
            content = @Content(schema = @Schema(implementation = ApiResponse.class))),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Unauthorized",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Not the owner",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Note not found",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @PatchMapping("/{id}")
    public ResponseEntity<ApiResponse> updateNoteById(@Parameter(description = "Note ID", example = "42") @PathVariable Integer id, @Valid @RequestBody UpdateNoteRequest updateNoteRequest) {
        updateNoteRequest.setNoteId(id);
        ApiResponse response = updateNoteService.execute(updateNoteRequest);
        return ResponseEntity.status(response.getResponseOutcome().getHttpStatus()).body(response);
    }

    @Operation(summary = "Delete a note (owner only, cascades shared records)")
    @ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Note deleted",
            content = @Content(schema = @Schema(implementation = ApiResponse.class))),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Unauthorized",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Not the owner",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Note not found",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse> deleteNoteById(@Parameter(description = "Note ID", example = "42") @PathVariable Integer id) {
        DeleteNoteRequest request = new DeleteNoteRequest();
        request.setId(id);
        ApiResponse response = deleteNoteService.execute(request);
        return ResponseEntity.status(response.getResponseOutcome().getHttpStatus()).body(response);
    }
}
