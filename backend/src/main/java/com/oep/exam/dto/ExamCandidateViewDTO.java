package com.oep.exam.dto;

import com.oep.exam.AttemptStatus;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

public class ExamCandidateViewDTO {

    private Long examId;
    private Long attemptId;
    private String title;
    private String description;
    private int durationMinutes;
    private LocalDateTime startedAt;
    private long remainingSeconds;
    private AttemptStatus status;
    private List<QuestionForCandidateDTO> questions;
    private Map<Long, String> savedAnswers;

    public ExamCandidateViewDTO() {
    }

    public ExamCandidateViewDTO(Long examId, Long attemptId, String title, String description,
                                int durationMinutes, LocalDateTime startedAt, long remainingSeconds,
                                AttemptStatus status, List<QuestionForCandidateDTO> questions,
                                Map<Long, String> savedAnswers) {
        this.examId = examId;
        this.attemptId = attemptId;
        this.title = title;
        this.description = description;
        this.durationMinutes = durationMinutes;
        this.startedAt = startedAt;
        this.remainingSeconds = remainingSeconds;
        this.status = status;
        this.questions = questions;
        this.savedAnswers = savedAnswers;
    }

    public Long getExamId() {
        return examId;
    }

    public void setExamId(Long examId) {
        this.examId = examId;
    }

    public Long getAttemptId() {
        return attemptId;
    }

    public void setAttemptId(Long attemptId) {
        this.attemptId = attemptId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public int getDurationMinutes() {
        return durationMinutes;
    }

    public void setDurationMinutes(int durationMinutes) {
        this.durationMinutes = durationMinutes;
    }

    public LocalDateTime getStartedAt() {
        return startedAt;
    }

    public void setStartedAt(LocalDateTime startedAt) {
        this.startedAt = startedAt;
    }

    public long getRemainingSeconds() {
        return remainingSeconds;
    }

    public void setRemainingSeconds(long remainingSeconds) {
        this.remainingSeconds = remainingSeconds;
    }

    public AttemptStatus getStatus() {
        return status;
    }

    public void setStatus(AttemptStatus status) {
        this.status = status;
    }

    public List<QuestionForCandidateDTO> getQuestions() {
        return questions;
    }

    public void setQuestions(List<QuestionForCandidateDTO> questions) {
        this.questions = questions;
    }

    public Map<Long, String> getSavedAnswers() {
        return savedAnswers;
    }

    public void setSavedAnswers(Map<Long, String> savedAnswers) {
        this.savedAnswers = savedAnswers;
    }
}
