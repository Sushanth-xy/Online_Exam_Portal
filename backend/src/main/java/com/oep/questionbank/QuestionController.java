package com.oep.questionbank;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/questions")
public class QuestionController {

    private final QuestionRepository questionRepository;

    public QuestionController(QuestionRepository questionRepository) {
        this.questionRepository = questionRepository;
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('INSTRUCTOR', 'ADMIN')")
    public ResponseEntity<List<Question>> getAllQuestions(
            @RequestParam(required = false) String topic,
            @RequestParam(required = false) Difficulty difficulty
    ) {
        if (topic != null && !topic.isBlank()) {
            return ResponseEntity.ok(questionRepository.findByTopic(topic));
        }
        if (difficulty != null) {
            return ResponseEntity.ok(questionRepository.findByDifficulty(difficulty));
        }
        return ResponseEntity.ok(questionRepository.findAll());
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('INSTRUCTOR', 'ADMIN')")
    public ResponseEntity<Question> getQuestionById(@PathVariable Long id) {
        return questionRepository.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('INSTRUCTOR', 'ADMIN')")
    public ResponseEntity<Question> createQuestion(@Valid @RequestBody QuestionRequest request, Authentication auth) {
        String createdBy = auth != null ? auth.getName() : "system";

        Question question = new Question(
                request.getPrompt(),
                request.getQuestionType(),
                request.getDifficulty(),
                request.getTopic(),
                request.getOptions(),
                request.getCorrectAnswer(),
                request.getMarks(),
                request.getNegativeMarks(),
                request.getExplanation(),
                createdBy
        );

        Question saved = questionRepository.save(question);
        return ResponseEntity.status(HttpStatus.CREATED).body(saved);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('INSTRUCTOR', 'ADMIN')")
    public ResponseEntity<Question> updateQuestion(@PathVariable Long id, @Valid @RequestBody QuestionRequest request) {
        return questionRepository.findById(id)
                .map(existing -> {
                    existing.setPrompt(request.getPrompt());
                    existing.setQuestionType(request.getQuestionType());
                    existing.setDifficulty(request.getDifficulty());
                    existing.setTopic(request.getTopic());
                    existing.setOptions(request.getOptions());
                    existing.setCorrectAnswer(request.getCorrectAnswer());
                    existing.setMarks(request.getMarks());
                    existing.setNegativeMarks(request.getNegativeMarks());
                    existing.setExplanation(request.getExplanation());
                    return ResponseEntity.ok(questionRepository.save(existing));
                })
                .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('INSTRUCTOR', 'ADMIN')")
    public ResponseEntity<Void> deleteQuestion(@PathVariable Long id) {
        if (!questionRepository.existsById(id)) {
            return ResponseEntity.notFound().build();
        }
        questionRepository.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}
