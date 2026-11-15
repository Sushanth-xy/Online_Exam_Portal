package com.oep.grading;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.oep.auth.JwtService;
import com.oep.auth.Role;
import com.oep.auth.User;
import com.oep.auth.UserRepository;
import com.oep.exam.*;
import com.oep.grading.dto.GradeQuestionItem;
import com.oep.grading.dto.SubmissionGradeRequest;
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
import java.util.Collections;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Sprint 2 / Issue #14 [Step 2.1] - Subjective Question Manual Grading & Feedback API.
 * Traceability: SRS OEP-F-017, Test Plan TC-Grade-02.
 */
@SpringBootTest
@AutoConfigureMockMvc
public class GradingApiTest {

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

    private String instructorToken;
    private String studentToken;
    private Question mcqQuestion;
    private Question subjectiveQuestion;
    private Exam exam;
    private ExamAttempt completedAttempt;

    @BeforeEach
    void setUp() {
        examAttemptRepository.deleteAll();
        examRepository.deleteAll();
        questionRepository.deleteAll();
        userRepository.deleteAll();

        User instructor = userRepository.save(new User(
                "Professor Smith",
                "instructor@oep.edu",
                passwordEncoder.encode("Password@123"),
                Role.INSTRUCTOR
        ));

        User student = userRepository.save(new User(
                "Alice Student",
                "student@oep.edu",
                passwordEncoder.encode("Password@123"),
                Role.STUDENT
        ));

        instructorToken = jwtService.generateToken(instructor);
        studentToken = jwtService.generateToken(student);

        // 1. Objective Question (MCQ, 2.0 marks)
        mcqQuestion = questionRepository.save(new Question(
                "Which data structure follows FIFO?",
                QuestionType.MCQ,
                Difficulty.EASY,
                "Computer Science",
                Arrays.asList("Stack", "Queue", "Tree", "Graph"),
                "Queue",
                2.0,
                0.5,
                "Queue is First-In-First-Out",
                "instructor@oep.edu"
        ));

        // 2. Subjective Question (SHORT_ANSWER, 5.0 marks)
        subjectiveQuestion = questionRepository.save(new Question(
                "Explain the concept of Polymorphism in OOP.",
                QuestionType.SHORT_ANSWER,
                Difficulty.MEDIUM,
                "Computer Science",
                Collections.emptyList(),
                "Ability of messages or objects to be processed in more than one form",
                5.0,
                0.0,
                "Can be compile-time (overloading) or runtime (overriding).",
                "instructor@oep.edu"
        ));

        // Create Exam
        exam = examRepository.save(new Exam(
                "Software Engineering Final Exam",
                "Final Assessment covering OOP and Data Structures",
                60,
                LocalDateTime.now().minusHours(2),
                LocalDateTime.now().plusHours(2),
                50.0, // 50% pass percentage
                0.0,
                false,
                "instructor@oep.edu",
                Arrays.asList(mcqQuestion, subjectiveQuestion)
        ));

        // Create Completed Attempt
        completedAttempt = new ExamAttempt(exam, "student@oep.edu");
        completedAttempt.setStatus(AttemptStatus.COMPLETED);
        completedAttempt.setStartedAt(LocalDateTime.now().minusMinutes(50));
        completedAttempt.setSubmittedAt(LocalDateTime.now().minusMinutes(10));

        // Answer 1: Objective (auto-graded 2.0)
        CandidateAnswer ans1 = new CandidateAnswer(mcqQuestion.getId(), "Queue");
        ans1.setMarksAwarded(2.0);
        ans1.setCorrect(true);
        ans1.setEvaluated(true);

        // Answer 2: Subjective (pending manual evaluation)
        CandidateAnswer ans2 = new CandidateAnswer(subjectiveQuestion.getId(),
                "Polymorphism allows objects to take multiple forms through method overloading and overriding.");
        ans2.setMarksAwarded(0.0);
        ans2.setEvaluated(false);

        completedAttempt.getAnswers().add(ans1);
        completedAttempt.getAnswers().add(ans2);
        completedAttempt.setTotalScore(2.0); // Initial score with only objective graded
        completedAttempt.setPassed(false); // 2.0 / 7.0 = 28.5% < 50%

        completedAttempt = examAttemptRepository.save(completedAttempt);
    }

    @Test
    @DisplayName("TC-Grade-02: Instructor fetches candidate submissions needing evaluation")
    void testGetSubmissionsForGrading() throws Exception {
        mockMvc.perform(get("/api/v1/grading/submissions")
                        .header("Authorization", "Bearer " + instructorToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].submissionId").value(completedAttempt.getId()))
                .andExpect(jsonPath("$[0].examTitle").value("Software Engineering Final Exam"))
                .andExpect(jsonPath("$[0].studentEmail").value("student@oep.edu"))
                .andExpect(jsonPath("$[0].subjectiveCount").value(1))
                .andExpect(jsonPath("$[0].pendingSubjectiveCount").value(1))
                .andExpect(jsonPath("$[0].fullyGraded").value(false))
                .andExpect(jsonPath("$[0].answers", hasSize(2)));
    }

    @Test
    @DisplayName("TC-Grade-02: Instructor filters submissions by examId and unevaluatedOnly")
    void testFilterSubmissions() throws Exception {
        // Filter by matching exam
        mockMvc.perform(get("/api/v1/grading/submissions")
                        .param("examId", exam.getId().toString())
                        .param("unevaluatedOnly", "true")
                        .header("Authorization", "Bearer " + instructorToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)));

        // Filter by non-existent exam
        mockMvc.perform(get("/api/v1/grading/submissions")
                        .param("examId", "999999")
                        .header("Authorization", "Bearer " + instructorToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));
    }

    @Test
    @DisplayName("TC-Grade-02: Instructor fetches single submission detail")
    void testGetSubmissionDetail() throws Exception {
        mockMvc.perform(get("/api/v1/grading/submissions/" + completedAttempt.getId())
                        .header("Authorization", "Bearer " + instructorToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.submissionId").value(completedAttempt.getId()))
                .andExpect(jsonPath("$.totalScore").value(2.0))
                .andExpect(jsonPath("$.maxMarks").value(7.0))
                .andExpect(jsonPath("$.answers[1].questionType").value("SHORT_ANSWER"))
                .andExpect(jsonPath("$.answers[1].candidateResponse").value(containsString("Polymorphism allows objects")))
                .andExpect(jsonPath("$.answers[1].evaluated").value(false));
    }

    @Test
    @DisplayName("TC-Grade-02: Instructor manually grades subjective answer and verifies dynamic score recalculation")
    void testGradeSubjectiveResponseAndDynamicScoreRecalculation() throws Exception {
        SubmissionGradeRequest gradeRequest = new SubmissionGradeRequest(Collections.singletonList(
                new GradeQuestionItem(
                        subjectiveQuestion.getId(),
                        4.5,
                        "Excellent explanation! Well articulated distinction of overloading and overriding."
                )
        ));

        mockMvc.perform(post("/api/v1/grading/submissions/" + completedAttempt.getId() + "/grade")
                        .header("Authorization", "Bearer " + instructorToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(gradeRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.submissionId").value(completedAttempt.getId()))
                .andExpect(jsonPath("$.totalScore").value(6.5)) // 2.0 (objective) + 4.5 (subjective) = 6.5
                .andExpect(jsonPath("$.passed").value(true)) // 6.5 / 7.0 = 92.8% >= 50%
                .andExpect(jsonPath("$.fullyGraded").value(true))
                .andExpect(jsonPath("$.pendingSubjectiveCount").value(0))
                .andExpect(jsonPath("$.answers[1].marksAwarded").value(4.5))
                .andExpect(jsonPath("$.answers[1].feedback").value("Excellent explanation! Well articulated distinction of overloading and overriding."))
                .andExpect(jsonPath("$.answers[1].evaluated").value(true));

        // Verify in Database directly
        ExamAttempt updated = examAttemptRepository.findById(completedAttempt.getId()).orElseThrow();
        assertThat(updated.getTotalScore()).isEqualTo(6.5);
        assertThat(updated.getPassed()).isTrue();

        CandidateAnswer updatedAnswer = updated.getAnswers().stream()
                .filter(a -> a.getQuestionId().equals(subjectiveQuestion.getId()))
                .findFirst()
                .orElseThrow();
        assertThat(updatedAnswer.getMarksAwarded()).isEqualTo(4.5);
        assertThat(updatedAnswer.getFeedback()).isEqualTo("Excellent explanation! Well articulated distinction of overloading and overriding.");
        assertThat(updatedAnswer.isEvaluated()).isTrue();
    }

    @Test
    @DisplayName("TC-Grade-02: Rejects marks awarded exceeding maximum question marks")
    void testMarksExceedingMaxMarksRejected() throws Exception {
        SubmissionGradeRequest invalidRequest = new SubmissionGradeRequest(Collections.singletonList(
                new GradeQuestionItem(subjectiveQuestion.getId(), 6.0, "Too high marks") // max is 5.0
        ));

        mockMvc.perform(post("/api/v1/grading/submissions/" + completedAttempt.getId() + "/grade")
                        .header("Authorization", "Bearer " + instructorToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value(containsString("cannot exceed maximum marks")));
    }

    @Test
    @DisplayName("TC-Grade-02: Rejects negative marks")
    void testNegativeMarksRejected() throws Exception {
        SubmissionGradeRequest invalidRequest = new SubmissionGradeRequest(Collections.singletonList(
                new GradeQuestionItem(subjectiveQuestion.getId(), -1.0, "Negative marks")
        ));

        mockMvc.perform(post("/api/v1/grading/submissions/" + completedAttempt.getId() + "/grade")
                        .header("Authorization", "Bearer " + instructorToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("RBAC: Students are forbidden from accessing grading endpoints")
    void testStudentCannotAccessGrading() throws Exception {
        mockMvc.perform(get("/api/v1/grading/submissions")
                        .header("Authorization", "Bearer " + studentToken))
                .andExpect(status().isForbidden());

        SubmissionGradeRequest gradeRequest = new SubmissionGradeRequest(Collections.singletonList(
                new GradeQuestionItem(subjectiveQuestion.getId(), 5.0, "Self grade")
        ));

        mockMvc.perform(post("/api/v1/grading/submissions/" + completedAttempt.getId() + "/grade")
                        .header("Authorization", "Bearer " + studentToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(gradeRequest)))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("Security: Unauthenticated requests are rejected with 401 or 403")
    void testUnauthenticatedCannotAccessGrading() throws Exception {
        mockMvc.perform(get("/api/v1/grading/submissions"))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("TC-Grade-02: Cannot grade an attempt that is still in progress")
    void testCannotGradeInProgressAttempt() throws Exception {
        ExamAttempt inProgressAttempt = new ExamAttempt(exam, "student2@oep.edu");
        inProgressAttempt.setStatus(AttemptStatus.IN_PROGRESS);
        inProgressAttempt = examAttemptRepository.save(inProgressAttempt);

        SubmissionGradeRequest gradeRequest = new SubmissionGradeRequest(Collections.singletonList(
                new GradeQuestionItem(subjectiveQuestion.getId(), 3.0, "Premature grading")
        ));

        mockMvc.perform(post("/api/v1/grading/submissions/" + inProgressAttempt.getId() + "/grade")
                        .header("Authorization", "Bearer " + instructorToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(gradeRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Invalid State"))
                .andExpect(jsonPath("$.message").value(containsString("still in progress")));
    }

    @AfterEach
    void tearDown() {
        examAttemptRepository.deleteAll();
        examRepository.deleteAll();
        questionRepository.deleteAll();
        userRepository.deleteAll();
    }
}
