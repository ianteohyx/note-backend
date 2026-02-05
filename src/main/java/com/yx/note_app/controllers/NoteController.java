package com.yx.note_app.controllers;

import com.yx.note_app.services.request.*;
import com.yx.note_app.services.service.*;
import com.yx.note_app.services.reponse.ApiResponse;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/notes")
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

    @PostMapping
    public ResponseEntity<ApiResponse> createNote(@Valid @RequestBody AddNoteRequest addNoteRequest) {
        ApiResponse response = addNoteService.execute(addNoteRequest);
        HttpStatus status = response.getResponseOutcome().getSuccess()
            ? HttpStatus.CREATED
            : response.getResponseOutcome().getHttpStatus();
        return ResponseEntity.status(status).body(response);
    }

    @GetMapping
    public ResponseEntity<ApiResponse> getAllNotes() {
        ApiResponse response = getAllNotesService.execute(new ApiRequest());
        return ResponseEntity.status(response.getResponseOutcome().getHttpStatus()).body(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse> getNoteById(@PathVariable Integer id) {
        GetSingleNoteRequest request = new GetSingleNoteRequest();
        request.setNoteId(id);
        ApiResponse response = getSingleNoteService.execute(request);
        return ResponseEntity.status(response.getResponseOutcome().getHttpStatus()).body(response);
    }

    @PatchMapping("/{id}")
    public ResponseEntity<ApiResponse> updateNoteById(@PathVariable Integer id, @Valid @RequestBody UpdateNoteRequest updateNoteRequest) {
        updateNoteRequest.setNoteId(id);
        ApiResponse response = updateNoteService.execute(updateNoteRequest);
        return ResponseEntity.status(response.getResponseOutcome().getHttpStatus()).body(response);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse> deleteNoteById(@PathVariable Integer id) {
        DeleteNoteRequest request = new DeleteNoteRequest();
        request.setId(id);
        ApiResponse response = deleteNoteService.execute(request);
        return ResponseEntity.status(response.getResponseOutcome().getHttpStatus()).body(response);
    }
}
