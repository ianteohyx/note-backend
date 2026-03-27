package com.yx.note_app.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.yx.note_app.dto.NoteDto;
import com.yx.note_app.exception.GlobalExceptionHandler;
import com.yx.note_app.exception.ResourceNotFoundException;
import com.yx.note_app.exception.UnauthorizedException;
import com.yx.note_app.services.reponse.GetAllNoteResponse;
import com.yx.note_app.services.reponse.GetSingleNoteResponse;
import com.yx.note_app.services.reponse.ResponseDirectory;
import com.yx.note_app.services.request.AddNoteRequest;
import com.yx.note_app.services.request.UpdateNoteRequest;
import com.yx.note_app.services.service.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class NoteControllerTest {

    @InjectMocks
    private NoteController noteController;

    @Mock
    private AddNoteService addNoteService;

    @Mock
    private GetAllNotesService getAllNotesService;

    @Mock
    private GetSingleNoteService getSingleNoteService;

    @Mock
    private UpdateNoteService updateNoteService;

    @Mock
    private DeleteNoteService deleteNoteService;

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        LocalValidatorFactoryBean validator = new LocalValidatorFactoryBean();
        validator.afterPropertiesSet();

        mockMvc = MockMvcBuilders.standaloneSetup(noteController)
                .setValidator(validator)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
        objectMapper = new ObjectMapper();
    }

    @Test
    void createNote_validRequest_returns201() throws Exception {
        when(addNoteService.execute(any())).thenReturn(ResponseDirectory.buildSuccessResponse());

        AddNoteRequest request = new AddNoteRequest();
        request.setNoteTitle("My Note");
        request.setNoteContent("Some content");

        mockMvc.perform(post("/api/notes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.responseOutcome").value("SUCCESS"));
    }

    @Test
    void createNote_blankTitle_returns400() throws Exception {
        AddNoteRequest request = new AddNoteRequest();
        request.setNoteTitle("");
        request.setNoteContent("Some content");

        mockMvc.perform(post("/api/notes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.responseOutcome").value("VALIDATION_ERROR"));
    }

    @Test
    void getAllNotes_returns200WithNoteList() throws Exception {
        GetAllNoteResponse response = new GetAllNoteResponse();
        response.setResponseOutcome(com.yx.note_app.enums.ResponseOutcome.SUCCESS);
        response.setNotes(List.of());

        when(getAllNotesService.execute(any())).thenReturn(response);

        mockMvc.perform(get("/api/notes"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.responseOutcome").value("SUCCESS"));
    }

    @Test
    void getNoteById_noteExists_returns200() throws Exception {
        NoteDto noteDto = new NoteDto();
        noteDto.setId(1);
        noteDto.setTitle("Test Note");

        GetSingleNoteResponse response = new GetSingleNoteResponse();
        response.setResponseOutcome(com.yx.note_app.enums.ResponseOutcome.SUCCESS);
        response.setNoteDto(noteDto);

        when(getSingleNoteService.execute(any())).thenReturn(response);

        mockMvc.perform(get("/api/notes/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.responseOutcome").value("SUCCESS"))
                .andExpect(jsonPath("$.noteDto.title").value("Test Note"));
    }

    @Test
    void getNoteById_noteNotFound_returns404() throws Exception {
        when(getSingleNoteService.execute(any())).thenThrow(ResourceNotFoundException.noteNotFound(99));

        mockMvc.perform(get("/api/notes/99"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.responseOutcome").value("NOTE_NOT_EXIST"));
    }

    @Test
    void updateNote_validRequest_returns200() throws Exception {
        when(updateNoteService.execute(any())).thenReturn(ResponseDirectory.buildSuccessResponse());

        UpdateNoteRequest request = new UpdateNoteRequest();
        request.setNoteTitle("Updated Title");
        request.setNoteContent("Updated Content");

        mockMvc.perform(patch("/api/notes/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.responseOutcome").value("SUCCESS"));
    }

    @Test
    void updateNote_notOwner_returns403() throws Exception {
        when(updateNoteService.execute(any())).thenThrow(UnauthorizedException.notOwner("other"));

        UpdateNoteRequest request = new UpdateNoteRequest();
        request.setNoteTitle("Updated Title");

        mockMvc.perform(patch("/api/notes/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.responseOutcome").value("ACTION_NOT_ALLOWED"));
    }

    @Test
    void deleteNote_validRequest_returns200() throws Exception {
        when(deleteNoteService.execute(any())).thenReturn(ResponseDirectory.buildSuccessResponse());

        mockMvc.perform(delete("/api/notes/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.responseOutcome").value("SUCCESS"));
    }

    @Test
    void deleteNote_noteNotFound_returns404() throws Exception {
        when(deleteNoteService.execute(any())).thenThrow(ResourceNotFoundException.noteNotFound(1));

        mockMvc.perform(delete("/api/notes/1"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.responseOutcome").value("NOTE_NOT_EXIST"));
    }
}
