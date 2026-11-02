package com.oep.audit.dto;

import java.time.LocalDateTime;

public class ArchiveResponseDTO {

    private int archivedCount;
    private LocalDateTime cutoff;
    private String message;

    public ArchiveResponseDTO() {
    }

    public ArchiveResponseDTO(int archivedCount, LocalDateTime cutoff, String message) {
        this.archivedCount = archivedCount;
        this.cutoff = cutoff;
        this.message = message;
    }

    public int getArchivedCount() {
        return archivedCount;
    }

    public void setArchivedCount(int archivedCount) {
        this.archivedCount = archivedCount;
    }

    public LocalDateTime getCutoff() {
        return cutoff;
    }

    public void setCutoff(LocalDateTime cutoff) {
        this.cutoff = cutoff;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
