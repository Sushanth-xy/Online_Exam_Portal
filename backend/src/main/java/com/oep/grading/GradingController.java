package com.oep.grading;

import com.oep.grading.dto.GradingSubmissionDTO;
import com.oep.grading.dto.SubmissionGradeRequest;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/grading")
@PreAuthorize("hasAnyRole('INSTRUCTOR', 'ADMIN')")
public class GradingController {

    private final GradingService gradingService;

    public GradingController(GradingService gradingService) {
        this.gradingService = gradingService;
    }

    @GetMapping("/submissions")
    public ResponseEntity<List<GradingSubmissionDTO>> getSubmissions(
            @RequestParam(required = false) Long examId,
            @RequestParam(required = false, defaultValue = "false") Boolean unevaluatedOnly
    ) {
        List<GradingSubmissionDTO> submissions = gradingService.getSubmissionsForGrading(examId, unevaluatedOnly);
        return ResponseEntity.ok(submissions);
    }

    @GetMapping("/submissions/{id}")
    public ResponseEntity<GradingSubmissionDTO> getSubmissionDetail(@PathVariable Long id) {
        GradingSubmissionDTO submission = gradingService.getSubmissionDetail(id);
        return ResponseEntity.ok(submission);
    }

    @PostMapping("/submissions/{id}/grade")
    public ResponseEntity<GradingSubmissionDTO> gradeSubmission(
            @PathVariable Long id,
            @Valid @RequestBody SubmissionGradeRequest request,
            Authentication auth
    ) {
        String instructorEmail = auth != null ? auth.getName() : "instructor";
        GradingSubmissionDTO updated = gradingService.gradeSubmission(id, request, instructorEmail);
        return ResponseEntity.ok(updated);
    }
}
