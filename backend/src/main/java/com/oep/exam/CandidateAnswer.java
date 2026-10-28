package com.oep.exam;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "candidate_answers")
public class CandidateAnswer {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long questionId;

    @Column(length = 2000)
    private String selectedOption; // option text or short answer text

    private Boolean correct;

    private double marksAwarded = 0.0;

    @Column(length = 1000)
    private String feedback;

    @Column(nullable = false)
    private boolean evaluated = false;

    @Column(nullable = false)
    private LocalDateTime lastSavedAt = LocalDateTime.now();

    public CandidateAnswer() {
    }

    public CandidateAnswer(Long questionId, String selectedOption) {
        this.questionId = questionId;
        this.selectedOption = selectedOption;
        this.lastSavedAt = LocalDateTime.now();
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
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
        this.lastSavedAt = LocalDateTime.now();
    }

    public Boolean getCorrect() {
        return correct;
    }

    public void setCorrect(Boolean correct) {
        this.correct = correct;
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

    public LocalDateTime getLastSavedAt() {
        return lastSavedAt;
    }

    public void setLastSavedAt(LocalDateTime lastSavedAt) {
        this.lastSavedAt = lastSavedAt;
    }
}
