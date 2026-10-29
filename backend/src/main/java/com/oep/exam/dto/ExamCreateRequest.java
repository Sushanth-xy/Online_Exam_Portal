package com.oep.exam.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;
import java.util.List;

public class ExamCreateRequest {

    @NotBlank(message = "Exam title is required")
    private String title;

    private String description;

    @Min(value = 1, message = "Duration must be at least 1 minute")
    private int durationMinutes = 60;

    @NotNull(message = "Start time is required")
    private LocalDateTime startTime;

    @NotNull(message = "End time is required")
    private LocalDateTime endTime;

    private double passPercentage = 40.0;

    private double negativeMarkingFactor = 0.0;

    private boolean shuffleQuestions = true;

    private List<Long> questionIds;

    public ExamCreateRequest() {
    }

    public ExamCreateRequest(String title, String description, int durationMinutes,
                             LocalDateTime startTime, LocalDateTime endTime, double passPercentage,
                             double negativeMarkingFactor, boolean shuffleQuestions, List<Long> questionIds) {
        this.title = title;
        this.description = description;
        this.durationMinutes = durationMinutes;
        this.startTime = startTime;
        this.endTime = endTime;
        this.passPercentage = passPercentage;
        this.negativeMarkingFactor = negativeMarkingFactor;
        this.shuffleQuestions = shuffleQuestions;
        this.questionIds = questionIds;
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

    public LocalDateTime getStartTime() {
        return startTime;
    }

    public void setStartTime(LocalDateTime startTime) {
        this.startTime = startTime;
    }

    public LocalDateTime getEndTime() {
        return endTime;
    }

    public void setEndTime(LocalDateTime endTime) {
        this.endTime = endTime;
    }

    public double getPassPercentage() {
        return passPercentage;
    }

    public void setPassPercentage(double passPercentage) {
        this.passPercentage = passPercentage;
    }

    public double getNegativeMarkingFactor() {
        return negativeMarkingFactor;
    }

    public void setNegativeMarkingFactor(double negativeMarkingFactor) {
        this.negativeMarkingFactor = negativeMarkingFactor;
    }

    public boolean isShuffleQuestions() {
        return shuffleQuestions;
    }

    public void setShuffleQuestions(boolean shuffleQuestions) {
        this.shuffleQuestions = shuffleQuestions;
    }

    public List<Long> getQuestionIds() {
        return questionIds;
    }

    public void setQuestionIds(List<Long> questionIds) {
        this.questionIds = questionIds;
    }
}
