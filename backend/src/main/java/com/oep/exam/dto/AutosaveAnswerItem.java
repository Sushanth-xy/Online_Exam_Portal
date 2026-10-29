package com.oep.exam.dto;

public class AutosaveAnswerItem {

    private Long questionId;
    private String selectedOption;

    public AutosaveAnswerItem() {
    }

    public AutosaveAnswerItem(Long questionId, String selectedOption) {
        this.questionId = questionId;
        this.selectedOption = selectedOption;
    }

    public Long getQuestionId() {
        return questionId;
    }

    public void setQuestionId(Long questionId) {
        this.questionId = questionId;
    }

    public String getSelectedOption() {
        return selectedOption;
    }

    public void setSelectedOption(String selectedOption) {
        this.selectedOption = selectedOption;
    }
}
