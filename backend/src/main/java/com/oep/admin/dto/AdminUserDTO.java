
package com.oep.admin.dto;

import com.oep.auth.User;
import java.time.LocalDateTime;

public class AdminUserDTO {
    private Long id;
    private String name;
    private String email;
    private String role;
    private boolean active;
    private LocalDateTime createdAt;

    public AdminUserDTO(User user) {
        this.id = user.getId();
        this.name = user.getName();
        this.email = user.getEmail();
        this.role = user.getRole().name();
        this.active = user.isActive();
        this.createdAt = user.getCreatedAt();
    }

    public Long getId() { return id; }
    public String getName() { return name; }
    public String getEmail() { return email; }
    public String getRole() { return role; }
    public boolean isActive() { return active; }
    public LocalDateTime getCreatedAt() { return createdAt; }
}
