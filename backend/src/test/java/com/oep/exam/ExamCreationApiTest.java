package com.oep.exam;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.oep.auth.JwtService;
import com.oep.auth.Role;
import com.oep.auth.User;
import com.oep.auth.UserRepository;
import com.oep.exam.dto.ExamCreateRequest;
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

import java.time.LocalDateTime;
import java.util.Arrays;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
public class ExamCreationApiTest {

    private static final String URL = "/api/v1/exams";

    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;
    @Autowired private ExamRepository examRepository;
    @Autowired private ExamAttemptRepository examAttemptRepository;
    @Autowired private QuestionRepository questionRepository;
    @Autowired private UserRepository userRepository;
    @Autowired private PasswordEncoder passwordEncoder;
    @Autowired private JwtService jwtService;

    private String instructorToken;
    private Question q1;
    private Question q2;

    @BeforeEach
    void setUp() {
        examAttemptRepository.deleteAll();
        examRepository.deleteAll();
        questionRepository.deleteAll();
        userRepository.deleteAll();

        User instructor = userRepository.save(new User("Instructor", "inst@oep-test.com",
                passwordEncoder.encode("Password@123"), Role.INSTRUCTOR));
        instructorToken = jwtService.generateToken(instructor);

        q1 = questionRepository.save(new Question("2+2?", QuestionType.MCQ, Difficulty.EASY,
                "Math", Arrays.asList("3", "4", "5"), "4", 4.0, 1.0, "Basic sum", "inst@oep-test.com"));
        q2 = questionRepository.save(new Question("The sun is a star.", QuestionType.TRUE_FALSE,
                Difficulty.EASY, "Science", Arrays.asList("True", "False"), "True", 2.0, 0.5,
                "It is a star", "inst@oep-test.com"));
    }

    @Test
    @DisplayName("TC-QB-02: exam is created with the correct question count and total marks")
    void createExamWithCorrectCountAndMarks() throws Exception {
        ExamCreateRequest request = new ExamCreateRequest(
                "Quiz 1", "Two question quiz", 30,
                LocalDateTime.now().plusHours(1), LocalDateTime.now().plusHours(3),
                40.0, 0.25, false, Arrays.asList(q1.getId(), q2.getId()));

        String body = mockMvc.perform(post(URL)
                        .header("Authorization", "Bearer " + instructorToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.title").value("Quiz 1"))
                .andExpect(jsonPath("$.questions.length()").value(2))
                .andReturn().getResponse().getContentAsString();

        double totalMarks = 0;
        for (JsonNode question : objectMapper.readTree(body).get("questions")) {
            totalMarks += question.get("marks").asDouble();
        }
        assertThat(totalMarks).isEqualTo(6.0);
    }
        @Test
    @DisplayName("TC-QB-03: duration and negative marking factor are saved as configured")
    void durationAndNegativeMarkingAreSaved() throws Exception {
        ExamCreateRequest request = new ExamCreateRequest(
                "Quiz 2", "Config check", 45,
                LocalDateTime.now().plusHours(1), LocalDateTime.now().plusHours(3),
                40.0, 0.25, false, Arrays.asList(q1.getId(), q2.getId()));

        mockMvc.perform(post(URL)
                        .header("Authorization", "Bearer " + instructorToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.durationMinutes").value(45))
                .andExpect(jsonPath("$.negativeMarkingFactor").value(0.25));
    }

    @Test
    @DisplayName("TC-QB-03: duration below 1 minute is rejected")
    void zeroDurationIsRejected() throws Exception {
        ExamCreateRequest request = new ExamCreateRequest(
                "Bad Quiz", "Invalid duration", 0,
                LocalDateTime.now().plusHours(1), LocalDateTime.now().plusHours(3),
                40.0, 0.25, false, Arrays.asList(q1.getId()));

        mockMvc.perform(post(URL)
                        .header("Authorization", "Bearer " + instructorToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @AfterEach
    void tearDown() {
        examAttemptRepository.deleteAll();
        examRepository.deleteAll();
        questionRepository.deleteAll();
        userRepository.deleteAll();
    }
}