package com.yx.note_app.services.reponse;

import com.yx.note_app.dto.SharedNoteDto;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;

public class GetAllSharedToMeResponse extends ApiResponse{
    @Schema(description = "List of shared notes on the current page")
    List<SharedNoteDto> sharedNotes;

    @Schema(description = "Current page number (0-indexed)", example = "0")
    private int page;

    @Schema(description = "Number of shared notes per page", example = "10")
    private int size;

    @Schema(description = "Total number of shared notes across all pages", example = "25")
    private long totalElements;

    @Schema(description = "Total number of pages", example = "3")
    private int totalPages;

    public List<SharedNoteDto> getSharedNotes() {
        return sharedNotes;
    }

    public void setSharedNotes(List<SharedNoteDto> sharedNotes) {
        this.sharedNotes = sharedNotes;
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
