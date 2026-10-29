package com.oep.exam;

import com.oep.questionbank.Question;
import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "exams")
public class Exam {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String title;

    @Column(length = 2000)
    private String description;

    @Column(nullable = false)
    private int durationMinutes = 60;

    @Column(nullable = false)
    private LocalDateTime startTime;

    @Column(nullable = false)
    private LocalDateTime endTime;

    @Column(nullable = false)
    private double passPercentage = 40.0;

    @Column(nullable = false)
    private double negativeMarkingFactor = 0.0; // e.g., 0.25 for 1/4th negative

    @Column(nullable = false)
    private boolean shuffleQuestions = true;

    @Column(nullable = false)
    private boolean resultsPublished = false;

    private String createdBy;

    @Column(nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
            name = "exam_questions",
            joinColumns = @JoinColumn(name = "exam_id"),
            inverseJoinColumns = @JoinColumn(name = "question_id")
    )
    private List<Question> questions = new ArrayList<>();

    public Exam() {
    }

    public Exam(String title, String description, int durationMinutes,
                LocalDateTime startTime, LocalDateTime endTime, double passPercentage,
                double negativeMarkingFactor, boolean shuffleQuestions, String createdBy,
                List<Question> questions) {
        this.title = title;
        this.description = description;
        this.durationMinutes = durationMinutes;
        this.startTime = startTime;
        this.endTime = endTime;
        this.passPercentage = passPercentage;
        this.negativeMarkingFactor = negativeMarkingFactor;
        this.shuffleQuestions = shuffleQuestions;
        this.createdBy = createdBy;
        this.questions = questions != null ? questions : new ArrayList<>();
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
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

    public boolean isResultsPublished() {
        return resultsPublished;
    }

    public void setResultsPublished(boolean resultsPublished) {
        this.resultsPublished = resultsPublished;
    }

    public String getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(String createdBy) {
        this.createdBy = createdBy;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public List<Question> getQuestions() {
        return questions;
    }

    public void setQuestions(List<Question> questions) {
        this.questions = questions;
    }

    public double getTotalMarks() {
        return questions.stream().mapToDouble(Question::getMarks).sum();
    }
}
