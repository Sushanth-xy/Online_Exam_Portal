package com.oep.audit.dto;

import java.time.LocalDateTime;

/**
 * 3-Year Retention Compliance Report DTO (OEP-NF-004, TC-Ops-01).
 */
public class RetentionReportDTO {

    private long totalLogs;
    private long activeLogs;
    private long archivedLogs;
    private long logsEligibleForArchival; // logs older than 3 years
    private int requiredRetentionYears = 3;
    private LocalDateTime retentionCutoff;
    private LocalDateTime oldestLogTimestamp;
    private LocalDateTime newestLogTimestamp;
    private boolean compliant;
    private String statusMessage;

    public RetentionReportDTO() {
    }

    public RetentionReportDTO(long totalLogs,
                              long activeLogs,
                              long archivedLogs,
                              long logsEligibleForArchival,
                              LocalDateTime retentionCutoff,
                              LocalDateTime oldestLogTimestamp,
                              LocalDateTime newestLogTimestamp,
                              boolean compliant,
                              String statusMessage) {
        this.totalLogs = totalLogs;
        this.activeLogs = activeLogs;
        this.archivedLogs = archivedLogs;
        this.logsEligibleForArchival = logsEligibleForArchival;
        this.retentionCutoff = retentionCutoff;
        this.oldestLogTimestamp = oldestLogTimestamp;
        this.newestLogTimestamp = newestLogTimestamp;
        this.compliant = compliant;
        this.statusMessage = statusMessage;
    }

    public long getTotalLogs() {
        return totalLogs;
    }

    public void setTotalLogs(long totalLogs) {
        this.totalLogs = totalLogs;
    }

    public long getActiveLogs() {
        return activeLogs;
    }

    public void setActiveLogs(long activeLogs) {
        this.activeLogs = activeLogs;
    }

    public long getArchivedLogs() {
        return archivedLogs;
    }

    public void setArchivedLogs(long archivedLogs) {
        this.archivedLogs = archivedLogs;
    }

    public long getLogsEligibleForArchival() {
        return logsEligibleForArchival;
    }

    public void setLogsEligibleForArchival(long logsEligibleForArchival) {
        this.logsEligibleForArchival = logsEligibleForArchival;
    }

    public int getRequiredRetentionYears() {
        return requiredRetentionYears;
    }

    public void setRequiredRetentionYears(int requiredRetentionYears) {
        this.requiredRetentionYears = requiredRetentionYears;
    }

    public LocalDateTime getRetentionCutoff() {
        return retentionCutoff;
    }

    public void setRetentionCutoff(LocalDateTime retentionCutoff) {
        this.retentionCutoff = retentionCutoff;
    }

    public LocalDateTime getOldestLogTimestamp() {
        return oldestLogTimestamp;
    }

    public void setOldestLogTimestamp(LocalDateTime oldestLogTimestamp) {
        this.oldestLogTimestamp = oldestLogTimestamp;
    }

    public LocalDateTime getNewestLogTimestamp() {
        return newestLogTimestamp;
    }

    public void setNewestLogTimestamp(LocalDateTime newestLogTimestamp) {
        this.newestLogTimestamp = newestLogTimestamp;
    }

    public boolean isCompliant() {
        return compliant;
    }

    public void setCompliant(boolean compliant) {
        this.compliant = compliant;
    }

    public String getStatusMessage() {
        return statusMessage;
    }

    public void setStatusMessage(String statusMessage) {
        this.statusMessage = statusMessage;
    }
}
