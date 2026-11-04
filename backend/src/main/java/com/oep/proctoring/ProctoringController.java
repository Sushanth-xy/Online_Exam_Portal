
package com.oep.proctoring;

import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1")
public class ProctoringController {

    private final ProctoringService proctoringService;

    public ProctoringController(ProctoringService proctoringService) {
        this.proctoringService = proctoringService;
    }

    @PostMapping("/proctoring/attempts/{attemptId}/incidents")
    @PreAuthorize("hasRole('STUDENT')")
    public ProctoringIncidentDTO reportIncident(
            @PathVariable Long attemptId,
            @RequestBody Map<String, String> request,
            Authentication authentication) {

        String typeValue = request.get("type");

        if (typeValue == null || typeValue.isBlank()) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST, "Incident type is required.");
        }

        IncidentType type;

        try {
            type = IncidentType.valueOf(typeValue.trim().toUpperCase());
        } catch (IllegalArgumentException exception) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST, "Invalid incident type.");
        }

        return proctoringService.reportIncident(
                attemptId, authentication.getName(), type);
    }

    @GetMapping("/admin/proctoring/incidents")
    @PreAuthorize("hasRole('ADMIN')")
    public List<ProctoringIncidentDTO> getAllIncidents() {
        return proctoringService.getAllIncidents();
    }

    @GetMapping("/instructor/proctoring/incidents")
    @PreAuthorize("hasAnyRole('INSTRUCTOR', 'ADMIN')")
    public List<ProctoringIncidentDTO> getInstructorIncidents(
            Authentication authentication) {
        return proctoringService.getIncidentsForInstructor(
                authentication.getName());
    }
}
