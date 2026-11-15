package com.oep.proctoring;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.oep.auth.JwtService;
import com.oep.auth.Role;
import com.oep.auth.User;
import com.oep.auth.UserRepository;
import com.oep.exam.Exam;
import com.oep.exam.ExamAttempt;
import com.oep.exam.ExamAttemptRepository;
import com.oep.exam.ExamRepository;
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
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class ProctoringApiTest {

    private static final String STUDENT_EMAIL = "candidate@oep-test.com";
    private static final String INSTRUCTOR_EMAIL = "instructor@oep-test.com";
    private static final String OTHER_INSTRUCTOR_EMAIL = "other@oep-test.com";
    private static final String ADMIN_EMAIL = "admin@oep-test.com";

    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;
    @Autowired private UserRepository userRepository;
    @Autowired private ExamRepository examRepository;
    @Autowired private ExamAttemptRepository attemptRepository;
    @Autowired private ProctoringIncidentRepository incidentRepository;
    @Autowired private ProctoringService proctoringService;
    @Autowired private PasswordEncoder passwordEncoder;
    @Autowired private JwtService jwtService;

    private String studentToken;
    private String instructorToken;
    private String otherInstructorToken;
    private String adminToken;
    private ExamAttempt instructorAttempt;
    private ExamAttempt otherInstructorAttempt;

    @BeforeEach
    void setUp() {
        incidentRepository.deleteAll();
        attemptRepository.deleteAll();
        examRepository.deleteAll();
        userRepository.deleteAll();

        User student = saveUser("Candidate", STUDENT_EMAIL, Role.STUDENT);
        User instructor = saveUser("Instructor", INSTRUCTOR_EMAIL, Role.INSTRUCTOR);
        User otherInstructor = saveUser("Other Instructor", OTHER_INSTRUCTOR_EMAIL, Role.INSTRUCTOR);
        User admin = saveUser("Admin", ADMIN_EMAIL, Role.ADMIN);

        studentToken = jwtService.generateToken(student);
        instructorToken = jwtService.generateToken(instructor);
        otherInstructorToken = jwtService.generateToken(otherInstructor);
        adminToken = jwtService.generateToken(admin);

        Exam instructorExam = saveExam("Instructor Exam", INSTRUCTOR_EMAIL);
        Exam otherExam = saveExam("Other Exam", OTHER_INSTRUCTOR_EMAIL);
        instructorAttempt = attemptRepository.save(
                new ExamAttempt(instructorExam, STUDENT_EMAIL));
        otherInstructorAttempt = attemptRepository.save(
                new ExamAttempt(otherExam, STUDENT_EMAIL));
    }

    @AfterEach
    void tearDown() {
        incidentRepository.deleteAll();
        attemptRepository.deleteAll();
        examRepository.deleteAll();
        userRepository.deleteAll();
    }

    @Test
    @DisplayName("TC-Mon-01: tab-switch incident is timestamped and saved to the active attempt")
    void tabSwitchIncidentIsTimestampedAndSaved() throws Exception {
        LocalDateTime requestedAt = LocalDateTime.now();

        String response = mockMvc.perform(post(
                        "/api/v1/proctoring/attempts/{attemptId}/incidents",
                        instructorAttempt.getId())
                        .header("Authorization", "Bearer " + studentToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"type\":\"TAB_SWITCH\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.attemptId").value(instructorAttempt.getId()))
                .andExpect(jsonPath("$.type").value("TAB_SWITCH"))
                .andExpect(jsonPath("$.occurredAt").isNotEmpty())
                .andReturn().getResponse().getContentAsString();

        LocalDateTime occurredAt = LocalDateTime.parse(
                objectMapper.readTree(response).get("occurredAt").asText());
        assertThat(occurredAt).isAfterOrEqualTo(requestedAt.minusSeconds(1));
        assertThat(occurredAt).isBeforeOrEqualTo(LocalDateTime.now().plusSeconds(1));

        assertThat(incidentRepository.findAllByOrderByOccurredAtDesc()).hasSize(1);
        assertThat(attemptRepository.findById(instructorAttempt.getId())
                .orElseThrow().getTabSwitchCount()).isEqualTo(1);
    }

    @Test
    @DisplayName("TC-Mon-02: fullscreen exit incident is saved and increments the attempt count")
    void fullscreenExitIncidentIsSavedToAttempt() throws Exception {
        mockMvc.perform(post(
                        "/api/v1/proctoring/attempts/{attemptId}/incidents",
                        instructorAttempt.getId())
                        .header("Authorization", "Bearer " + studentToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"type\":\"FULLSCREEN_EXIT\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.attemptId").value(instructorAttempt.getId()))
                .andExpect(jsonPath("$.type").value("FULLSCREEN_EXIT"))
                .andExpect(jsonPath("$.occurredAt").isNotEmpty());

        assertThat(incidentRepository.findAllByOrderByOccurredAtDesc()).hasSize(1);
        assertThat(attemptRepository.findById(instructorAttempt.getId())
                .orElseThrow().getFullscreenExitCount()).isEqualTo(1);
    }

    @Test
    @DisplayName("TC-Mon-03: instructor monitoring returns only incidents for their exams")
    void instructorCanViewIncidentsForOwnedExams() throws Exception {
        proctoringService.reportIncident(
                instructorAttempt.getId(), STUDENT_EMAIL, IncidentType.TAB_SWITCH);
        proctoringService.reportIncident(
                otherInstructorAttempt.getId(), STUDENT_EMAIL, IncidentType.FULLSCREEN_EXIT);

        mockMvc.perform(get("/api/v1/instructor/proctoring/incidents")
                        .header("Authorization", "Bearer " + instructorToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].examTitle").value("Instructor Exam"))
                .andExpect(jsonPath("$[0].studentEmail").value(STUDENT_EMAIL))
                .andExpect(jsonPath("$[0].occurredAt").isNotEmpty());

        mockMvc.perform(get("/api/v1/instructor/proctoring/incidents")
                        .header("Authorization", "Bearer " + otherInstructorToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].examTitle").value("Other Exam"));

        mockMvc.perform(get("/api/v1/admin/proctoring/incidents")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2));
    }

    private User saveUser(String name, String email, Role role) {
        return userRepository.save(new User(
                name, email, passwordEncoder.encode("Password@123"), role));
    }

    private Exam saveExam(String title, String createdBy) {
        return examRepository.save(new Exam(
                title,
                "Proctoring test exam",
                30,
                LocalDateTime.now().minusHours(1),
                LocalDateTime.now().plusHours(1),
                40.0,
                0.0,
                false,
                createdBy,
                List.of()));
    }
}
