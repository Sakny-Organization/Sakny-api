package com.sakny.auth.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Schema(description = "Request object for user authentication")
public class AuthenticationRequest {
    @Schema(description = "User email address", example = "john.doe@example.com")
    private String email;
    
    @Schema(description = "User password", example = "password123")
    private String password;
}
