package com.oep.exam.dto;

import java.util.List;

public class AutosaveRequest {

    private List<AutosaveAnswerItem> answers;

    public AutosaveRequest() {
    }

    public AutosaveRequest(List<AutosaveAnswerItem> answers) {
        this.answers = answers;
    }

    public List<AutosaveAnswerItem> getAnswers() {
        return answers;
    }

    public void setAnswers(List<AutosaveAnswerItem> answers) {
        this.answers = answers;
    }
}
