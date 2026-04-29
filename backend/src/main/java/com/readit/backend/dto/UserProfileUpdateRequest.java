package com.readit.backend.dto;

import jakarta.validation.constraints.Email;
import lombok.*;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class UserProfileUpdateRequest {
    private String name;
    private String fullName;
    private String phone;
    private String address;

    @Email(message = "Invalid email format")
    private String email;

    private String currentPassword;
    private String newPassword;
}
