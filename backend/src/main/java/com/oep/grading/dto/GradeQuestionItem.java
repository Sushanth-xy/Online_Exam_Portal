package com.oep.grading.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;

public class GradeQuestionItem {

    private Long questionId;
    private Long answerId;

    @NotNull(message = "Marks awarded is required")
    @DecimalMin(value = "0.0", message = "Marks awarded cannot be negative")
    private Double marks;

    private String feedback;

    public GradeQuestionItem() {
    }

    public GradeQuestionItem(Long questionId, Double marks, String feedback) {
        this.questionId = questionId;
        this.marks = marks;
        this.feedback = feedback;
    }

    public GradeQuestionItem(Long questionId, Long answerId, Double marks, String feedback) {
        this.questionId = questionId;
        this.answerId = answerId;
        this.marks = marks;
        this.feedback = feedback;
    }

    public Long getQuestionId() {
        return questionId;
    }

    public void setQuestionId(Long questionId) {
        this.questionId = questionId;
    }

    public Long getAnswerId() {
        return answerId;
    }

    public void setAnswerId(Long answerId) {
        this.answerId = answerId;
    }

    public Double getMarks() {
        return marks;
    }

    public void setMarks(Double marks) {
        this.marks = marks;
    }

    public String getFeedback() {
        return feedback;
    }

    public void setFeedback(String feedback) {
        this.feedback = feedback;
    }
}
