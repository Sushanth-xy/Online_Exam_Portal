package com.oep.exam;

import com.oep.exam.dto.AutosaveRequest;
import com.oep.exam.dto.ExamCandidateViewDTO;
import com.oep.exam.dto.ExamCreateRequest;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/exams")
public class ExamController {

    private final ExamService examService;

    public ExamController(ExamService examService) {
        this.examService = examService;
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('INSTRUCTOR', 'ADMIN')")
    public ResponseEntity<Exam> createExam(@Valid @RequestBody ExamCreateRequest request, Authentication auth) {
        String instructorEmail = auth != null ? auth.getName() : "instructor";
        Exam exam = examService.createExam(request, instructorEmail);
        return ResponseEntity.status(HttpStatus.CREATED).body(exam);
    }

    @GetMapping
    public ResponseEntity<List<Exam>> getAllExams() {
        return ResponseEntity.ok(examService.getAllExams());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Exam> getExamById(@PathVariable Long id) {
        return ResponseEntity.ok(examService.getExamById(id));
    }

    @PostMapping("/{id}/start")
    @PreAuthorize("hasAnyRole('STUDENT', 'ADMIN')")
    public ResponseEntity<ExamCandidateViewDTO> startExam(@PathVariable Long id, Authentication auth) {
        String studentEmail = auth != null ? auth.getName() : "student";
        ExamCandidateViewDTO view = examService.startExamAttempt(id, studentEmail);
        return ResponseEntity.ok(view);
    }

    @PostMapping("/{id}/autosave")
    @PreAuthorize("hasAnyRole('STUDENT', 'ADMIN')")
    public ResponseEntity<Map<String, Object>> autosave(
            @PathVariable Long id,
            @RequestBody AutosaveRequest request,
            Authentication auth
    ) {
        String studentEmail = auth != null ? auth.getName() : "student";
        Map<String, Object> result = examService.autosave(id, request, studentEmail);
        return ResponseEntity.ok(result);
    }

    @GetMapping("/{id}/attempt")
    @PreAuthorize("hasAnyRole('STUDENT', 'ADMIN')")
    public ResponseEntity<ExamCandidateViewDTO> getAttempt(@PathVariable Long id, Authentication auth) {
        String studentEmail = auth != null ? auth.getName() : "student";
        ExamCandidateViewDTO view = examService.getActiveAttempt(id, studentEmail);
        return ResponseEntity.ok(view);
    }

    @PostMapping("/{id}/submit")
    @PreAuthorize("hasAnyRole('STUDENT', 'ADMIN')")
    public ResponseEntity<ExamAttempt> submitExam(@PathVariable Long id, Authentication auth) {
        String studentEmail = auth != null ? auth.getName() : "student";
        ExamAttempt attempt = examService.submitExam(id, studentEmail);
        return ResponseEntity.ok(attempt);
    }
}
