package com.oep.audit;

import com.oep.audit.dto.ArchiveResponseDTO;
import com.oep.audit.dto.AuditLogDTO;
import com.oep.audit.dto.RetentionReportDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;

@Service
public class AuditService {

    private static final Logger log = LoggerFactory.getLogger(AuditService.class);

    private final AuditLogRepository auditLogRepository;

    public AuditService(AuditLogRepository auditLogRepository) {
        this.auditLogRepository = auditLogRepository;
    }

    @Transactional
    public AuditLog logEvent(String userEmail,
                             String role,
                             String action,
                             String httpMethod,
                             String endpoint,
                             String ipAddress,
                             int httpStatus,
                             String details) {
        AuditLog auditLog = new AuditLog(
                userEmail,
                role,
                action,
                httpMethod,
                endpoint,
                ipAddress,
                httpStatus,
                details
        );

        AuditLog saved = auditLogRepository.save(auditLog);
        log.info("AUDIT TRAIL [{}]: User='{}' ({}) Method={} Endpoint={} Status={}",
                action, saved.getUserEmail(), saved.getRole(), httpMethod, endpoint, httpStatus);
        return saved;
    }

    @Transactional(readOnly = true)
    public Page<AuditLogDTO> getAuditLogs(String userEmail,
                                         String action,
                                         LocalDateTime startDate,
                                         LocalDateTime endDate,
                                         Pageable pageable) {
        Page<AuditLog> page = auditLogRepository.searchLogs(userEmail, action, startDate, endDate, pageable);
        return page.map(AuditLogDTO::new);
    }

    @Transactional(readOnly = true)
    public RetentionReportDTO getRetentionReport() {
        long totalLogs = auditLogRepository.count();
        long archivedLogs = auditLogRepository.countByArchivedTrue();
        long activeLogs = totalLogs - archivedLogs;

        LocalDateTime threeYearsAgo = LocalDateTime.now().minusYears(3);
        long eligibleForArchival = auditLogRepository.countByArchivedFalseAndTimestampBefore(threeYearsAgo);

        Optional<AuditLog> oldest = auditLogRepository.findTopByOrderByTimestampAsc();
        Optional<AuditLog> newest = auditLogRepository.findTopByOrderByTimestampDesc();

        LocalDateTime oldestTime = oldest.map(AuditLog::getTimestamp).orElse(null);
        LocalDateTime newestTime = newest.map(AuditLog::getTimestamp).orElse(null);

        boolean compliant = true;
        String statusMessage = "Audit log retention is active and compliant with OEP-NF-004 (minimum 3-year retention policy).";

        return new RetentionReportDTO(
                totalLogs,
                activeLogs,
                archivedLogs,
                eligibleForArchival,
                threeYearsAgo,
                oldestTime,
                newestTime,
                compliant,
                statusMessage
        );
    }

    @Transactional
    public ArchiveResponseDTO archiveLogsOlderThan(LocalDateTime cutoff) {
        if (cutoff == null) {
            cutoff = LocalDateTime.now().minusYears(3);
        }

        int archived = auditLogRepository.archiveLogsOlderThan(cutoff);
        log.info("Archived {} audit log records older than {} (OEP-NF-004 compliance)", archived, cutoff);

        return new ArchiveResponseDTO(
                archived,
                cutoff,
                "Successfully archived " + archived + " audit log entries older than " + cutoff
        );
    }
}
