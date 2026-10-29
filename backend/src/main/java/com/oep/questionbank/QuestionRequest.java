package com.oep.questionbank;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.util.List;

public class QuestionRequest {

    @NotBlank(message = "Question prompt is required")
    private String prompt;

    @NotNull(message = "Question type is required")
    private QuestionType questionType = QuestionType.MCQ;

    private Difficulty difficulty = Difficulty.MEDIUM;

    @NotBlank(message = "Topic is required")
    private String topic = "General";

    private List<String> options;

    @NotBlank(message = "Correct answer is required")
    private String correctAnswer;

    @Positive(message = "Marks must be positive")
    private double marks = 1.0;

    private double negativeMarks = 0.0;

    private String explanation;

    public QuestionRequest() {
    }

    public QuestionRequest(String prompt, QuestionType questionType, Difficulty difficulty, String topic,
                           List<String> options, String correctAnswer, double marks, double negativeMarks,
                           String explanation) {
        this.prompt = prompt;
        this.questionType = questionType;
        this.difficulty = difficulty;
        this.topic = topic;
        this.options = options;
        this.correctAnswer = correctAnswer;
        this.marks = marks;
        this.negativeMarks = negativeMarks;
        this.explanation = explanation;
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

    public Difficulty getDifficulty() {
        return difficulty;
    }

    public void setDifficulty(Difficulty difficulty) {
        this.difficulty = difficulty;
    }

    public String getTopic() {
        return topic;
    }

    public void setTopic(String topic) {
        this.topic = topic;
    }

    public List<String> getOptions() {
        return options;
    }

    public void setOptions(List<String> options) {
        this.options = options;
    }

    public String getCorrectAnswer() {
        return correctAnswer;
    }

    public void setCorrectAnswer(String correctAnswer) {
        this.correctAnswer = correctAnswer;
    }

    public double getMarks() {
        return marks;
    }

    public void setMarks(double marks) {
        this.marks = marks;
    }

    public double getNegativeMarks() {
        return negativeMarks;
    }

    public void setNegativeMarks(double negativeMarks) {
        this.negativeMarks = negativeMarks;
    }

    public String getExplanation() {
        return explanation;
    }

    public void setExplanation(String explanation) {
        this.explanation = explanation;
    }
}
