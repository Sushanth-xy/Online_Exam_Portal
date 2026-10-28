package com.oep.exam;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.oep.auth.Role;
import com.oep.auth.User;
import com.oep.auth.UserRepository;
import com.oep.auth.JwtService;
import com.oep.exam.dto.AutosaveAnswerItem;
import com.oep.exam.dto.AutosaveRequest;
import com.oep.exam.dto.ExamCreateRequest;
import com.oep.questionbank.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
public class ExamDeliveryTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ExamRepository examRepository;

    @Autowired
    private ExamAttemptRepository examAttemptRepository;

    @Autowired
    private QuestionRepository questionRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private JwtService jwtService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private ObjectMapper objectMapper;

    private String studentToken;
    private String instructorToken;
    private Question q1;
    private Question q2;
    private Exam activeExam;

    @BeforeEach
    void setUp() {
        examAttemptRepository.deleteAll();
        examRepository.deleteAll();
        questionRepository.deleteAll();
        userRepository.deleteAll();

        // Create student & instructor
        User student = userRepository.save(new User("Student One", "student@test.com", passwordEncoder.encode("pass123"), Role.STUDENT));
        User instructor = userRepository.save(new User("Instructor One", "instructor@test.com", passwordEncoder.encode("pass123"), Role.INSTRUCTOR));

        studentToken = jwtService.generateToken(student);
        instructorToken = jwtService.generateToken(instructor);

        // Seed 2 questions: MCQ and True/False
        q1 = questionRepository.save(new Question(
                "What is the capital of France?",
                QuestionType.MCQ,
                Difficulty.EASY,
                "Geography",
                Arrays.asList("Paris", "London", "Berlin", "Rome"),
                "Paris",
                4.0,
                1.0, // 1 mark negative
                "Paris is France's capital",
                "instructor@test.com"
        ));

        q2 = questionRepository.save(new Question(
                "Java is a compiled and interpreted language.",
                QuestionType.TRUE_FALSE,
                Difficulty.EASY,
                "Computer Science",
                Arrays.asList("True", "False"),
                "True",
                2.0,
                0.5,
                "Java bytecode is interpreted by JVM",
                "instructor@test.com"
        ));

        // Create an active exam window [now - 1 hour to now + 2 hours]
        activeExam = new Exam(
                "Midterm Assessment",
                "Comprehensive Midterm Examination",
                60,
                LocalDateTime.now().minusHours(1),
                LocalDateTime.now().plusHours(2),
                50.0,
                0.25,
                false,
                "instructor@test.com",
                Arrays.asList(q1, q2)
        );
        activeExam = examRepository.save(activeExam);
    }

    @Test
    @DisplayName("TC-Exam-04: Prevent starting an exam outside scheduled window")
    void testExamScheduleWindowGate() throws Exception {
        // Create an upcoming exam starting tomorrow
        Exam futureExam = examRepository.save(new Exam(
                "Future Exam",
                "Not yet open",
                30,
                LocalDateTime.now().plusDays(1),
                LocalDateTime.now().plusDays(2),
                50.0,
                0.0,
                false,
                "instructor@test.com",
                Collections.singletonList(q1)
        ));

        // Attempting to start before start window returns 400 Bad Request
        mockMvc.perform(post("/api/v1/exams/" + futureExam.getId() + "/start")
                        .header("Authorization", "Bearer " + studentToken))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Invalid State"))
                .andExpect(jsonPath("$.message").value(org.hamcrest.Matchers.containsString("Exam has not started yet")));
    }

    @Test
    @DisplayName("TC-Sec-03: Active exam delivery does NOT expose correct answers or explanations to student")
    void testStartExamCandidateViewSecurity() throws Exception {
        mockMvc.perform(post("/api/v1/exams/" + activeExam.getId() + "/start")
                        .header("Authorization", "Bearer " + studentToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.examId").value(activeExam.getId()))
                .andExpect(jsonPath("$.durationMinutes").value(60))
                .andExpect(jsonPath("$.remainingSeconds").isNumber())
                .andExpect(jsonPath("$.questions").isArray())
                .andExpect(jsonPath("$.questions[0].prompt").isNotEmpty())
                .andExpect(jsonPath("$.questions[0].correctAnswer").doesNotExist())
                .andExpect(jsonPath("$.questions[0].explanation").doesNotExist());
    }

    @Test
    @DisplayName("TC-Exam-02: Auto-save answers every 30 seconds and restore upon reload")
    void testAutosaveAndRestoreProgress() throws Exception {
        // 1. Start exam
        mockMvc.perform(post("/api/v1/exams/" + activeExam.getId() + "/start")
                        .header("Authorization", "Bearer " + studentToken))
                .andExpect(status().isOk());

        // 2. Autosave answer for q1
        AutosaveRequest autosave = new AutosaveRequest(Collections.singletonList(
                new AutosaveAnswerItem(q1.getId(), "Paris")
        ));

        mockMvc.perform(post("/api/v1/exams/" + activeExam.getId() + "/autosave")
                        .header("Authorization", "Bearer " + studentToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(autosave)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("SAVED"));

        // 3. Mid-exam browser refresh: fetch attempt and verify saved answer restored
        mockMvc.perform(get("/api/v1/exams/" + activeExam.getId() + "/attempt")
                        .header("Authorization", "Bearer " + studentToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.savedAnswers." + q1.getId()).value("Paris"));
    }

    @Test
    @DisplayName("TC-Exam-05 & TC-Grade-01: Submit exam marks status COMPLETED and performs instant auto-grading")
    void testSubmitExamAndInstantAutoGrading() throws Exception {
        // 1. Start exam
        mockMvc.perform(post("/api/v1/exams/" + activeExam.getId() + "/start")
                        .header("Authorization", "Bearer " + studentToken))
                .andExpect(status().isOk());

        // 2. Save answers: q1 correct ("Paris" -> 4.0 marks), q2 incorrect ("False" -> -0.5 marks with 0.25 negative factor)
        AutosaveRequest answers = new AutosaveRequest(Arrays.asList(
                new AutosaveAnswerItem(q1.getId(), "Paris"),
                new AutosaveAnswerItem(q2.getId(), "False")
        ));

        mockMvc.perform(post("/api/v1/exams/" + activeExam.getId() + "/autosave")
                        .header("Authorization", "Bearer " + studentToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(answers)))
                .andExpect(status().isOk());

        // 3. Final submit
        mockMvc.perform(post("/api/v1/exams/" + activeExam.getId() + "/submit")
                        .header("Authorization", "Bearer " + studentToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("COMPLETED"))
                .andExpect(jsonPath("$.submittedAt").isNotEmpty())
                .andExpect(jsonPath("$.totalScore").value(3.5)); // 4.0 - (2.0 * 0.25 = 0.5) = 3.5

        // Verify in DB
        ExamAttempt attempt = examAttemptRepository.findByExamIdAndStudentEmail(activeExam.getId(), "student@test.com").orElseThrow();
        assertThat(attempt.getStatus()).isEqualTo(AttemptStatus.COMPLETED);
        assertThat(attempt.getSubmittedAt()).isNotNull();
        assertThat(attempt.getTotalScore()).isEqualTo(3.5);
    }
}
