package com.oep.exam.dto;

import com.oep.questionbank.QuestionType;
import java.util.List;

public class QuestionForCandidateDTO {

    private Long id;
    private String prompt;
    private QuestionType questionType;
    private List<String> options;
    private double marks;

    public QuestionForCandidateDTO() {
    }

    public QuestionForCandidateDTO(Long id, String prompt, QuestionType questionType, List<String> options, double marks) {
        this.id = id;
        this.prompt = prompt;
        this.questionType = questionType;
        this.options = options;
        this.marks = marks;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
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

    public List<String> getOptions() {
        return options;
    }

    public void setOptions(List<String> options) {
        this.options = options;
    }

    public double getMarks() {
        return marks;
    }

    public void setMarks(double marks) {
        this.marks = marks;
    }
}
