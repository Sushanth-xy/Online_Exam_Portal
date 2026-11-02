package com.oep.audit;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface AuditLogRepository extends JpaRepository<AuditLog, Long> {

    Page<AuditLog> findAllByOrderByTimestampDesc(Pageable pageable);

    List<AuditLog> findByUserEmailIgnoreCaseOrderByTimestampDesc(String userEmail);

    List<AuditLog> findByActionOrderByTimestampDesc(String action);

    List<AuditLog> findByTimestampBetweenOrderByTimestampDesc(LocalDateTime start, LocalDateTime end);

    @Query("SELECT a FROM AuditLog a WHERE " +
            "(:userEmail IS NULL OR LOWER(a.userEmail) = LOWER(:userEmail)) AND " +
            "(:action IS NULL OR a.action = :action) AND " +
            "(:startDate IS NULL OR a.timestamp >= :startDate) AND " +
            "(:endDate IS NULL OR a.timestamp <= :endDate) " +
            "ORDER BY a.timestamp DESC")
    Page<AuditLog> searchLogs(@Param("userEmail") String userEmail,
                             @Param("action") String action,
                             @Param("startDate") LocalDateTime startDate,
                             @Param("endDate") LocalDateTime endDate,
                             Pageable pageable);

    // Retention queries (OEP-NF-004, TC-Ops-01)
    List<AuditLog> findByArchivedFalseAndTimestampBefore(LocalDateTime cutoff);

    long countByArchivedFalseAndTimestampBefore(LocalDateTime cutoff);

    long countByArchivedTrue();

    Optional<AuditLog> findTopByOrderByTimestampAsc();

    Optional<AuditLog> findTopByOrderByTimestampDesc();

    @Modifying
    @Query("UPDATE AuditLog a SET a.archived = true WHERE a.timestamp < :cutoff AND a.archived = false")
    int archiveLogsOlderThan(@Param("cutoff") LocalDateTime cutoff);
}
