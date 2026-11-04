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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
public class ManualGradingApiTest {

    private static final String STUDENT_EMAIL = "student@oep-test.com";

    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;
    @Autowired private ExamRepository examRepository;
    @Autowired private ExamAttemptRepository examAttemptRepository;
    @Autowired private QuestionRepository questionRepository;
    @Autowired private UserRepository userRepository;
    @Autowired private PasswordEncoder passwordEncoder;
    @Autowired private JwtService jwtService;

    private String studentToken;
    private String instructorToken;
    private Question mcq;
    private Question shortAnswer;
    private Long attemptId;

    @BeforeEach
    void setUp() throws Exception {
        examAttemptRepository.deleteAll();
        examRepository.deleteAll();
        questionRepository.deleteAll();
        userRepository.deleteAll();

        User student = userRepository.save(new User("Student", STUDENT_EMAIL,
                passwordEncoder.encode("Password@123"), Role.STUDENT));
        User instructor = userRepository.save(new User("Instructor", "inst@oep-test.com",
                passwordEncoder.encode("Password@123"), Role.INSTRUCTOR));
        studentToken = jwtService.generateToken(student);
        instructorToken = jwtService.generateToken(instructor);

        mcq = questionRepository.save(new Question("2+2?", QuestionType.MCQ, Difficulty.EASY,
                "Math", Arrays.asList("3", "4", "5"), "4", 4.0, 0.0, "Basic sum", "inst@oep-test.com"));
        shortAnswer = questionRepository.save(new Question("Explain gravity.", QuestionType.SHORT_ANSWER,
                Difficulty.MEDIUM, "Science", Collections.emptyList(), "A force that attracts masses",
                6.0, 0.0, "Reference answer", "inst@oep-test.com"));

        Exam exam = examRepository.save(new Exam("Mixed Exam", "Manual grading test", 60,
                LocalDateTime.now().minusHours(1), LocalDateTime.now().plusHours(2),
                40.0, 0.0, false, "inst@oep-test.com", Arrays.asList(mcq, shortAnswer)));

        // Student starts, answers both questions, submits
        String base = "/api/v1/exams/" + exam.getId();
        mockMvc.perform(post(base + "/start")
                        .header("Authorization", "Bearer " + studentToken))
                .andExpect(status().isOk());

        List<AutosaveAnswerItem> answers = Arrays.asList(
                new AutosaveAnswerItem(mcq.getId(), "4"),
                new AutosaveAnswerItem(shortAnswer.getId(), "Gravity pulls objects toward each other."));
        mockMvc.perform(post(base + "/autosave")
                        .header("Authorization", "Bearer " + studentToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new AutosaveRequest(answers))))
                .andExpect(status().isOk());

        mockMvc.perform(post(base + "/submit")
                        .header("Authorization", "Bearer " + studentToken))
                .andExpect(status().isOk());

        attemptId = examAttemptRepository.findByExamIdAndStudentEmail(exam.getId(), STUDENT_EMAIL)
                .orElseThrow().getId();
    }

    private String gradeUrl() {
        return "/api/v1/grading/submissions/" + attemptId + "/grade";
    }

    @Test
    @DisplayName("TC-Grade-02: instructor manual marks and feedback update the aggregate score")
    void manualMarksAndFeedbackUpdateTotalScore() throws Exception {
        // Before manual grading: only the MCQ was auto-graded (4.0)
        assertThat(examAttemptRepository.findById(attemptId).orElseThrow().getTotalScore()).isEqualTo(4.0);

        String body = "{\"questionId\":" + shortAnswer.getId() + ",\"marks\":5.0,\"feedback\":\"Good answer\"}";

        mockMvc.perform(post(gradeUrl())
                        .header("Authorization", "Bearer " + instructorToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk());

        // After manual grading: 4.0 (MCQ) + 5.0 (short answer) = 9.0
        ExamAttempt updated = examAttemptRepository.findById(attemptId).orElseThrow();
        assertThat(updated.getTotalScore()).isEqualTo(9.0);

        CandidateAnswer graded = updated.getAnswers().stream()
                .filter(a -> a.getQuestionId().equals(shortAnswer.getId()))
                .findFirst().orElseThrow();
        assertThat(graded.getMarksAwarded()).isEqualTo(5.0);
        assertThat(graded.getFeedback()).isEqualTo("Good answer");
        assertThat(graded.isEvaluated()).isTrue();
    }

    @Test
    @DisplayName("TC-Grade-02: marks above the question maximum are rejected and the score is unchanged")
    void marksAboveMaximumAreRejected() throws Exception {
        String body = "{\"questionId\":" + shortAnswer.getId() + ",\"marks\":7.0,\"feedback\":\"Too high\"}";

        mockMvc.perform(post(gradeUrl())
                        .header("Authorization", "Bearer " + instructorToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest());

        assertThat(examAttemptRepository.findById(attemptId).orElseThrow().getTotalScore()).isEqualTo(4.0);
    }

    @Test
    @DisplayName("TC-Grade-02: a student cannot grade a submission")
    void studentCannotGrade() throws Exception {
        String body = "{\"questionId\":" + shortAnswer.getId() + ",\"marks\":6.0,\"feedback\":\"Self grade\"}";

        mockMvc.perform(post(gradeUrl())
                        .header("Authorization", "Bearer " + studentToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isForbidden());
    }
}