package com.oep.grading.dto;

import com.oep.exam.AttemptStatus;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class GradingSubmissionDTO {

    private Long submissionId;
    private Long examId;
    private String examTitle;
    private String studentEmail;
    private LocalDateTime submittedAt;
    private AttemptStatus status;
    private double totalScore;
    private double maxMarks;
    private Boolean passed;
    private boolean fullyGraded;
    private int subjectiveCount;
    private int pendingSubjectiveCount;
    private List<GradingAnswerItemDTO> answers = new ArrayList<>();

    public GradingSubmissionDTO() {
    }

    public GradingSubmissionDTO(Long submissionId, Long examId, String examTitle, String studentEmail,
                                LocalDateTime submittedAt, AttemptStatus status, double totalScore,
                                double maxMarks, Boolean passed, boolean fullyGraded,
                                int subjectiveCount, int pendingSubjectiveCount,
                                List<GradingAnswerItemDTO> answers) {
        this.submissionId = submissionId;
        this.examId = examId;
        this.examTitle = examTitle;
        this.studentEmail = studentEmail;
        this.submittedAt = submittedAt;
        this.status = status;
        this.totalScore = totalScore;
        this.maxMarks = maxMarks;
        this.passed = passed;
        this.fullyGraded = fullyGraded;
        this.subjectiveCount = subjectiveCount;
        this.pendingSubjectiveCount = pendingSubjectiveCount;
        this.answers = answers != null ? answers : new ArrayList<>();
    }

    public Long getSubmissionId() {
        return submissionId;
    }

    public void setSubmissionId(Long submissionId) {
        this.submissionId = submissionId;
    }

    public Long getExamId() {
        return examId;
    }

    public void setExamId(Long examId) {
        this.examId = examId;
    }

    public String getExamTitle() {
        return examTitle;
    }

    public void setExamTitle(String examTitle) {
        this.examTitle = examTitle;
    }

    public String getStudentEmail() {
        return studentEmail;
    }

    public void setStudentEmail(String studentEmail) {
        this.studentEmail = studentEmail;
    }

    public LocalDateTime getSubmittedAt() {
        return submittedAt;
    }

    public void setSubmittedAt(LocalDateTime submittedAt) {
        this.submittedAt = submittedAt;
    }

    public AttemptStatus getStatus() {
        return status;
    }

    public void setStatus(AttemptStatus status) {
        this.status = status;
    }

    public double getTotalScore() {
        return totalScore;
    }

    public void setTotalScore(double totalScore) {
        this.totalScore = totalScore;
    }

    public double getMaxMarks() {
        return maxMarks;
    }

    public void setMaxMarks(double maxMarks) {
        this.maxMarks = maxMarks;
    }

    public Boolean getPassed() {
        return passed;
    }

    public void setPassed(Boolean passed) {
        this.passed = passed;
    }

    public boolean isFullyGraded() {
        return fullyGraded;
    }

    public void setFullyGraded(boolean fullyGraded) {
        this.fullyGraded = fullyGraded;
    }

    public int getSubjectiveCount() {
        return subjectiveCount;
    }

    public void setSubjectiveCount(int subjectiveCount) {
        this.subjectiveCount = subjectiveCount;
    }

    public int getPendingSubjectiveCount() {
        return pendingSubjectiveCount;
    }

    public void setPendingSubjectiveCount(int pendingSubjectiveCount) {
        this.pendingSubjectiveCount = pendingSubjectiveCount;
    }

    public List<GradingAnswerItemDTO> getAnswers() {
        return answers;
    }

    public void setAnswers(List<GradingAnswerItemDTO> answers) {
        this.answers = answers;
    }
}
