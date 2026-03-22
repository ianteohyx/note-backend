package com.yx.note_app.controllers;

import com.yx.note_app.config.OpenApiConfig;
import com.yx.note_app.services.reponse.*;
import com.yx.note_app.services.request.*;
import com.yx.note_app.services.request.GetAllSharedToMeRequest;
import com.yx.note_app.services.service.*;
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
@RequestMapping("/api/shares")
@Tag(name = "Shares", description = "Share notes with other users and manage permissions")
@SecurityRequirement(name = OpenApiConfig.BEARER_AUTH)
public class ShareNoteController {

    @Autowired
    ShareNoteToOthersService shareNoteToOthersService;

    @Autowired
    GetAllSharedToMeService getAllSharedToMeService;

    @Autowired
    GetSingleSharedNoteService getSingleSharedNoteService;

    @Autowired
    EditSharedNoteService editSharedNoteService;

    @Autowired
    GetSharedToUsersService getSharedToUsersService;

    @Autowired
    UnshareNoteService unshareNoteService;

    @Autowired
    UpdateShareNotePermissionService updateShareNotePermissionService;

    @Operation(summary = "Share a note with another user")
    @ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "201", description = "Note shared",
            content = @Content(schema = @Schema(implementation = ApiResponse.class))),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Not the owner",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Note or user not found",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "409", description = "Already shared with this user",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @PostMapping
    public ResponseEntity<ApiResponse> shareToOther(@Valid @RequestBody ShareNoteToRequest shareNoteToRequest) {
        ApiResponse response = shareNoteToOthersService.execute(shareNoteToRequest);
        HttpStatus status = response.getResponseOutcome().getSuccess()
            ? HttpStatus.CREATED
            : response.getResponseOutcome().getHttpStatus();
        return ResponseEntity.status(status).body(response);
    }

    @Operation(summary = "Get all notes shared to the authenticated user (paginated)")
    @ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Paginated list of shared notes",
            content = @Content(schema = @Schema(implementation = GetAllSharedToMeResponse.class))),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Unauthorized",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @GetMapping("/received")
    public ResponseEntity<ApiResponse> getAllSharedToMe(
            @Parameter(description = "Page number (0-indexed)", example = "0") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Number of shared notes per page", example = "10") @RequestParam(defaultValue = "10") int size) {
        GetAllSharedToMeRequest request = new GetAllSharedToMeRequest();
        request.setPage(page);
        request.setSize(size);
        ApiResponse response = getAllSharedToMeService.execute(request);
        return ResponseEntity.status(response.getResponseOutcome().getHttpStatus()).body(response);
    }

    @Operation(summary = "Get a single shared note by ID (recipient only)")
    @ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Shared note found",
            content = @Content(schema = @Schema(implementation = GetSingleSharedNoteResponse.class))),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Not the recipient",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Shared note not found",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse> getSingleSharedNote(@Parameter(description = "Shared note ID", example = "7") @PathVariable Integer id) {
        GetSingleSharedNoteRequest request = new GetSingleSharedNoteRequest();
        request.setSharedNoteId(id);
        ApiResponse response = getSingleSharedNoteService.execute(request);
        return ResponseEntity.status(response.getResponseOutcome().getHttpStatus()).body(response);
    }

    @Operation(summary = "Edit a shared note's content (WRITE permission only)")
    @ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Shared note updated",
            content = @Content(schema = @Schema(implementation = ApiResponse.class))),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "No WRITE permission",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Shared note not found",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @PatchMapping("/{id}")
    public ResponseEntity<ApiResponse> editSharedNote(@Parameter(description = "Shared note ID", example = "7") @PathVariable Integer id, @Valid @RequestBody EditShareNoteRequest request) {
        request.setShareNoteId(id);
        ApiResponse response = editSharedNoteService.execute(request);
        return ResponseEntity.status(response.getResponseOutcome().getHttpStatus()).body(response);
    }

    @Operation(summary = "List all users a note is shared with (owner only)")
    @ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "List of usernames",
            content = @Content(schema = @Schema(implementation = GetSharedToUsersResponse.class))),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Not the owner",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Note not found",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @GetMapping("/note/{noteId}/users")
    public ResponseEntity<ApiResponse> getSharedToUser(@Parameter(description = "Note ID", example = "42") @PathVariable Integer noteId) {
        GetSharedToUsersRequest request = new GetSharedToUsersRequest();
        request.setId(noteId);
        ApiResponse response = getSharedToUsersService.execute(request);
        return ResponseEntity.status(response.getResponseOutcome().getHttpStatus()).body(response);
    }

    @Operation(summary = "Revoke a user's access to a note (owner only)")
    @ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Access revoked",
            content = @Content(schema = @Schema(implementation = ApiResponse.class))),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Not the owner",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Note or user not found / note not shared to user",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @DeleteMapping("/note/{noteId}/user/{username}")
    public ResponseEntity<ApiResponse> unshareNote(
            @Parameter(description = "Note ID", example = "42") @PathVariable Integer noteId,
            @Parameter(description = "Username of the user to revoke access from", example = "jane_doe") @PathVariable String username) {
        UnshareNoteRequest request = new UnshareNoteRequest();
        request.setNoteId(noteId);
        request.setSharedToUsername(username);
        ApiResponse response = unshareNoteService.execute(request);
        return ResponseEntity.status(response.getResponseOutcome().getHttpStatus()).body(response);
    }

    @Operation(summary = "Change a user's permission on a shared note (owner only)")
    @ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Permission updated",
            content = @Content(schema = @Schema(implementation = ApiResponse.class))),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Not the owner",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Note not found or not shared to user",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @PatchMapping("/note/{noteId}/user/{username}/permission")
    public ResponseEntity<ApiResponse> updatePermission(
            @Parameter(description = "Note ID", example = "42") @PathVariable Integer noteId,
            @Parameter(description = "Username of the user whose permission to update", example = "jane_doe") @PathVariable String username,
            @Valid @RequestBody UpdateShareNotePermissionRequest request) {
        request.setNoteId(noteId);
        request.setSharedToUsername(username);
        ApiResponse response = updateShareNotePermissionService.execute(request);
        return ResponseEntity.status(response.getResponseOutcome().getHttpStatus()).body(response);
    }

}
