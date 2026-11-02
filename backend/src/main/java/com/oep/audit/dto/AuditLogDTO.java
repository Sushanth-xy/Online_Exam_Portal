package com.oep.audit.dto;

import com.oep.audit.AuditLog;
import java.time.LocalDateTime;

public class AuditLogDTO {

    private Long id;
    private LocalDateTime timestamp;
    private String userEmail;
    private String role;
    private String action;
    private String httpMethod;
    private String endpoint;
    private String ipAddress;
    private int httpStatus;
    private String details;
    private boolean archived;
    private LocalDateTime retentionUntil;

    public AuditLogDTO() {
    }

    public AuditLogDTO(AuditLog log) {
        this.id = log.getId();
        this.timestamp = log.getTimestamp();
        this.userEmail = log.getUserEmail();
        this.role = log.getRole();
        this.action = log.getAction();
        this.httpMethod = log.getHttpMethod();
        this.endpoint = log.getEndpoint();
        this.ipAddress = log.getIpAddress();
        this.httpStatus = log.getHttpStatus();
        this.details = log.getDetails();
        this.archived = log.isArchived();
        this.retentionUntil = log.getRetentionUntil();
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
