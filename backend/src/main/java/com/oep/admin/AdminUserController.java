
package com.oep.admin;

import com.oep.admin.dto.AdminUserDTO;
import com.oep.admin.dto.CreateUserRequest;
import com.oep.auth.Role;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/admin/users")
@PreAuthorize("hasRole('ADMIN')")
public class AdminUserController {

    private final AdminUserService adminUserService;

    public AdminUserController(AdminUserService adminUserService) {
        this.adminUserService = adminUserService;
    }

    @GetMapping
    public ResponseEntity<List<AdminUserDTO>> getAllUsers() {
        return ResponseEntity.ok(adminUserService.getAllUsers());
    }

    @PostMapping
    public ResponseEntity<AdminUserDTO> createUser(
            @Valid @RequestBody CreateUserRequest request) {
        AdminUserDTO created = adminUserService.createUser(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @PatchMapping("/{userId}/role")
    public ResponseEntity<AdminUserDTO> updateRole(
            @PathVariable Long userId,
            @RequestBody Map<String, String> request,
            Authentication authentication) {

        String roleValue = request.get("role");

        if (roleValue == null || roleValue.isBlank()) {
            throw new IllegalArgumentException("Role is required.");
        }

        Role role;
        try {
            role = Role.valueOf(roleValue.trim().toUpperCase());
        } catch (IllegalArgumentException ex) {
            throw new IllegalArgumentException(
                    "Invalid role. Use STUDENT, INSTRUCTOR, or ADMIN.");
        }

        return ResponseEntity.ok(
                adminUserService.updateRole(
                        userId, role, authentication.getName()));
    }

    @PatchMapping("/{userId}/status")
    public ResponseEntity<AdminUserDTO> updateStatus(
            @PathVariable Long userId,
            @RequestBody Map<String, Boolean> request,
            Authentication authentication) {

        Boolean active = request.get("active");

        if (active == null) {
            throw new IllegalArgumentException(
                    "The active field is required.");
        }

        return ResponseEntity.ok(
                adminUserService.setActive(
                        userId, active, authentication.getName()));
    }
}
