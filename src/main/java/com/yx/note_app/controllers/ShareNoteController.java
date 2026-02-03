package com.yx.note_app.controllers;

import com.yx.note_app.services.reponse.ApiResponse;
import com.yx.note_app.services.request.*;
import com.yx.note_app.services.service.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/shares")
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

    @PostMapping
    public ResponseEntity<ApiResponse> shareToOther(@RequestBody ShareNoteToRequest shareNoteToRequest) {
        ApiResponse response = shareNoteToOthersService.execute(shareNoteToRequest);
        HttpStatus status = response.getResponseOutcome().getSuccess()
            ? HttpStatus.CREATED
            : response.getResponseOutcome().getHttpStatus();
        return ResponseEntity.status(status).body(response);
    }

    @GetMapping("/received")
    public ResponseEntity<ApiResponse> getAllSharedToMe() {
        ApiResponse response = getAllSharedToMeService.execute(new ApiRequest());
        return ResponseEntity.status(response.getResponseOutcome().getHttpStatus()).body(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse> getSingleSharedNote(@PathVariable Integer id) {
        GetSingleSharedNoteRequest request = new GetSingleSharedNoteRequest();
        request.setSharedNoteId(id);
        ApiResponse response = getSingleSharedNoteService.execute(request);
        return ResponseEntity.status(response.getResponseOutcome().getHttpStatus()).body(response);
    }

    @PatchMapping("/{id}")
    public ResponseEntity<ApiResponse> editSharedNote(@PathVariable Integer id, @RequestBody EditShareNoteRequest request) {
        request.setShareNoteId(id);
        ApiResponse response = editSharedNoteService.execute(request);
        return ResponseEntity.status(response.getResponseOutcome().getHttpStatus()).body(response);
    }

    @GetMapping("/note/{noteId}/users")
    public ResponseEntity<ApiResponse> getSharedToUser(@PathVariable Integer noteId) {
        GetSharedToUsersRequest request = new GetSharedToUsersRequest();
        request.setId(noteId);
        ApiResponse response = getSharedToUsersService.execute(request);
        return ResponseEntity.status(response.getResponseOutcome().getHttpStatus()).body(response);
    }

    @DeleteMapping("/note/{noteId}/user/{username}")
    public ResponseEntity<ApiResponse> unshareNote(@PathVariable Integer noteId, @PathVariable String username) {
        UnshareNoteRequest request = new UnshareNoteRequest();
        request.setNoteId(noteId);
        request.setSharedToUsername(username);
        ApiResponse response = unshareNoteService.execute(request);
        return ResponseEntity.status(response.getResponseOutcome().getHttpStatus()).body(response);
    }

    @PatchMapping("/note/{noteId}/user/{username}/permission")
    public ResponseEntity<ApiResponse> updatePermission(@PathVariable Integer noteId, @PathVariable String username, @RequestBody UpdateShareNotePermissionRequest request) {
        request.setNoteId(noteId);
        request.setSharedToUsername(username);
        ApiResponse response = updateShareNotePermissionService.execute(request);
        return ResponseEntity.status(response.getResponseOutcome().getHttpStatus()).body(response);
    }

}
