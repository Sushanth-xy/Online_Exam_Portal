package com.oep.grading.dto;

import com.oep.questionbank.QuestionType;

public class GradingAnswerItemDTO {

    private Long answerId;
    private Long questionId;
    private String prompt;
    private QuestionType questionType;
    private double maxMarks;
    private String candidateResponse;
    private double marksAwarded;
    private String feedback;
    private boolean evaluated;
    private String referenceAnswer;
    private String explanation;

    public GradingAnswerItemDTO() {
    }

    public GradingAnswerItemDTO(Long answerId, Long questionId, String prompt,
                                QuestionType questionType, double maxMarks,
                                String candidateResponse, double marksAwarded,
                                String feedback, boolean evaluated,
                                String referenceAnswer, String explanation) {
        this.answerId = answerId;
        this.questionId = questionId;
        this.prompt = prompt;
        this.questionType = questionType;
        this.maxMarks = maxMarks;
        this.candidateResponse = candidateResponse;
        this.marksAwarded = marksAwarded;
        this.feedback = feedback;
        this.evaluated = evaluated;
        this.referenceAnswer = referenceAnswer;
        this.explanation = explanation;
    }

    public Long getAnswerId() {
        return answerId;
    }

    public void setAnswerId(Long answerId) {
        this.answerId = answerId;
    }

    public Long getQuestionId() {
        return questionId;
    }

    public void setQuestionId(Long questionId) {
        this.questionId = questionId;
    }

    public String getPrompt() {
        return prompt;
    }

    public void setPrompt(String prompt) {
        this.prompt = prompt;
    }

    public QuestionType getQuestionType() {
        return questionType;
    }

    public void setQuestionType(QuestionType questionType) {
        this.questionType = questionType;
    }

    public double getMaxMarks() {
        return maxMarks;
    }

    public void setMaxMarks(double maxMarks) {
        this.maxMarks = maxMarks;
    }

    public String getCandidateResponse() {
        return candidateResponse;
    }

    public void setCandidateResponse(String candidateResponse) {
        this.candidateResponse = candidateResponse;
    }

    public double getMarksAwarded() {
        return marksAwarded;
    }

    public void setMarksAwarded(double marksAwarded) {
        this.marksAwarded = marksAwarded;
    }

    public String getFeedback() {
        return feedback;
    }

    public void setFeedback(String feedback) {
        this.feedback = feedback;
    }

    public boolean isEvaluated() {
        return evaluated;
    }

    public void setEvaluated(boolean evaluated) {
        this.evaluated = evaluated;
    }

    public String getReferenceAnswer() {
        return referenceAnswer;
    }

    public void setReferenceAnswer(String referenceAnswer) {
        this.referenceAnswer = referenceAnswer;
    }

    public String getExplanation() {
        return explanation;
    }

    public void setExplanation(String explanation) {
        this.explanation = explanation;
    }
}
