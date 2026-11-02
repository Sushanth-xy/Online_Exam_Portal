package com.oep.grading.dto;

import java.util.ArrayList;
import java.util.List;

public class SubmissionGradeRequest {

    private List<GradeQuestionItem> grades = new ArrayList<>();

    // Top-level single item convenience fields
    private Long questionId;
    private Long answerId;
    private Double marks;
    private String feedback;

    public SubmissionGradeRequest() {
    }

    public SubmissionGradeRequest(List<GradeQuestionItem> grades) {
        this.grades = grades != null ? grades : new ArrayList<>();
    }

    public List<GradeQuestionItem> getEffectiveGrades() {
        List<GradeQuestionItem> result = new ArrayList<>();
        if (grades != null && !grades.isEmpty()) {
            result.addAll(grades);
        }
        if (marks != null && (questionId != null || answerId != null)) {
            result.add(new GradeQuestionItem(questionId, answerId, marks, feedback));
        }
        return result;
    }

    public List<GradeQuestionItem> getGrades() {
        return grades;
    }

    public void setGrades(List<GradeQuestionItem> grades) {
        this.grades = grades;
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
