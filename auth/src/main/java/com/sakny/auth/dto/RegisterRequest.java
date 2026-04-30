package com.sakny.auth.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Schema(description = "Request object for user registration")
public class RegisterRequest {
    @NotBlank(message = "Name is required")
    @Schema(description = "Full name of the user", example = "John Doe")
    private String name;

    @NotBlank(message = "Email is required")
    @Email(message = "Email should be valid")
    @Schema(description = "Email address of the user", example = "john.doe@example.com")
    private String email;

    @NotBlank(message = "Password is required")
    @Size(min = 6, message = "Password should be at least 6 characters")
    @Schema(description = "User password (minimum 6 characters)", example = "password123")
    private String password;

    @NotBlank(message = "Phone is required")
    @Schema(description = "Phone number of the user", example = "+1234567890")
    private String phone;
}
