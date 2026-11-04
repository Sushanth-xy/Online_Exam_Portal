package com.oep.exam;

import com.oep.exam.dto.*;
import com.oep.questionbank.Question;
import com.oep.questionbank.QuestionRepository;
import com.oep.questionbank.QuestionType;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class ExamService {

    private final ExamRepository examRepository;
    private final ExamAttemptRepository examAttemptRepository;
    private final QuestionRepository questionRepository;

    public ExamService(ExamRepository examRepository,
                       ExamAttemptRepository examAttemptRepository,
                       QuestionRepository questionRepository) {
        this.examRepository = examRepository;
        this.examAttemptRepository = examAttemptRepository;
        this.questionRepository = questionRepository;
    }

    @Transactional
    public Exam createExam(ExamCreateRequest request, String instructorEmail) {
        if (request.getStartTime().isAfter(request.getEndTime())) {
            throw new IllegalArgumentException("Exam start time cannot be after end time.");
        }

        List<Question> questions = new ArrayList<>();
        if (request.getQuestionIds() != null && !request.getQuestionIds().isEmpty()) {
            questions = questionRepository.findAllById(request.getQuestionIds());
        }

        Exam exam = new Exam(
                request.getTitle().trim(),
                request.getDescription(),
                request.getDurationMinutes(),
                request.getStartTime(),
                request.getEndTime(),
                request.getPassPercentage(),
                request.getNegativeMarkingFactor(),
                request.isShuffleQuestions(),
                instructorEmail,
                questions
        );

        return examRepository.save(exam);
    }

    public List<Exam> getAllExams() {
        return examRepository.findAll();
    }

    public Exam getExamById(Long id) {
        return examRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Exam not found with id: " + id));
    }

    // CHANGED: noRollbackFor keeps the TIMED_OUT status saved even though an error is thrown
    @Transactional(noRollbackFor = IllegalStateException.class)
    public ExamCandidateViewDTO startExamAttempt(Long examId, String studentEmail) {
        Exam exam = getExamById(examId);
        LocalDateTime now = LocalDateTime.now();

        // SRS OEP-F-014 / TC-Exam-04: Prevent starting outside scheduled window
        if (now.isBefore(exam.getStartTime())) {
            throw new IllegalStateException("Exam has not started yet. Starts at: " + exam.getStartTime());
        }
        if (now.isAfter(exam.getEndTime())) {
            throw new IllegalStateException("Exam window has closed. Ended at: " + exam.getEndTime());
        }

        Optional<ExamAttempt> existingOpt = examAttemptRepository.findByExamIdAndStudentEmail(examId, studentEmail);

        ExamAttempt attempt;
        if (existingOpt.isPresent()) {
            attempt = existingOpt.get();
            if (attempt.getStatus() == AttemptStatus.COMPLETED || attempt.getStatus() == AttemptStatus.TIMED_OUT) {
                throw new IllegalStateException("Exam attempt is already completed.");
            }
        } else {
            attempt = new ExamAttempt(exam, studentEmail);
            attempt = examAttemptRepository.save(attempt);
        }

        // Calculate remaining seconds
        LocalDateTime expiryTime = attempt.getStartedAt().plusMinutes(exam.getDurationMinutes());
        long remainingSeconds = Duration.between(now, expiryTime).getSeconds();

        if (remainingSeconds <= 0) {
            attempt.setStatus(AttemptStatus.TIMED_OUT);
            attempt.setSubmittedAt(now);
            examAttemptRepository.save(attempt);
            throw new IllegalStateException("Exam duration has expired.");
        }

        List<QuestionForCandidateDTO> candidateQuestions = buildCandidateQuestions(exam.getQuestions(), exam.isShuffleQuestions());

        Map<Long, String> savedAnswers = attempt.getAnswers().stream()
                .filter(a -> a.getSelectedOption() != null)
                .collect(Collectors.toMap(CandidateAnswer::getQuestionId, CandidateAnswer::getSelectedOption, (a, b) -> b));

        return new ExamCandidateViewDTO(
                exam.getId(),
                attempt.getId(),
                exam.getTitle(),
                exam.getDescription(),
                exam.getDurationMinutes(),
                attempt.getStartedAt(),
                remainingSeconds,
                attempt.getStatus(),
                candidateQuestions,
                savedAnswers
        );
    }

    // CHANGED: noRollbackFor keeps the TIMED_OUT status saved even though an error is thrown
    @Transactional(noRollbackFor = IllegalStateException.class)
    public Map<String, Object> autosave(Long examId, AutosaveRequest request, String studentEmail) {
        ExamAttempt attempt = examAttemptRepository.findByExamIdAndStudentEmail(examId, studentEmail)
                .orElseThrow(() -> new IllegalArgumentException("No active attempt found for exam id: " + examId));

        if (attempt.getStatus() != AttemptStatus.IN_PROGRESS) {
            throw new IllegalStateException("Cannot autosave. Exam attempt status is: " + attempt.getStatus());
        }

        LocalDateTime now = LocalDateTime.now();
        LocalDateTime expiryTime = attempt.getStartedAt().plusMinutes(attempt.getExam().getDurationMinutes());

        if (now.isAfter(expiryTime)) {
            attempt.setStatus(AttemptStatus.TIMED_OUT);
            attempt.setSubmittedAt(now);
            examAttemptRepository.save(attempt);
            throw new IllegalStateException("Exam duration expired. Attempt automatically submitted.");
        }

        if (request.getAnswers() != null) {
            Map<Long, CandidateAnswer> existingAnswersMap = attempt.getAnswers().stream()
                    .collect(Collectors.toMap(CandidateAnswer::getQuestionId, a -> a, (a, b) -> b));

            for (AutosaveAnswerItem item : request.getAnswers()) {
                CandidateAnswer candidateAnswer = existingAnswersMap.get(item.getQuestionId());
                if (candidateAnswer != null) {
                    candidateAnswer.setSelectedOption(item.getSelectedOption());
                    candidateAnswer.setLastSavedAt(now);
                } else {
                    CandidateAnswer newAnswer = new CandidateAnswer(item.getQuestionId(), item.getSelectedOption());
                    attempt.getAnswers().add(newAnswer);
                }
            }
            examAttemptRepository.save(attempt);
        }

        Map<String, Object> result = new HashMap<>();
        result.put("status", "SAVED");
        result.put("timestamp", now);
        result.put("savedCount", request.getAnswers() != null ? request.getAnswers().size() : 0);
        return result;
    }

    @Transactional
    public ExamAttempt submitExam(Long examId, String studentEmail) {
        ExamAttempt attempt = examAttemptRepository.findByExamIdAndStudentEmail(examId, studentEmail)
                .orElseThrow(() -> new IllegalArgumentException("No attempt found for exam id: " + examId));

        if (attempt.getStatus() == AttemptStatus.COMPLETED) {
            return attempt;
        }

        LocalDateTime now = LocalDateTime.now();
        attempt.setStatus(AttemptStatus.COMPLETED);
        attempt.setSubmittedAt(now);

        // Instant Auto-Grading for Objective Questions (SRS OEP-F-016 / TC-Grade-01)
        Exam exam = attempt.getExam();
        Map<Long, Question> questionMap = exam.getQuestions().stream()
                .collect(Collectors.toMap(Question::getId, q -> q));

        double totalScore = 0.0;
        double negativeFactor = exam.getNegativeMarkingFactor();

        for (CandidateAnswer answer : attempt.getAnswers()) {
            Question question = questionMap.get(answer.getQuestionId());
            if (question == null) continue;

            if (question.getQuestionType() == QuestionType.MCQ || question.getQuestionType() == QuestionType.TRUE_FALSE) {
                String candidateChoice = answer.getSelectedOption() != null ? answer.getSelectedOption().trim() : "";
                String correctChoice = question.getCorrectAnswer() != null ? question.getCorrectAnswer().trim() : "";

                if (!candidateChoice.isEmpty() && candidateChoice.equalsIgnoreCase(correctChoice)) {
                    answer.setCorrect(true);
                    answer.setMarksAwarded(question.getMarks());
                } else if (!candidateChoice.isEmpty()) {
                    answer.setCorrect(false);
                    double penalty = question.getMarks() * negativeFactor;
                    answer.setMarksAwarded(-penalty);
                } else {
                    answer.setCorrect(false);
                    answer.setMarksAwarded(0.0);
                }
                answer.setEvaluated(true);
                totalScore += answer.getMarksAwarded();
            }
        }

        // Total score cannot be negative overall
        totalScore = Math.max(0.0, totalScore);
        attempt.setTotalScore(totalScore);

        double totalMarks = exam.getTotalMarks();
        if (totalMarks > 0) {
            double percentage = (totalScore / totalMarks) * 100.0;
            attempt.setPassed(percentage >= exam.getPassPercentage());
        }

        return examAttemptRepository.save(attempt);
    }

    public ExamCandidateViewDTO getActiveAttempt(Long examId, String studentEmail) {
        return startExamAttempt(examId, studentEmail);
    }

    private List<QuestionForCandidateDTO> buildCandidateQuestions(List<Question> questions, boolean shuffle) {
        List<Question> list = new ArrayList<>(questions);
        if (shuffle) {
            Collections.shuffle(list);
        }

        return list.stream()
                .map(q -> {
                    List<String> options = new ArrayList<>(q.getOptions());
                    if (shuffle && q.getQuestionType() == QuestionType.MCQ) {
                        Collections.shuffle(options);
                    }
                    return new QuestionForCandidateDTO(
                            q.getId(),
                            q.getPrompt(),
                            q.getQuestionType(),
                            options,
                            q.getMarks()
                    );
                })
                .collect(Collectors.toList());
    }
}