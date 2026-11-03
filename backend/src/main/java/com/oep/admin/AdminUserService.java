
package com.oep.admin;

import com.oep.admin.dto.AdminUserDTO;
import com.oep.admin.dto.CreateUserRequest;
import com.oep.auth.Role;
import com.oep.auth.User;
import com.oep.auth.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Locale;

@Service
public class AdminUserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public AdminUserService(
            UserRepository userRepository,
            PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional(readOnly = true)
    public List<AdminUserDTO> getAllUsers() {
        return userRepository.findAll()
                .stream()
                .map(AdminUserDTO::new)
                .toList();
    }

    @Transactional
    public AdminUserDTO createUser(CreateUserRequest request) {
        String email = request.getEmail().trim().toLowerCase(Locale.ROOT);

        if (userRepository.existsByEmail(email)) {
            throw new IllegalArgumentException(
                    "A user with this email already exists.");
        }

        User user = new User();
        user.setName(request.getName().trim());
        user.setEmail(email);
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setRole(request.getRole());
        user.setActive(true);

        return new AdminUserDTO(userRepository.save(user));
    }

    @Transactional
    public AdminUserDTO updateRole(
            Long userId, Role newRole, String adminEmail) {
        User user = getUser(userId);

        if (user.getEmail().equalsIgnoreCase(adminEmail)
                && user.getRole() != newRole) {
            throw new IllegalStateException(
                    "You cannot change your own administrator role.");
        }

        user.setRole(newRole);
        return new AdminUserDTO(userRepository.save(user));
    }

    @Transactional
    public AdminUserDTO setActive(
            Long userId, boolean active, String adminEmail) {
        User user = getUser(userId);

        if (!active && user.getEmail().equalsIgnoreCase(adminEmail)) {
            throw new IllegalStateException(
                    "You cannot deactivate your own account.");
        }

        user.setActive(active);
        return new AdminUserDTO(userRepository.save(user));
    }

    private User getUser(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() ->
                        new IllegalArgumentException("User not found."));
    }
}
