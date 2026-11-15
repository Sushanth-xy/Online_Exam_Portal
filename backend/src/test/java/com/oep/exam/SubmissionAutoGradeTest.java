package com.oep.exam;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.oep.auth.JwtService;
import com.oep.auth.Role;
import com.oep.auth.User;
import com.oep.auth.UserRepository;
import com.oep.exam.dto.AutosaveAnswerItem;
import com.oep.exam.dto.AutosaveRequest;
import com.oep.questionbank.Difficulty;
import com.oep.questionbank.Question;
import com.oep.questionbank.QuestionRepository;
import com.oep.questionbank.QuestionType;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@AutoConfigureMockMvc
public class SubmissionAutoGradeTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;
    @Autowired private ExamRepository examRepository;
    @Autowired private ExamAttemptRepository examAttemptRepository;
    @Autowired private QuestionRepository questionRepository;
    @Autowired private UserRepository userRepository;
    @Autowired private PasswordEncoder passwordEncoder;
    @Autowired private JwtService jwtService;

    private String studentToken;

    @BeforeEach
    void setUp() {
        examAttemptRepository.deleteAll();
        examRepository.deleteAll();
        questionRepository.deleteAll();
        userRepository.deleteAll();

        User student = userRepository.save(new User("Student", "student@oep-test.com",
                passwordEncoder.encode("Password@123"), Role.STUDENT));
        studentToken = jwtService.generateToken(student);
    }

    // ---------------------------------------------------------------- helpers

    private Question saveQuestion(String prompt, QuestionType type, List<String> options,
                                  String correctAnswer, double marks) {
        return questionRepository.save(new Question(prompt, type, Difficulty.EASY, "General",
                options, correctAnswer, marks, 0.0, "Explanation", "inst@oep-test.com"));
    }

    // Exam that is open right now (started 1 hour ago, ends in 2 hours)
    private Exam saveActiveExam(double negativeFactor, Question... questions) {
        return examRepository.save(new Exam("Grade Exam", "Grading test", 60,
                LocalDateTime.now().minusHours(1), LocalDateTime.now().plusHours(2),
                40.0, negativeFactor, false, "inst@oep-test.com", Arrays.asList(questions)));
    }

    // Student starts the exam, saves answers, submits. Returns the submit response.
    private ResultActions startAnswerAndSubmit(Exam exam, AutosaveAnswerItem... answers) throws Exception {
        String base = "/api/v1/exams/" + exam.getId();

        mockMvc.perform(post(base + "/start")
                        .header("Authorization", "Bearer " + studentToken))
                .andExpect(status().isOk());

        mockMvc.perform(post(base + "/autosave")
                        .header("Authorization", "Bearer " + studentToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new AutosaveRequest(Arrays.asList(answers)))))
                .andExpect(status().isOk());

        return mockMvc.perform(post(base + "/submit")
                .header("Authorization", "Bearer " + studentToken));
    }

    // ------------------------------------------------------------------ tests

    @Test
    @DisplayName("TC-Grade-01: scoring across MCQ, TRUE_FALSE, unanswered and SHORT_ANSWER with negative marking")
    void scoringAcrossQuestionTypesWithNegativeMarking() throws Exception {
        Question mcq = saveQuestion("2+2?", QuestionType.MCQ,
                Arrays.asList("3", "4", "5"), "4", 4.0);
        Question trueFalse = saveQuestion("The sun is a star.", QuestionType.TRUE_FALSE,
                Arrays.asList("True", "False"), "True", 2.0);
        Question unanswered = saveQuestion("Capital of India?", QuestionType.MCQ,
                Arrays.asList("Delhi", "Mumbai"), "Delhi", 6.0);
        Question shortAnswer = saveQuestion("Capital of France?", QuestionType.SHORT_ANSWER,
                Collections.emptyList(), "Paris", 3.0);

        Exam exam = saveActiveExam(0.5, mcq, trueFalse, unanswered, shortAnswer);

        // MCQ correct: +4.0
        // TRUE_FALSE wrong: -(2.0 x 0.5) = -1.0
        // MCQ unanswered: 0.0
        // SHORT_ANSWER is not auto-graded: 0.0
        // Total = 3.0
        startAnswerAndSubmit(exam,
                new AutosaveAnswerItem(mcq.getId(), "4"),
                new AutosaveAnswerItem(trueFalse.getId(), "False"),
                new AutosaveAnswerItem(shortAnswer.getId(), "Paris"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("COMPLETED"))
                .andExpect(jsonPath("$.totalScore").value(3.0));
    }

    @Test
    @DisplayName("TC-Grade-01: total score never falls below zero")
    void totalScoreNeverBelowZero() throws Exception {
        Question mcq = saveQuestion("2+2?", QuestionType.MCQ,
                Arrays.asList("3", "4", "5"), "4", 4.0);

        // Full penalty (factor 1.0). One wrong answer: 0 - 4.0 = -4.0, which is lifted to 0.0
        Exam exam = saveActiveExam(1.0, mcq);

        startAnswerAndSubmit(exam, new AutosaveAnswerItem(mcq.getId(), "3"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("COMPLETED"))
                .andExpect(jsonPath("$.totalScore").value(0.0));
    }
        @Test
    @DisplayName("TC-Exam-01: attempt is auto-submitted (TIMED_OUT) when remaining time reaches zero")
    void examAutoSubmitsWhenTimeRunsOut() throws Exception {
        Question mcq = saveQuestion("2+2?", QuestionType.MCQ,
                Arrays.asList("3", "4", "5"), "4", 4.0);

        // 30-minute exam, window is open now
        Exam exam = examRepository.save(new Exam("Timed Exam", "Timeout test", 30,
                LocalDateTime.now().minusHours(1), LocalDateTime.now().plusHours(2),
                40.0, 0.0, false, "inst@oep-test.com", Collections.singletonList(mcq)));

        // The student started 31 minutes ago, so the 30 minutes are already over
        ExamAttempt attempt = new ExamAttempt(exam, "student@oep-test.com");
        attempt.setStartedAt(LocalDateTime.now().minusMinutes(31));
        examAttemptRepository.save(attempt);

        // The student tries to save an answer after time is up: it must be refused
        mockMvc.perform(post("/api/v1/exams/" + exam.getId() + "/autosave")
                        .header("Authorization", "Bearer " + studentToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new AutosaveRequest(
                                Collections.singletonList(new AutosaveAnswerItem(mcq.getId(), "4"))))))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value(org.hamcrest.Matchers.containsString("expired")));

        // The attempt must now be stored as TIMED_OUT with a submit time
        ExamAttempt saved = examAttemptRepository
                .findByExamIdAndStudentEmail(exam.getId(), "student@oep-test.com").orElseThrow();
        assertThat(saved.getStatus()).isEqualTo(AttemptStatus.TIMED_OUT);
        assertThat(saved.getSubmittedAt()).isNotNull();
    }

    @AfterEach
    void tearDown() {
        examAttemptRepository.deleteAll();
        examRepository.deleteAll();
        questionRepository.deleteAll();
        userRepository.deleteAll();
    }
}