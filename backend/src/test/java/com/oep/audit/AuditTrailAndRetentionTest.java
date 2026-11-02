package com.oep.audit;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.oep.auth.JwtService;
import com.oep.auth.Role;
import com.oep.auth.User;
import com.oep.auth.UserRepository;
import com.oep.auth.dto.LoginRequest;
import com.oep.auth.dto.RegisterRequest;
import com.oep.exam.*;
import com.oep.grading.dto.GradeQuestionItem;
import com.oep.grading.dto.SubmissionGradeRequest;
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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Sprint 2 / Issue #16: System Audit Trail Logging Interceptor & 3-Year Retention Compliance
 * Traceability:
 * - SRS: OEP-SR-004 (Audit logging of submission and grading actions), OEP-NF-004 (3-Year retention policy)
 * - STP: TC-Sec-04 (Audit log completeness), TC-Ops-01 (3-Year log and result retention verification)
 */
@SpringBootTest
@AutoConfigureMockMvc
public class AuditTrailAndRetentionTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private AuditLogRepository auditLogRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ExamRepository examRepository;

    @Autowired
    private ExamAttemptRepository examAttemptRepository;

    @Autowired
    private QuestionRepository questionRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtService jwtService;

    @Autowired
    private ObjectMapper objectMapper;

    private User adminUser;
    private User instructorUser;
    private User studentUser;

    private String adminToken;
    private String instructorToken;
    private String studentToken;

    @BeforeEach
    void setUp() {
        auditLogRepository.deleteAll();
        examAttemptRepository.deleteAll();
        examRepository.deleteAll();
        questionRepository.deleteAll();
        userRepository.deleteAll();

        adminUser = userRepository.save(new User("Admin User", "admin.audit@oep.com", passwordEncoder.encode("Pass@123"), Role.ADMIN));
        instructorUser = userRepository.save(new User("Prof Smith", "prof.audit@oep.com", passwordEncoder.encode("Pass@123"), Role.INSTRUCTOR));
        studentUser = userRepository.save(new User("Student Bob", "bob.audit@oep.com", passwordEncoder.encode("Pass@123"), Role.STUDENT));

        adminToken = jwtService.generateToken(adminUser);
        instructorToken = jwtService.generateToken(instructorUser);
        studentToken = jwtService.generateToken(studentUser);
    }

    // =========================================================================
    // TC-Sec-04 / OEP-SR-004: Audit Logging of Critical System Actions
    // =========================================================================

    @Test
    @DisplayName("TC-Sec-04: Submission action is automatically logged with student email, timestamp, IP, and status")
    void testExamSubmissionIsAuditLogged() throws Exception {
        Question q = questionRepository.save(new Question(
                "Capital of France?",
                QuestionType.MCQ,
                Difficulty.EASY,
                "Geography",
                Arrays.asList("Paris", "London", "Berlin", "Rome"),
                "Paris",
                2.0,
                0.0,
                "Paris is capital",
                "prof.audit@oep.com"
        ));

        Exam exam = examRepository.save(new Exam(
                "General Knowledge",
                "GK Exam",
                60,
                LocalDateTime.now().minusHours(1),
                LocalDateTime.now().plusHours(1),
                50.0,
                0.0,
                false,
                "prof.audit@oep.com",
                List.of(q)
        ));

        ExamAttempt attempt = new ExamAttempt(exam, "bob.audit@oep.com");
        attempt.setStatus(AttemptStatus.IN_PROGRESS);
        CandidateAnswer ca = new CandidateAnswer(q.getId(), "Paris");
        attempt.getAnswers().add(ca);
        attempt = examAttemptRepository.save(attempt);

        // Submit exam via HTTP POST
        mockMvc.perform(post("/api/v1/exams/" + attempt.getId() + "/submit")
                        .header("Authorization", "Bearer " + studentToken)
                        .header("X-Forwarded-For", "198.51.100.42"))
                .andExpect(status().isOk());

        // Verify audit log entry
        List<AuditLog> logs = auditLogRepository.findByActionOrderByTimestampDesc("EXAM_SUBMISSION");
        assertThat(logs).isNotEmpty();

        AuditLog submissionLog = logs.get(0);
        assertThat(submissionLog.getUserEmail()).isEqualTo("bob.audit@oep.com");
        assertThat(submissionLog.getRole()).isEqualTo("STUDENT");
        assertThat(submissionLog.getHttpMethod()).isEqualTo("POST");
        assertThat(submissionLog.getEndpoint()).isEqualTo("/api/v1/exams/" + attempt.getId() + "/submit");
        assertThat(submissionLog.getIpAddress()).isEqualTo("198.51.100.42");
        assertThat(submissionLog.getHttpStatus()).isEqualTo(200);
        assertThat(submissionLog.getTimestamp()).isNotNull();
        assertThat(submissionLog.getRetentionUntil()).isAfter(LocalDateTime.now().plusYears(2));
    }

    @Test
    @DisplayName("TC-Sec-04: Manual grading action is automatically logged with instructor email and status")
    void testManualGradingIsAuditLogged() throws Exception {
        Question q = questionRepository.save(new Question(
                "Explain polymorphism",
                QuestionType.SHORT_ANSWER,
                Difficulty.MEDIUM,
                "Computer Science",
                Collections.emptyList(),
                "Key OOP concept",
                5.0,
                0.0,
                "Polymorphism explanation",
                "prof.audit@oep.com"
        ));

        Exam exam = examRepository.save(new Exam(
                "Java Basics",
                "Java Exam",
                45,
                LocalDateTime.now().minusHours(2),
                LocalDateTime.now().plusHours(2),
                50.0,
                0.0,
                false,
                "prof.audit@oep.com",
                List.of(q)
        ));

        ExamAttempt attempt = new ExamAttempt(exam, "bob.audit@oep.com");
        attempt.setStatus(AttemptStatus.COMPLETED);
        CandidateAnswer ca = new CandidateAnswer(q.getId(), "Polymorphism means many forms");
        ca.setEvaluated(false);
        attempt.getAnswers().add(ca);
        attempt = examAttemptRepository.save(attempt);

        // Grade submission
        SubmissionGradeRequest gradeReq = new SubmissionGradeRequest(
                List.of(new GradeQuestionItem(q.getId(), 4.5, "Strong answer with good clarity"))
        );

        mockMvc.perform(post("/api/v1/grading/submissions/" + attempt.getId() + "/grade")
                        .header("Authorization", "Bearer " + instructorToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(gradeReq)))
                .andExpect(status().isOk());

        // Verify audit log entry
        List<AuditLog> logs = auditLogRepository.findByActionOrderByTimestampDesc("MANUAL_GRADING");
        assertThat(logs).isNotEmpty();

        AuditLog gradeLog = logs.get(0);
        assertThat(gradeLog.getUserEmail()).isEqualTo("prof.audit@oep.com");
        assertThat(gradeLog.getRole()).isEqualTo("INSTRUCTOR");
        assertThat(gradeLog.getEndpoint()).isEqualTo("/api/v1/grading/submissions/" + attempt.getId() + "/grade");
        assertThat(gradeLog.getHttpStatus()).isEqualTo(200);
    }

    @Test
    @DisplayName("TC-Sec-04: Auth actions (login, register) are automatically recorded in audit trail")
    void testAuthActionsAreAuditLogged() throws Exception {
        // Test Register audit
        RegisterRequest regReq = new RegisterRequest("Newbie", "newbie@oep.com", "Password@123");
        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(regReq)))
                .andExpect(status().isCreated());

        List<AuditLog> regLogs = auditLogRepository.findByActionOrderByTimestampDesc("AUTH_REGISTER");
        assertThat(regLogs).isNotEmpty();
        assertThat(regLogs.get(0).getHttpStatus()).isEqualTo(201);

        // Test Login audit
        LoginRequest loginReq = new LoginRequest("newbie@oep.com", "Password@123");
        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginReq)))
                .andExpect(status().isOk());

        List<AuditLog> loginLogs = auditLogRepository.findByActionOrderByTimestampDesc("AUTH_LOGIN");
        assertThat(loginLogs).isNotEmpty();
        assertThat(loginLogs.get(0).getHttpStatus()).isEqualTo(200);
    }

    // =========================================================================
    // TC-Ops-01 / OEP-NF-004: 3-Year Retention Compliance & Archival Verification
    // =========================================================================

    @Test
    @DisplayName("TC-Ops-01 / OEP-NF-004: Retention report confirms 3-year policy compliance")
    void testRetentionReportCompliance() throws Exception {
        // Seed some sample logs
        AuditLog recentLog = new AuditLog("bob.audit@oep.com", "STUDENT", "EXAM_SUBMISSION", "POST", "/api/v1/exams/1/submit", "127.0.0.1", 200, "Normal submission");
        auditLogRepository.save(recentLog);

        AuditLog historicalLog = new AuditLog("old.student@oep.com", "STUDENT", "EXAM_SUBMISSION", "POST", "/api/v1/exams/1/submit", "127.0.0.1", 200, "Old submission");
        historicalLog.setTimestamp(LocalDateTime.now().minusYears(4)); // 4 years old (past 3-year retention policy)
        auditLogRepository.save(historicalLog);

        mockMvc.perform(get("/api/v1/admin/audit-logs/retention-report")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.compliant").value(true))
                .andExpect(jsonPath("$.requiredRetentionYears").value(3))
                .andExpect(jsonPath("$.totalLogs").value(2))
                .andExpect(jsonPath("$.logsEligibleForArchival").value(1));
    }

    @Test
    @DisplayName("TC-Ops-01 / OEP-NF-004: Archival process marks expired records without deleting compliance data")
    void testArchivalCompliancePreservesData() throws Exception {
        // Record 1: 4 years old (eligible for archival)
        AuditLog log1 = new AuditLog("student1@oep.com", "STUDENT", "EXAM_SUBMISSION", "POST", "/api/v1/exams/1/submit", "127.0.0.1", 200, "Log 1");
        log1.setTimestamp(LocalDateTime.now().minusYears(4));
        auditLogRepository.save(log1);

        // Record 2: 1 year old (within active 3-year retention)
        AuditLog log2 = new AuditLog("student2@oep.com", "STUDENT", "EXAM_SUBMISSION", "POST", "/api/v1/exams/2/submit", "127.0.0.1", 200, "Log 2");
        log2.setTimestamp(LocalDateTime.now().minusYears(1));
        auditLogRepository.save(log2);

        // Run archival via Admin endpoint
        mockMvc.perform(post("/api/v1/admin/audit-logs/archive")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.archivedCount").value(1));

        // Historical data is NOT purged (3-year retention proof), only marked as archived.
        // Interceptor also logs the admin archive operation itself (2 initial + 1 admin operation = 3 total)
        assertThat(auditLogRepository.count()).isEqualTo(3);

        AuditLog refreshed1 = auditLogRepository.findById(log1.getId()).orElseThrow();
        assertThat(refreshed1.isArchived()).isTrue();

        AuditLog refreshed2 = auditLogRepository.findById(log2.getId()).orElseThrow();
        assertThat(refreshed2.isArchived()).isFalse();
    }

    // =========================================================================
    // TC-Admin-01 & RBAC Security Verification
    // =========================================================================

    @Test
    @DisplayName("TC-Admin-01: Admin can query paginated audit logs with action filter")
    void testAdminCanQueryAuditLogsWithFilter() throws Exception {
        auditLogRepository.save(new AuditLog("bob.audit@oep.com", "STUDENT", "EXAM_SUBMISSION", "POST", "/api/v1/exams/1/submit", "127.0.0.1", 200, "Detail 1"));
        auditLogRepository.save(new AuditLog("prof.audit@oep.com", "INSTRUCTOR", "MANUAL_GRADING", "POST", "/api/v1/grading/submissions/1/grade", "127.0.0.1", 200, "Detail 2"));

        mockMvc.perform(get("/api/v1/admin/audit-logs?action=EXAM_SUBMISSION")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(1))
                .andExpect(jsonPath("$.content[0].action").value("EXAM_SUBMISSION"))
                .andExpect(jsonPath("$.content[0].userEmail").value("bob.audit@oep.com"));
    }

    @Test
    @DisplayName("RBAC Security: Non-Admin users are forbidden from accessing audit logs")
    void testNonAdminCannotAccessAuditLogs() throws Exception {
        // Student blocked with 403
        mockMvc.perform(get("/api/v1/admin/audit-logs")
                        .header("Authorization", "Bearer " + studentToken))
                .andExpect(status().isForbidden());

        // Instructor blocked with 403
        mockMvc.perform(get("/api/v1/admin/audit-logs")
                        .header("Authorization", "Bearer " + instructorToken))
                .andExpect(status().isForbidden());

        // Unauthenticated blocked with 403
        mockMvc.perform(get("/api/v1/admin/audit-logs"))
                .andExpect(status().isForbidden());
    }
}
