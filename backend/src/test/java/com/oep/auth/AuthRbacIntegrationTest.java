package com.oep.auth;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.oep.auth.dto.LoginRequest;
import com.oep.auth.dto.RegisterRequest;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.ResultActions;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Sprint 1 / Issue #5 - Automated integration suite for Authentication & RBAC.
 * Traceability: OEP-F-001/002/003, OEP-SR-002 | TC-Auth-01, TC-Auth-02, TC-Auth-03, TC-Sec-02.
 *
 * Needs the Postgres container (docker compose up -d) - same as CI's service container.
 * WARNING: @BeforeEach wipes the users table, so don't point this at data you care about.
 */
@SpringBootTest
@AutoConfigureMockMvc
class AuthRbacIntegrationTest {

    private static final String PASSWORD = "Password@123";
    private static final String ADMIN_PROBE = "/api/v1/admin/probe";
    private static final String INSTRUCTOR_PROBE = "/api/v1/instructor/probe";
    private static final String STUDENT_PROBE = "/api/v1/student/probe";

    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;
    @Autowired private UserRepository userRepository;
    @Autowired private PasswordEncoder passwordEncoder;
    @Autowired private JwtService jwtService;
    @Value("${app.jwt.secret}") private String jwtSecret;

    @BeforeEach
    void cleanDatabase() {
        userRepository.deleteAll();
    }

    // ------------------------------------------------------------------ helpers

    private ResultActions register(String name, String email, String password, Role role) throws Exception {
        return mockMvc.perform(post("/api/v1/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(new RegisterRequest(name, email, password, role))));
    }

    private ResultActions login(String email, String password) throws Exception {
        return mockMvc.perform(post("/api/v1/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(new LoginRequest(email, password))));
    }

    private String tokenOf(MvcResult result) throws Exception {
        return objectMapper.readTree(result.getResponse().getContentAsString()).get("token").asText();
    }

    /** Inserts a user directly (so RBAC tests don't depend on how /register treats the role field) and logs in. */
    private String seedUserAndLogin(Role role) throws Exception {
        String email = role.name().toLowerCase() + "@oep-test.com";
        userRepository.save(new User("Seeded " + role, email, passwordEncoder.encode(PASSWORD), role));
        return tokenOf(login(email, PASSWORD).andExpect(status().isOk()).andReturn());
    }

    private ResultActions getWithToken(String url, String token) throws Exception {
        return mockMvc.perform(get(url).header("Authorization", "Bearer " + token));
    }

    /** Spring Security's default for anonymous callers can be 401 or 403 depending on config; both mean "blocked". */
    private void assertBlocked(ResultActions actions) throws Exception {
        int code = actions.andReturn().getResponse().getStatus();
        assertThat(code).as("expected request to be blocked (401/403) but got %s", code).isIn(401, 403);
    }

    // ------------------------------------------------- TC-Auth-01 / TC-Sec-02

    @Test
    @DisplayName("TC-Auth-01: student registration returns 201, JWT, and defaults role to STUDENT")
    void registerStudentSuccess() throws Exception {
        register("Asha Rao", "asha@oep-test.com", PASSWORD, Role.STUDENT)
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.token").isNotEmpty())
                .andExpect(jsonPath("$.email").value("asha@oep-test.com"))
                .andExpect(jsonPath("$.role").value("STUDENT"))
                .andExpect(jsonPath("$.password").doesNotExist());

        assertThat(userRepository.findByEmail("asha@oep-test.com")).isPresent();
    }

    @Test
    @DisplayName("TC-Auth-01: registration without a role field defaults to STUDENT")
    void registerWithoutRoleDefaultsToStudent() throws Exception {
        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"No Role\",\"email\":\"norole@oep-test.com\",\"password\":\"" + PASSWORD + "\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.role").value("STUDENT"));
    }

    @Test
    @DisplayName("TC-Auth-01: email is normalised and duplicates (any casing) are rejected")
    void registerRejectsDuplicateEmailCaseInsensitive() throws Exception {
        register("First", "dup@oep-test.com", PASSWORD, Role.STUDENT).andExpect(status().isCreated());
        register("Second", "DUP@OEP-TEST.COM", PASSWORD, Role.STUDENT).andExpect(status().isBadRequest());

        assertThat(userRepository.count()).isEqualTo(1);
        assertThat(userRepository.findByEmail("dup@oep-test.com")).isPresent();
    }

    @Test
    @DisplayName("TC-Auth-01: invalid name/email/short password rejected with field-level details")
    void registerValidationFailures() throws Exception {
        register("", "not-an-email", "123", Role.STUDENT)
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Validation Failed"))
                .andExpect(jsonPath("$.details.name").exists())
                .andExpect(jsonPath("$.details.email").exists())
                .andExpect(jsonPath("$.details.password").exists());

        assertThat(userRepository.count()).isZero();
    }

    @Test
    @DisplayName("TC-Sec-02 / OEP-SR-002: password stored only as a BCrypt salted hash")
    void passwordIsStoredAsBcryptHash() throws Exception {
        register("Hash Check", "hash@oep-test.com", PASSWORD, Role.STUDENT).andExpect(status().isCreated());

        String stored = userRepository.findByEmail("hash@oep-test.com").orElseThrow().getPassword();
        assertThat(stored).isNotEqualTo(PASSWORD);
        assertThat(stored).doesNotContain(PASSWORD);
        assertThat(stored).matches("^\\$2[aby]\\$\\d{2}\\$.{53}$");
        assertThat(passwordEncoder.matches(PASSWORD, stored)).isTrue();
    }

    @Test
    @DisplayName("TC-Sec-02 / OEP-SR-002: same password for two users yields different hashes (salted)")
    void identicalPasswordsGetDifferentSalts() throws Exception {
        register("User One", "one@oep-test.com", PASSWORD, Role.STUDENT).andExpect(status().isCreated());
        register("User Two", "two@oep-test.com", PASSWORD, Role.STUDENT).andExpect(status().isCreated());

        String hash1 = userRepository.findByEmail("one@oep-test.com").orElseThrow().getPassword();
        String hash2 = userRepository.findByEmail("two@oep-test.com").orElseThrow().getPassword();
        assertThat(hash1).isNotEqualTo(hash2);
    }

    // ------------------------------------------------------------ TC-Auth-02

    @Test
    @DisplayName("TC-Auth-02: valid credentials return 200 and a JWT with correct subject, role and expiry")
    void loginIssuesValidJwt() throws Exception {
        register("Login User", "login@oep-test.com", PASSWORD, Role.STUDENT).andExpect(status().isCreated());

        MvcResult result = login("login@oep-test.com", PASSWORD)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").isNotEmpty())
                .andExpect(jsonPath("$.tokenType").value("Bearer"))
                .andExpect(jsonPath("$.role").value("STUDENT"))
                .andReturn();

        String token = tokenOf(result);
        assertThat(jwtService.extractUsername(token)).isEqualTo("login@oep-test.com");
        assertThat(jwtService.extractRole(token)).isEqualTo("STUDENT");
        assertThat(jwtService.extractAllClaims(token).getExpiration()).isAfter(new Date());
        assertThat(jwtService.isTokenValid(token, "login@oep-test.com")).isTrue();
    }

    @Test
    @DisplayName("TC-Auth-02: login is case-insensitive on email")
    void loginIsCaseInsensitiveOnEmail() throws Exception {
        register("Case User", "case@oep-test.com", PASSWORD, Role.STUDENT).andExpect(status().isCreated());
        login("CASE@oep-test.com", PASSWORD).andExpect(status().isOk());
    }

    @Test
    @DisplayName("TC-Auth-02: issued token grants access to /auth/me")
    void issuedTokenWorksOnProtectedProfileEndpoint() throws Exception {
        register("Me User", "me@oep-test.com", PASSWORD, Role.STUDENT).andExpect(status().isCreated());
        String token = tokenOf(login("me@oep-test.com", PASSWORD).andReturn());

        getWithToken("/api/v1/auth/me", token)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value("me@oep-test.com"))
                .andExpect(jsonPath("$.password").doesNotExist());
    }

    @Test
    @DisplayName("TC-Auth-02: wrong password and unknown email both return 401 with the same message")
    void badCredentialsReturn401WithoutUserEnumeration() throws Exception {
        register("Known", "known@oep-test.com", PASSWORD, Role.STUDENT).andExpect(status().isCreated());

        String wrongPw = login("known@oep-test.com", "WrongPassword1")
                .andExpect(status().isUnauthorized())
                .andReturn().getResponse().getContentAsString();
        String unknown = login("ghost@oep-test.com", "WrongPassword1")
                .andExpect(status().isUnauthorized())
                .andReturn().getResponse().getContentAsString();

        assertThat(objectMapper.readTree(wrongPw).get("message").asText())
                .isEqualTo(objectMapper.readTree(unknown).get("message").asText());
    }

    @Test
    @DisplayName("TC-Auth-02: deactivated account cannot log in")
    void deactivatedAccountCannotLogin() throws Exception {
        User user = new User("Inactive", "inactive@oep-test.com", passwordEncoder.encode(PASSWORD), Role.STUDENT);
        user.setActive(false);
        userRepository.save(user);

        login("inactive@oep-test.com", PASSWORD).andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("TC-Auth-02: login with blank fields is a 400 validation error")
    void loginValidation() throws Exception {
        login("", "").andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Validation Failed"));
    }

    // ------------------------------------------------------------ TC-Auth-03

    @ParameterizedTest
    @ValueSource(strings = {ADMIN_PROBE, INSTRUCTOR_PROBE})
    @DisplayName("TC-Auth-03: STUDENT is forbidden from Instructor and Admin endpoints")
    void studentCannotAccessInstructorOrAdmin(String url) throws Exception {
        String studentToken = seedUserAndLogin(Role.STUDENT);
        getWithToken(url, studentToken).andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("TC-Auth-03: STUDENT can still reach student endpoints")
    void studentCanAccessStudentArea() throws Exception {
        String studentToken = seedUserAndLogin(Role.STUDENT);
        getWithToken(STUDENT_PROBE, studentToken).andExpect(status().isOk());
    }

    @Test
    @DisplayName("TC-Auth-03: INSTRUCTOR can access instructor area but not admin or student-only area")
    void instructorAccessMatrix() throws Exception {
        String token = seedUserAndLogin(Role.INSTRUCTOR);
        getWithToken(INSTRUCTOR_PROBE, token).andExpect(status().isOk());
        getWithToken(ADMIN_PROBE, token).andExpect(status().isForbidden());
        getWithToken(STUDENT_PROBE, token).andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("TC-Auth-03: ADMIN can access admin and instructor areas but not student-only area")
    void adminAccessMatrix() throws Exception {
        String token = seedUserAndLogin(Role.ADMIN);
        getWithToken(ADMIN_PROBE, token).andExpect(status().isOk());
        getWithToken(INSTRUCTOR_PROBE, token).andExpect(status().isOk());
        getWithToken(STUDENT_PROBE, token).andExpect(status().isForbidden());
    }

    @ParameterizedTest
    @ValueSource(strings = {ADMIN_PROBE, INSTRUCTOR_PROBE, STUDENT_PROBE})
    @DisplayName("TC-Auth-03: requests with no token are blocked on every role area")
    void anonymousRequestsAreBlocked(String url) throws Exception {
        assertBlocked(mockMvc.perform(get(url)));
    }

    @Test
    @DisplayName("TC-Auth-03: garbage, tampered, forged-role and expired tokens are all rejected")
    void invalidTokensAreRejected() throws Exception {
        // garbage
        assertBlocked(getWithToken(STUDENT_PROBE, "not.a.jwt"));

        // tampered signature (flip first signature char - last char can hide padding bits)
        String valid = seedUserAndLogin(Role.STUDENT);
        String[] parts = valid.split("\\.");
        char first = parts[2].charAt(0);
        String tampered = parts[0] + "." + parts[1] + "." + (first == 'A' ? 'B' : 'A') + parts[2].substring(1);
        assertBlocked(getWithToken(STUDENT_PROBE, tampered));

        // forged token claiming ADMIN, signed with a different key
        SecretKey wrongKey = Keys.hmacShaKeyFor(
                "a-completely-different-secret-key-for-forgery-0123456789".getBytes(StandardCharsets.UTF_8));
        String forged = Jwts.builder()
                .subject("student@oep-test.com").claim("role", "ADMIN")
                .issuedAt(new Date()).expiration(new Date(System.currentTimeMillis() + 60_000))
                .signWith(wrongKey).compact();
        assertBlocked(getWithToken(ADMIN_PROBE, forged));

        // correctly signed but already expired
        SecretKey realKey = Keys.hmacShaKeyFor(jwtSecret.getBytes(StandardCharsets.UTF_8));
        String expired = Jwts.builder()
                .subject("student@oep-test.com").claim("role", "STUDENT")
                .issuedAt(new Date(System.currentTimeMillis() - 120_000))
                .expiration(new Date(System.currentTimeMillis() - 60_000))
                .signWith(realKey).compact();
        assertBlocked(getWithToken(STUDENT_PROBE, expired));
    }
}
