package com.oep.auth;

import com.oep.auth.dto.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.LockedException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
public class AuthService {

    private static final Logger log = LoggerFactory.getLogger(AuthService.class);

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final PasswordResetTokenRepository passwordResetTokenRepository;

    public AuthService(UserRepository userRepository,
                       PasswordEncoder passwordEncoder,
                       JwtService jwtService,
                       PasswordResetTokenRepository passwordResetTokenRepository) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
        this.passwordResetTokenRepository = passwordResetTokenRepository;
    }

    @Transactional
    public AuthResponse register(RegisterRequest request) {
        String normalizedEmail = request.getEmail().trim().toLowerCase();

        if (userRepository.existsByEmail(normalizedEmail)) {
            throw new IllegalArgumentException("Email already in use: " + request.getEmail());
        }

        Role assignedRole = Role.STUDENT;

        User user = new User(
                request.getName().trim(),
                normalizedEmail,
                passwordEncoder.encode(request.getPassword()),
                assignedRole
        );

        User savedUser = userRepository.save(user);
        String token = jwtService.generateToken(savedUser);

        return new AuthResponse(
                token,
                savedUser.getId(),
                savedUser.getName(),
                savedUser.getEmail(),
                savedUser.getRole()
        );
    }

    @Transactional(noRollbackFor = {BadCredentialsException.class, LockedException.class})
    public AuthResponse login(LoginRequest request) {
        String normalizedEmail = request.getEmail().trim().toLowerCase();

        User user = userRepository.findByEmail(normalizedEmail)
                .orElseThrow(() -> new BadCredentialsException("Invalid email or password"));

        if (!user.isActive()) {
            throw new BadCredentialsException("Account is deactivated. Please contact administrator.");
        }

        // Check if account is temporarily locked
        if (user.getLockedUntil() != null) {
            if (user.getLockedUntil().isAfter(LocalDateTime.now())) {
                log.warn("Login attempt rejected for locked account: {}", normalizedEmail);
                throw new LockedException("Account is locked due to 5 consecutive failed login attempts. Please try again after 15 minutes or reset your password.");
            } else {
                // Lockout has elapsed, auto-unlock account
                user.resetLockout();
                userRepository.save(user);
            }
        }

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            user.recordFailedLogin();
            userRepository.save(user);

            if (user.isAccountLocked() || user.getFailedLoginAttempts() >= 5) {
                log.warn("Account {} locked after {} consecutive failed login attempts. Notification email dispatched.",
                        normalizedEmail, user.getFailedLoginAttempts());
                throw new LockedException("Account is locked due to 5 consecutive failed login attempts. Please try again after 15 minutes or reset your password.");
            }
            throw new BadCredentialsException("Invalid email or password");
        }

        // Reset failed login attempts on successful login
        user.resetLockout();
        userRepository.save(user);

        String token = jwtService.generateToken(user);

        return new AuthResponse(
                token,
                user.getId(),
                user.getName(),
                user.getEmail(),
                user.getRole()
        );
    }

    @Transactional
    public ForgotPasswordResponse requestPasswordReset(ForgotPasswordRequest request) {
        String normalizedEmail = request.getEmail().trim().toLowerCase();
        User user = userRepository.findByEmail(normalizedEmail)
                .orElseThrow(() -> new IllegalArgumentException("No account found with email: " + request.getEmail()));

        // Invalidate prior reset tokens for user
        passwordResetTokenRepository.deleteByUser(user);

        String token = UUID.randomUUID().toString();
        LocalDateTime expiry = LocalDateTime.now().plusMinutes(30);

        PasswordResetToken resetToken = new PasswordResetToken(token, user, expiry);
        passwordResetTokenRepository.save(resetToken);

        log.info("Dispatched password reset token link to {} (valid for 30 minutes).", normalizedEmail);

        return new ForgotPasswordResponse(
                "Password reset link and instructions sent successfully.",
                token,
                30L
        );
    }

    @Transactional
    public ResetPasswordResponse resetPassword(ResetPasswordRequest request) {
        PasswordResetToken resetToken = passwordResetTokenRepository.findByToken(request.getToken().trim())
                .orElseThrow(() -> new IllegalArgumentException("Invalid password reset token"));

        if (resetToken.isUsed()) {
            throw new IllegalStateException("Password reset token has already been used");
        }

        if (resetToken.isExpired()) {
            throw new IllegalStateException("Password reset token has expired. Please request a new link.");
        }

        User user = resetToken.getUser();
        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        // Successful password reset immediately clears any active lockout
        user.resetLockout();
        userRepository.save(user);

        resetToken.setUsed(true);
        passwordResetTokenRepository.save(resetToken);

        log.info("Password successfully reset and lockout cleared for user: {}", user.getEmail());

        return new ResetPasswordResponse(
                "Password has been reset successfully. You can now log in with your new password.",
                user.getEmail()
        );
    }

    @Transactional
    public void unlockAccount(String email) {
        User user = getUserByEmail(email);
        user.resetLockout();
        userRepository.save(user);
        log.info("Account unlocked administratively for user: {}", email);
    }

    public User getUserByEmail(String email) {
        return userRepository.findByEmail(email.trim().toLowerCase())
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + email));
    }
}
