package com.oep.exam;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.oep.auth.JwtService;
import com.oep.auth.Role;
import com.oep.auth.User;
import com.oep.auth.UserRepository;
import com.oep.exam.dto.AutosaveRequest;
import com.oep.questionbank.Difficulty;
import com.oep.questionbank.Question;
import com.oep.questionbank.QuestionRepository;
import com.oep.questionbank.QuestionType;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.junit.jupiter.api.Disabled;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
public class TimeoutGradingProbeTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;
    @Autowired private ExamRepository examRepository;
    @Autowired private ExamAttemptRepository examAttemptRepository;
    @Autowired private QuestionRepository questionRepository;
    @Autowired private UserRepository userRepository;
    @Autowired private PasswordEncoder passwordEncoder;
    @Autowired private JwtService jwtService;

    @Test
    @DisplayName("DEF-01: a timed-out attempt should still score its saved correct answer")
    void timedOutAttemptKeepsScoreOfSavedAnswers() throws Exception {
        examAttemptRepository.deleteAll();
        examRepository.deleteAll();
        questionRepository.deleteAll();
        userRepository.deleteAll();

        User student = userRepository.save(new User("Student", "student@oep-test.com",
                passwordEncoder.encode("Password@123"), Role.STUDENT));
        String token = jwtService.generateToken(student);

        Question mcq = questionRepository.save(new Question("2+2?", QuestionType.MCQ, Difficulty.EASY,
                "Math", Arrays.asList("3", "4", "5"), "4", 4.0, 0.0, "Basic sum", "inst@oep-test.com"));

        Exam exam = examRepository.save(new Exam("Timed Exam", "Probe", 30,
                LocalDateTime.now().minusHours(1), LocalDateTime.now().plusHours(2),
                40.0, 0.0, false, "inst@oep-test.com", Collections.singletonList(mcq)));

        // Student started 31 minutes ago and already saved a CORRECT answer
        ExamAttempt attempt = new ExamAttempt(exam, "student@oep-test.com");
        attempt.setStartedAt(LocalDateTime.now().minusMinutes(31));
        attempt.getAnswers().add(new CandidateAnswer(mcq.getId(), "4"));
        examAttemptRepository.save(attempt);

        // Student sends another autosave after time is up: the app times the attempt out
        mockMvc.perform(post("/api/v1/exams/" + exam.getId() + "/autosave")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new AutosaveRequest(Collections.emptyList()))))
                .andExpect(status().isBadRequest());

        ExamAttempt saved = examAttemptRepository
                .findByExamIdAndStudentEmail(exam.getId(), "student@oep-test.com").orElseThrow();
        assertThat(saved.getStatus()).isEqualTo(AttemptStatus.TIMED_OUT);
        // Expected: 4.0 (the saved answer is correct). A defect would show 0.0
        assertThat(saved.getTotalScore()).isEqualTo(4.0);
    }

    @AfterEach
    void tearDown() {
        examAttemptRepository.deleteAll();
        examRepository.deleteAll();
        questionRepository.deleteAll();
        userRepository.deleteAll();
    }
}