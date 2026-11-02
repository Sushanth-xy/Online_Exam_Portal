package com.oep.audit;

import com.oep.audit.dto.ArchiveResponseDTO;
import com.oep.audit.dto.AuditLogDTO;
import com.oep.audit.dto.RetentionReportDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

@RestController
@RequestMapping("/api/v1/admin/audit-logs")
@PreAuthorize("hasRole('ADMIN')")
public class AuditController {

    private final AuditService auditService;

    public AuditController(AuditService auditService) {
        this.auditService = auditService;
    }

    @GetMapping
    public ResponseEntity<Page<AuditLogDTO>> getAuditLogs(
            @RequestParam(required = false) String userEmail,
            @RequestParam(required = false) String action,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        Page<AuditLogDTO> logs = auditService.getAuditLogs(userEmail, action, startDate, endDate, PageRequest.of(page, size));
        return ResponseEntity.ok(logs);
    }

    @GetMapping("/retention-report")
    public ResponseEntity<RetentionReportDTO> getRetentionReport() {
        RetentionReportDTO report = auditService.getRetentionReport();
        return ResponseEntity.ok(report);
    }

    @PostMapping("/archive")
    public ResponseEntity<ArchiveResponseDTO> archiveLogs(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime cutoff
    ) {
        ArchiveResponseDTO response = auditService.archiveLogsOlderThan(cutoff);
        return ResponseEntity.ok(response);
    }
}
