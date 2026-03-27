package com.yx.note_app.services.reponse;

import com.yx.note_app.dto.NoteDto;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;

public class GetAllNoteResponse extends ApiResponse {
    @Schema(description = "List of notes on the current page")
    private List<NoteDto> notes;

    @Schema(description = "Current page number (0-indexed)", example = "0")
    private int page;

    @Schema(description = "Number of notes per page", example = "10")
    private int size;

    @Schema(description = "Total number of notes across all pages", example = "42")
    private long totalElements;

    @Schema(description = "Total number of pages", example = "5")
    private int totalPages;

    public List<NoteDto> getNotes() {
        return notes;
    }

    public void setNotes(List<NoteDto> notes) {
        this.notes = notes;
    }

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

    public long getTotalElements() {
        return totalElements;
    }

    public void setTotalElements(long totalElements) {
        this.totalElements = totalElements;
    }

    public int getTotalPages() {
        return totalPages;
    }

    public void setTotalPages(int totalPages) {
        this.totalPages = totalPages;
    }
}
