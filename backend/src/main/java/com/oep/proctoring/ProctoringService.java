
package com.oep.proctoring;

import com.oep.exam.AttemptStatus;
import com.oep.exam.ExamAttempt;
import com.oep.exam.ExamAttemptRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class ProctoringService {

    private final ProctoringIncidentRepository incidentRepository;
    private final ExamAttemptRepository attemptRepository;

    public ProctoringService(
            ProctoringIncidentRepository incidentRepository,
            ExamAttemptRepository attemptRepository) {
        this.incidentRepository = incidentRepository;
        this.attemptRepository = attemptRepository;
    }

    @Transactional
    public ProctoringIncidentDTO reportIncident(
            Long attemptId, String studentEmail, IncidentType type) {

        if (type == null) {
            throw new IllegalArgumentException("Incident type is required.");
        }

        ExamAttempt attempt = attemptRepository.findById(attemptId)
                .orElseThrow(() ->
                        new IllegalArgumentException("Exam attempt not found."));

        if (!attempt.getStudentEmail().equalsIgnoreCase(studentEmail)) {
            throw new org.springframework.security.access.AccessDeniedException(
                    "You cannot report incidents for another student's attempt.");
        }

        if (attempt.getStatus() != AttemptStatus.IN_PROGRESS) {
            throw new IllegalStateException(
                    "Incidents can only be reported for an active exam attempt.");
        }

        if (type == IncidentType.TAB_SWITCH) {
            attempt.setTabSwitchCount(attempt.getTabSwitchCount() + 1);
        } else if (type == IncidentType.FULLSCREEN_EXIT) {
            attempt.setFullscreenExitCount(
                    attempt.getFullscreenExitCount() + 1);
        }

        attemptRepository.save(attempt);

        ProctoringIncident incident = new ProctoringIncident();
        incident.setAttempt(attempt);
        incident.setType(type);

        ProctoringIncident saved =
                incidentRepository.save(incident);

        return new ProctoringIncidentDTO(saved);
    }

    @Transactional(readOnly = true)
    public List<ProctoringIncidentDTO> getAllIncidents() {
        return incidentRepository.findAllByOrderByOccurredAtDesc()
                .stream()
                .map(ProctoringIncidentDTO::new)
                .toList();
    }
}
