package com.oep.exam;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface ExamAttemptRepository extends JpaRepository<ExamAttempt, Long> {
    List<ExamAttempt> findByStudentEmail(String studentEmail);
    List<ExamAttempt> findByExamId(Long examId);
    Optional<ExamAttempt> findByExamIdAndStudentEmail(Long examId, String studentEmail);
}
