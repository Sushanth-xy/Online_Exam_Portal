
package com.oep.proctoring;

import java.time.LocalDateTime;

public class ProctoringIncidentDTO {

    private Long id;
    private Long attemptId;
    private String examTitle;
    private String studentEmail;
    private String type;
    private LocalDateTime occurredAt;

    public ProctoringIncidentDTO(ProctoringIncident incident) {
        this.id = incident.getId();
        this.attemptId = incident.getAttempt().getId();
        this.examTitle = incident.getAttempt().getExam().getTitle();
        this.studentEmail = incident.getAttempt().getStudentEmail();
        this.type = incident.getType().name();
        this.occurredAt = incident.getOccurredAt();
    }

    public Long getId() {
        return id;
    }

    public Long getAttemptId() {
        return attemptId;
    }

    public String getExamTitle() {
        return examTitle;
    }

    public String getStudentEmail() {
        return studentEmail;
    }

    public String getType() {
        return type;
    }

    public LocalDateTime getOccurredAt() {
        return occurredAt;
    }
}
