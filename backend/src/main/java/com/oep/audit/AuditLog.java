package com.oep.audit;

import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * System Audit Trail Entity.
 * Satisfies SRS OEP-SR-004 (Audit logging of submission, grading, and auth actions)
 * and SRS OEP-NF-004 (3-year log and result retention compliance).
 */
@Entity
@Table(name = "audit_logs", indexes = {
        @Index(name = "idx_audit_timestamp", columnList = "timestamp"),
        @Index(name = "idx_audit_user_email", columnList = "userEmail"),
        @Index(name = "idx_audit_action", columnList = "action"),
        @Index(name = "idx_audit_archived", columnList = "archived")
})
public class AuditLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private LocalDateTime timestamp;

    @Column(nullable = false)
    private String userEmail;

    @Column(nullable = false)
    private String role;

    @Column(nullable = false)
    private String action;

    @Column(nullable = false)
    private String httpMethod;

    @Column(nullable = false)
    private String endpoint;

    @Column(nullable = false)
    private String ipAddress;

    @Column(nullable = false)
    private int httpStatus;

    @Column(length = 2000)
    private String details;

    @Column(nullable = false)
    private boolean archived = false;

    @Column(nullable = false)
    private LocalDateTime retentionUntil;

    public AuditLog() {
        this.timestamp = LocalDateTime.now();
        this.retentionUntil = this.timestamp.plusYears(3);
    }

    public AuditLog(String userEmail,
                    String role,
                    String action,
                    String httpMethod,
                    String endpoint,
                    String ipAddress,
                    int httpStatus,
                    String details) {
        this.timestamp = LocalDateTime.now();
        this.userEmail = (userEmail != null && !userEmail.isBlank()) ? userEmail : "anonymous";
        this.role = (role != null && !role.isBlank()) ? role : "ANONYMOUS";
        this.action = action;
        this.httpMethod = httpMethod;
        this.endpoint = endpoint;
        this.ipAddress = (ipAddress != null && !ipAddress.isBlank()) ? ipAddress : "127.0.0.1";
        this.httpStatus = httpStatus;
        this.details = details;
        this.archived = false;
        this.retentionUntil = this.timestamp.plusYears(3);
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
        if (this.retentionUntil == null) {
            this.retentionUntil = timestamp.plusYears(3);
        }
    }

    public String getUserEmail() {
        return userEmail;
    }

    public void setUserEmail(String userEmail) {
        this.userEmail = userEmail;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public String getAction() {
        return action;
    }

    public void setAction(String action) {
        this.action = action;
    }

    public String getHttpMethod() {
        return httpMethod;
    }

    public void setHttpMethod(String httpMethod) {
        this.httpMethod = httpMethod;
    }

    public String getEndpoint() {
        return endpoint;
    }

    public void setEndpoint(String endpoint) {
        this.endpoint = endpoint;
    }

    public String getIpAddress() {
        return ipAddress;
    }

    public void setIpAddress(String ipAddress) {
        this.ipAddress = ipAddress;
    }

    public int getHttpStatus() {
        return httpStatus;
    }

    public void setHttpStatus(int httpStatus) {
        this.httpStatus = httpStatus;
    }

    public String getDetails() {
        return details;
    }

    public void setDetails(String details) {
        this.details = details;
    }

    public boolean isArchived() {
        return archived;
    }

    public void setArchived(boolean archived) {
        this.archived = archived;
    }

    public LocalDateTime getRetentionUntil() {
        return retentionUntil;
    }

    public void setRetentionUntil(LocalDateTime retentionUntil) {
        this.retentionUntil = retentionUntil;
    }
}
