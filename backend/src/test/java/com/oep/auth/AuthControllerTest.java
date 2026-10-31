
package com.oep.auth;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.oep.auth.dto.LoginRequest;
import com.oep.auth.dto.RegisterRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
public class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        userRepository.deleteAll();
    }

    @Test
    @DisplayName("TC-Auth-01: Valid registration creates a student account and issues JWT")
    void testRegisterSuccess() throws Exception {
        RegisterRequest request = new RegisterRequest(
                "Sushanth B",
                "sushanth@example.com",
                "Password@123"
        );

        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.token").isNotEmpty())
                .andExpect(jsonPath("$.name").value("Sushanth B"))
                .andExpect(jsonPath("$.email").value("sushanth@example.com"))
                .andExpect(jsonPath("$.role").value("STUDENT"));

        User savedUser = userRepository.findByEmail("sushanth@example.com").orElseThrow();
        assertThat(savedUser.getPassword()).isNotEqualTo("Password@123");
        assertThat(savedUser.getPassword()).startsWith("$2a$");
        assertThat(savedUser.getRole()).isEqualTo(Role.STUDENT);
    }

    @Test
    @DisplayName("Public registration cannot assign an instructor or admin role")
    void testRegistrationCannotAssignPrivilegedRole() throws Exception {
        String request = """
                {
                    "name": "Malicious User",
                    "email": "malicious@example.com",
                    "password": "Password@123",
                    "role": "ADMIN"
                }
                """;

        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(request))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.role").value("STUDENT"));

        User savedUser = userRepository.findByEmail("malicious@example.com").orElseThrow();
        assertThat(savedUser.getRole()).isEqualTo(Role.STUDENT);
    }

    @Test
    @DisplayName("Registration rejects duplicate email with 400 Bad Request")
    void testRegisterDuplicateEmail() throws Exception {
        RegisterRequest request = new RegisterRequest(
                "Alice",
                "alice@example.com",
                "Secret@123"
        );

        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated());

        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Bad Request"));
    }

    @Test
    @DisplayName("Registration validates required fields and password length")
    void testRegisterValidationFailure() throws Exception {
        RegisterRequest invalidRequest = new RegisterRequest(
                "",
                "invalid-email",
                "123"
        );

        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Validation Failed"));
    }

    @Test
    @DisplayName("TC-Auth-02: Valid credentials authenticate user and return JWT bearer token")
    void testLoginSuccess() throws Exception {
        RegisterRequest reg = new RegisterRequest(
                "Bob",
                "bob@example.com",
                "SecretPass123"
        );

        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(reg)))
                .andExpect(status().isCreated());

        LoginRequest login = new LoginRequest("bob@example.com", "SecretPass123");

        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(login)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").isNotEmpty())
                .andExpect(jsonPath("$.email").value("bob@example.com"))
                .andExpect(jsonPath("$.role").value("STUDENT"));
    }

    @Test
    @DisplayName("TC-Auth-02: Incorrect password is rejected with 401 Unauthorized")
    void testLoginIncorrectPassword() throws Exception {
        RegisterRequest reg = new RegisterRequest(
                "Carol",
                "carol@example.com",
                "CorrectPass1"
        );

        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(reg)))
                .andExpect(status().isCreated());

        LoginRequest wrongLogin = new LoginRequest("carol@example.com", "WrongPassword");

        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(wrongLogin)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.error").value("Unauthorized"));
    }

    @Test
    @DisplayName("Authenticated endpoint /api/v1/auth/me returns profile with valid JWT")
    void testGetCurrentUserProfile() throws Exception {
        RegisterRequest reg = new RegisterRequest(
                "Dave",
                "dave@example.com",
                "SecureDave123"
        );

        MvcResult regResult = mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(reg)))
                .andExpect(status().isCreated())
                .andReturn();

        String responseBody = regResult.getResponse().getContentAsString();
        String token = objectMapper.readTree(responseBody).get("token").asText();

        mockMvc.perform(get("/api/v1/auth/me")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Dave"))
                .andExpect(jsonPath("$.email").value("dave@example.com"))
                .andExpect(jsonPath("$.role").value("STUDENT"));

        mockMvc.perform(get("/api/v1/auth/me"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("TC-Auth-03: RBAC middleware blocks STUDENT from Instructor or Admin endpoints")
    void testRbacStudentAccessBlocked() throws Exception {
        RegisterRequest studentReg = new RegisterRequest(
                "Student User",
                "student.rbac@example.com",
                "Password@123"
        );

        MvcResult regResult = mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(studentReg)))
                .andExpect(status().isCreated())
                .andReturn();

        String token = objectMapper.readTree(regResult.getResponse().getContentAsString())
                .get("token").asText();

        mockMvc.perform(get("/api/v1/admin/dashboard")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isForbidden());
    }
}
