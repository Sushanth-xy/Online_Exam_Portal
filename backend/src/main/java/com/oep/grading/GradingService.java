package com.oep.grading;

import com.oep.exam.AttemptStatus;
import com.oep.exam.CandidateAnswer;
import com.oep.exam.Exam;
import com.oep.exam.ExamAttempt;
import com.oep.exam.ExamAttemptRepository;
import com.oep.grading.dto.GradeQuestionItem;
import com.oep.grading.dto.GradingAnswerItemDTO;
import com.oep.grading.dto.GradingSubmissionDTO;
import com.oep.grading.dto.SubmissionGradeRequest;
import com.oep.questionbank.Question;
import com.oep.questionbank.QuestionRepository;
import com.oep.questionbank.QuestionType;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class GradingService {

    private final ExamAttemptRepository examAttemptRepository;
    private final QuestionRepository questionRepository;

    public GradingService(ExamAttemptRepository examAttemptRepository,
                          QuestionRepository questionRepository) {
        this.examAttemptRepository = examAttemptRepository;
        this.questionRepository = questionRepository;
    }

    @Transactional(readOnly = true)
    public List<GradingSubmissionDTO> getSubmissionsForGrading(Long examId, Boolean unevaluatedOnly) {
        List<ExamAttempt> attempts;
        if (examId != null) {
            attempts = examAttemptRepository.findByExamId(examId);
        } else {
            attempts = examAttemptRepository.findAll();
        }

        // Only submitted/completed attempts are gradeable
        List<ExamAttempt> submittedAttempts = attempts.stream()
                .filter(a -> a.getStatus() == AttemptStatus.COMPLETED || a.getStatus() == AttemptStatus.TIMED_OUT)
                .collect(Collectors.toList());

        List<GradingSubmissionDTO> dtos = submittedAttempts.stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());

        if (Boolean.TRUE.equals(unevaluatedOnly)) {
            dtos = dtos.stream()
                    .filter(s -> !s.isFullyGraded())
                    .collect(Collectors.toList());
        }

        return dtos;
    }

    @Transactional(readOnly = true)
    public GradingSubmissionDTO getSubmissionDetail(Long attemptId) {
        ExamAttempt attempt = examAttemptRepository.findById(attemptId)
                .orElseThrow(() -> new IllegalArgumentException("Submission attempt not found with id: " + attemptId));
        return mapToDTO(attempt);
    }

    @Transactional
    public GradingSubmissionDTO gradeSubmission(Long attemptId, SubmissionGradeRequest request, String instructorEmail) {
        ExamAttempt attempt = examAttemptRepository.findById(attemptId)
                .orElseThrow(() -> new IllegalArgumentException("Submission attempt not found with id: " + attemptId));

        if (attempt.getStatus() == AttemptStatus.IN_PROGRESS) {
            throw new IllegalStateException("Cannot grade an exam attempt that is still in progress.");
        }

        List<GradeQuestionItem> grades = request.getEffectiveGrades();
        if (grades == null || grades.isEmpty()) {
            throw new IllegalArgumentException("At least one question grade must be provided.");
        }

        Exam exam = attempt.getExam();
        Map<Long, Question> questionMap = exam.getQuestions().stream()
                .collect(Collectors.toMap(Question::getId, q -> q));

        for (GradeQuestionItem item : grades) {
            if (item.getMarks() == null) {
                throw new IllegalArgumentException("Marks awarded cannot be null.");
            }
            if (item.getMarks() < 0.0) {
                throw new IllegalArgumentException("Marks awarded cannot be negative.");
            }

            CandidateAnswer targetAnswer = null;
            if (item.getAnswerId() != null) {
                targetAnswer = attempt.getAnswers().stream()
                        .filter(a -> a.getId() != null && a.getId().equals(item.getAnswerId()))
                        .findFirst()
                        .orElse(null);
            }
            if (targetAnswer == null && item.getQuestionId() != null) {
                targetAnswer = attempt.getAnswers().stream()
                        .filter(a -> a.getQuestionId().equals(item.getQuestionId()))
                        .findFirst()
                        .orElse(null);
            }

            if (targetAnswer == null) {
                throw new IllegalArgumentException("Answer for question id " + item.getQuestionId() + " not found in submission.");
            }

            Question question = questionMap.get(targetAnswer.getQuestionId());
            if (question == null) {
                question = questionRepository.findById(targetAnswer.getQuestionId()).orElse(null);
            }

            if (question != null && item.getMarks() > question.getMarks()) {
                throw new IllegalArgumentException("Marks awarded (" + item.getMarks() +
                        ") cannot exceed maximum marks for this question (" + question.getMarks() + ").");
            }

            targetAnswer.setMarksAwarded(item.getMarks());
            targetAnswer.setFeedback(item.getFeedback() != null ? item.getFeedback().trim() : null);
            targetAnswer.setEvaluated(true);
            targetAnswer.setCorrect(item.getMarks() > 0.0);
        }

        // Dynamic recalculation of aggregate score (SRS OEP-F-017 / TC-Grade-02)
        double totalScore = attempt.getAnswers().stream()
                .mapToDouble(CandidateAnswer::getMarksAwarded)
                .sum();
        totalScore = Math.max(0.0, totalScore);
        attempt.setTotalScore(totalScore);

        double examTotalMarks = exam.getTotalMarks();
        if (examTotalMarks > 0) {
            double percentage = (totalScore / examTotalMarks) * 100.0;
            attempt.setPassed(percentage >= exam.getPassPercentage());
        }

        ExamAttempt saved = examAttemptRepository.save(attempt);
        return mapToDTO(saved);
    }

    private GradingSubmissionDTO mapToDTO(ExamAttempt attempt) {
        Exam exam = attempt.getExam();
        Map<Long, Question> questionMap = exam.getQuestions().stream()
                .collect(Collectors.toMap(Question::getId, q -> q));

        List<GradingAnswerItemDTO> answerDTOs = new ArrayList<>();
        int subjectiveCount = 0;
        int pendingSubjectiveCount = 0;

        for (CandidateAnswer answer : attempt.getAnswers()) {
            Question question = questionMap.get(answer.getQuestionId());
            if (question == null) {
                question = questionRepository.findById(answer.getQuestionId()).orElse(null);
            }

            String prompt = question != null ? question.getPrompt() : "Question #" + answer.getQuestionId();
            QuestionType type = question != null ? question.getQuestionType() : QuestionType.MCQ;
            double maxMarks = question != null ? question.getMarks() : 1.0;
            String refAnswer = question != null ? question.getCorrectAnswer() : null;
            String explanation = question != null ? question.getExplanation() : null;

            if (type == QuestionType.SHORT_ANSWER) {
                subjectiveCount++;
                if (!answer.isEvaluated()) {
                    pendingSubjectiveCount++;
                }
            }

            answerDTOs.add(new GradingAnswerItemDTO(
                    answer.getId(),
                    answer.getQuestionId(),
                    prompt,
                    type,
                    maxMarks,
                    answer.getSelectedOption(),
                    answer.getMarksAwarded(),
                    answer.getFeedback(),
                    answer.isEvaluated(),
                    refAnswer,
                    explanation
            ));
        }

        boolean fullyGraded = pendingSubjectiveCount == 0;

        return new GradingSubmissionDTO(
                attempt.getId(),
                exam.getId(),
                exam.getTitle(),
                attempt.getStudentEmail(),
                attempt.getSubmittedAt(),
                attempt.getStatus(),
                attempt.getTotalScore(),
                exam.getTotalMarks(),
                attempt.getPassed(),
                fullyGraded,
                subjectiveCount,
                pendingSubjectiveCount,
                answerDTOs
        );
    }
}
