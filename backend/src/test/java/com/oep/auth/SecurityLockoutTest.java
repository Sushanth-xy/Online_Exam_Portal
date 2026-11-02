package com.oep.auth;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.oep.auth.dto.ForgotPasswordRequest;
import com.oep.auth.dto.LoginRequest;
import com.oep.auth.dto.RegisterRequest;
import com.oep.auth.dto.ResetPasswordRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.Date;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Sprint 2 / Issue #15: Security Hardening - Account Lockout & Password Reset
 * Traceability:
 * - SRS: OEP-F-004 (Account lockout after 5 failures), OEP-F-005 (Password reset via 30m link), OEP-SR-001 (Security headers / TLS)
 * - STP: TC-Auth-04 (Account Lockout), TC-Auth-05 (Password Reset)
 */
@SpringBootTest
@AutoConfigureMockMvc
public class SecurityLockoutTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordResetTokenRepository passwordResetTokenRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtService jwtService;

    @Value("${app.jwt.secret}")
    private String jwtSecret;

    @Autowired
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        passwordResetTokenRepository.deleteAll();
        userRepository.deleteAll();
    }

    private void registerUser(String email, String password) throws Exception {
        RegisterRequest reg = new RegisterRequest("Test User", email, password);
        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(reg)))
                .andExpect(status().isCreated());
    }

    // =========================================================================
    // TC-Auth-04 / OEP-F-004: Account Lockout after 5 Failed Attempts
    // =========================================================================

    @Test
    @DisplayName("TC-Auth-04: Failed attempts 1 through 4 increment counter and return 401 Unauthorized")
    void testFailedLoginAttemptsUnderThreshold() throws Exception {
        registerUser("lockout1@test.com", "CorrectPassword123");

        for (int i = 1; i <= 4; i++) {
            mockMvc.perform(post("/api/v1/auth/login")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(new LoginRequest("lockout1@test.com", "WrongPassword"))))
                    .andExpect(status().isUnauthorized())
                    .andExpect(jsonPath("$.error").value("Unauthorized"));

            User user = userRepository.findByEmail("lockout1@test.com").orElseThrow();
            assertThat(user.getFailedLoginAttempts()).isEqualTo(i);
            assertThat(user.isAccountLocked()).isFalse();
        }
    }

    @Test
    @DisplayName("TC-Auth-04: 5th consecutive failed attempt locks account for 15 minutes and returns 423 Locked")
    void testFifthFailedLoginLocksAccount() throws Exception {
        registerUser("lockout2@test.com", "CorrectPassword123");

        for (int i = 1; i <= 4; i++) {
            mockMvc.perform(post("/api/v1/auth/login")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(new LoginRequest("lockout2@test.com", "WrongPassword"))))
                    .andExpect(status().isUnauthorized());
        }

        // 5th failed attempt locks the account
        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new LoginRequest("lockout2@test.com", "WrongPassword"))))
                .andExpect(status().isLocked())
                .andExpect(jsonPath("$.status").value(423))
                .andExpect(jsonPath("$.error").value("Locked"))
                .andExpect(jsonPath("$.message").value(org.hamcrest.Matchers.containsString("Account is locked due to 5 consecutive failed login attempts")));

        User user = userRepository.findByEmail("lockout2@test.com").orElseThrow();
        assertThat(user.getFailedLoginAttempts()).isEqualTo(5);
        assertThat(user.isAccountLocked()).isTrue();
        assertThat(user.getLockedUntil()).isAfter(LocalDateTime.now().plusMinutes(14));
    }

    @Test
    @DisplayName("TC-Auth-04: 6th attempt while locked is blocked even when providing the correct password")
    void testSixthAttemptBlockedEvenWithCorrectPassword() throws Exception {
        registerUser("lockout3@test.com", "CorrectPassword123");

        for (int i = 1; i <= 5; i++) {
            mockMvc.perform(post("/api/v1/auth/login")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(new LoginRequest("lockout3@test.com", "WrongPassword"))));
        }

        // 6th attempt with correct password
        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new LoginRequest("lockout3@test.com", "CorrectPassword123"))))
                .andExpect(status().isLocked())
                .andExpect(jsonPath("$.status").value(423))
                .andExpect(jsonPath("$.message").value(org.hamcrest.Matchers.containsString("Account is locked")));
    }

    @Test
    @DisplayName("TC-Auth-04: Successful login resets failed attempts counter back to zero")
    void testSuccessfulLoginResetsCounter() throws Exception {
        registerUser("lockout4@test.com", "CorrectPassword123");

        // 3 failed attempts
        for (int i = 1; i <= 3; i++) {
            mockMvc.perform(post("/api/v1/auth/login")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(new LoginRequest("lockout4@test.com", "WrongPassword"))))
                    .andExpect(status().isUnauthorized());
        }

        User beforeSuccess = userRepository.findByEmail("lockout4@test.com").orElseThrow();
        assertThat(beforeSuccess.getFailedLoginAttempts()).isEqualTo(3);

        // Valid login
        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new LoginRequest("lockout4@test.com", "CorrectPassword123"))))
                .andExpect(status().isOk());

        User afterSuccess = userRepository.findByEmail("lockout4@test.com").orElseThrow();
        assertThat(afterSuccess.getFailedLoginAttempts()).isZero();
        assertThat(afterSuccess.getLockedUntil()).isNull();
    }

    @Test
    @DisplayName("TC-Auth-04: Expired lockout automatically unlocks account on next valid login")
    void testExpiredLockoutUnlocksOnLogin() throws Exception {
        registerUser("lockout5@test.com", "CorrectPassword123");

        User user = userRepository.findByEmail("lockout5@test.com").orElseThrow();
        user.setFailedLoginAttempts(5);
        user.setLockedUntil(LocalDateTime.now().minusMinutes(5)); // Lockout already passed
        userRepository.save(user);

        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new LoginRequest("lockout5@test.com", "CorrectPassword123"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").isNotEmpty());

        User refreshed = userRepository.findByEmail("lockout5@test.com").orElseThrow();
        assertThat(refreshed.getFailedLoginAttempts()).isZero();
        assertThat(refreshed.getLockedUntil()).isNull();
    }

    // =========================================================================
    // TC-Auth-05 / OEP-F-005: Password Reset Workflow (30-Minute Token)
    // =========================================================================

    @Test
    @DisplayName("TC-Auth-05: Forgot password generates a 30-minute valid token")
    void testForgotPasswordGeneratesToken() throws Exception {
        registerUser("reset1@test.com", "OldPassword123");

        ForgotPasswordRequest request = new ForgotPasswordRequest("reset1@test.com");

        MvcResult result = mockMvc.perform(post("/api/v1/auth/forgot-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.resetToken").isNotEmpty())
                .andExpect(jsonPath("$.expiresInMinutes").value(30))
                .andReturn();

        String token = objectMapper.readTree(result.getResponse().getContentAsString()).get("resetToken").asText();
        PasswordResetToken storedToken = passwordResetTokenRepository.findByToken(token).orElseThrow();

        assertThat(storedToken.isValid()).isTrue();
        assertThat(storedToken.getUser().getEmail()).isEqualTo("reset1@test.com");
        assertThat(storedToken.getExpiryDate()).isAfter(LocalDateTime.now().plusMinutes(28));
    }

    @Test
    @DisplayName("TC-Auth-05: Reset password updates password and enables immediate login with new credentials")
    void testResetPasswordWorkflow() throws Exception {
        registerUser("reset2@test.com", "OldPassword123");

        // Step 1: Request reset
        MvcResult forgotResult = mockMvc.perform(post("/api/v1/auth/forgot-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new ForgotPasswordRequest("reset2@test.com"))))
                .andExpect(status().isOk())
                .andReturn();

        String token = objectMapper.readTree(forgotResult.getResponse().getContentAsString()).get("resetToken").asText();

        // Step 2: Reset password
        ResetPasswordRequest resetReq = new ResetPasswordRequest(token, "BrandNewPassword123");
        mockMvc.perform(post("/api/v1/auth/reset-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(resetReq)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value("reset2@test.com"));

        // Step 3: Old password must fail
        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new LoginRequest("reset2@test.com", "OldPassword123"))))
                .andExpect(status().isUnauthorized());

        // Step 4: New password must succeed
        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new LoginRequest("reset2@test.com", "BrandNewPassword123"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").isNotEmpty());
    }

    @Test
    @DisplayName("TC-Auth-05: Reset password immediately unlocks a locked account")
    void testResetPasswordUnlocksLockedAccount() throws Exception {
        registerUser("locked_reset@test.com", "OldPassword123");

        // Lock the account with 5 failed attempts
        for (int i = 0; i < 5; i++) {
            mockMvc.perform(post("/api/v1/auth/login")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(new LoginRequest("locked_reset@test.com", "WrongPw"))));
        }

        User lockedUser = userRepository.findByEmail("locked_reset@test.com").orElseThrow();
        assertThat(lockedUser.isAccountLocked()).isTrue();

        // Request reset and execute reset
        MvcResult forgotResult = mockMvc.perform(post("/api/v1/auth/forgot-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new ForgotPasswordRequest("locked_reset@test.com"))))
                .andExpect(status().isOk())
                .andReturn();

        String token = objectMapper.readTree(forgotResult.getResponse().getContentAsString()).get("resetToken").asText();

        mockMvc.perform(post("/api/v1/auth/reset-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new ResetPasswordRequest(token, "NewUnlockedPassword@1"))))
                .andExpect(status().isOk());

        // Account must now be unlocked and accept new password
        User unlockedUser = userRepository.findByEmail("locked_reset@test.com").orElseThrow();
        assertThat(unlockedUser.isAccountLocked()).isFalse();
        assertThat(unlockedUser.getFailedLoginAttempts()).isZero();

        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new LoginRequest("locked_reset@test.com", "NewUnlockedPassword@1"))))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("TC-Auth-05: Reset token cannot be reused after being used once")
    void testResetTokenCannotBeReused() throws Exception {
        registerUser("reuse@test.com", "OldPassword123");

        MvcResult forgotResult = mockMvc.perform(post("/api/v1/auth/forgot-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new ForgotPasswordRequest("reuse@test.com"))))
                .andExpect(status().isOk())
                .andReturn();

        String token = objectMapper.readTree(forgotResult.getResponse().getContentAsString()).get("resetToken").asText();

        // First use: success
        mockMvc.perform(post("/api/v1/auth/reset-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new ResetPasswordRequest(token, "FirstNewPass123"))))
                .andExpect(status().isOk());

        // Second use: 400 Bad Request
        mockMvc.perform(post("/api/v1/auth/reset-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new ResetPasswordRequest(token, "SecondNewPass123"))))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value(org.hamcrest.Matchers.containsString("already been used")));
    }

    @Test
    @DisplayName("TC-Auth-05: Expired reset token is rejected with 400 Bad Request")
    void testExpiredResetTokenRejected() throws Exception {
        registerUser("expired@test.com", "OldPassword123");

        User user = userRepository.findByEmail("expired@test.com").orElseThrow();
        PasswordResetToken expiredToken = new PasswordResetToken(
                "expired-token-12345",
                user,
                LocalDateTime.now().minusMinutes(2)
        );
        passwordResetTokenRepository.save(expiredToken);

        mockMvc.perform(post("/api/v1/auth/reset-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new ResetPasswordRequest("expired-token-12345", "NewPassword123"))))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value(org.hamcrest.Matchers.containsString("expired")));
    }

    @Test
    @DisplayName("TC-Auth-05: Non-existent email for forgot password returns 400 Bad Request")
    void testForgotPasswordUnknownEmail() throws Exception {
        mockMvc.perform(post("/api/v1/auth/forgot-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new ForgotPasswordRequest("nobody@example.com"))))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value(org.hamcrest.Matchers.containsString("No account found with email")));
    }

    // =========================================================================
    // OEP-SR-001: Security Headers Verification
    // =========================================================================

    @Test
    @DisplayName("OEP-SR-001: Security headers (X-Content-Type-Options, X-Frame-Options) are enforced on responses")
    void testSecurityHeadersEnforced() throws Exception {
        mockMvc.perform(get("/api/v1/health"))
                .andExpect(status().isOk())
                .andExpect(header().string("X-Content-Type-Options", "nosniff"))
                .andExpect(header().string("X-Frame-Options", "DENY"));
    }

    // =========================================================================
    // TC-Sec-05 / OEP-SR-005: Inactivity / Token Expiry Verification
    // =========================================================================

    @Test
    @DisplayName("TC-Sec-05: Expired JWT tokens are rejected as invalid")
    void testExpiredJwtTokensRejected() {
        SecretKey key = Keys.hmacShaKeyFor(jwtSecret.getBytes(StandardCharsets.UTF_8));
        String expiredToken = Jwts.builder()
                .subject("expireduser@example.com")
                .issuedAt(new Date(System.currentTimeMillis() - 7200_000))
                .expiration(new Date(System.currentTimeMillis() - 3600_000))
                .signWith(key)
                .compact();

        assertThat(jwtService.isTokenValid(expiredToken, "expireduser@example.com")).isFalse();
    }
}
