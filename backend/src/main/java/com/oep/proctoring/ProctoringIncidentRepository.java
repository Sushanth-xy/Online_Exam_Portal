
package com.oep.proctoring;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface ProctoringIncidentRepository
        extends JpaRepository<ProctoringIncident, Long> {

    List<ProctoringIncident> findAllByOrderByOccurredAtDesc();

    List<ProctoringIncident> findByAttemptStudentEmailOrderByOccurredAtDesc(
            String studentEmail);

    List<ProctoringIncident> findByAttemptExamCreatedByOrderByOccurredAtDesc(
            String instructorEmail);
}
