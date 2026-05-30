package com.sakny.auth.dto;

import com.sakny.common.model.HousingRole;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Schema(description = "Response object containing authentication token")
public class AuthenticationResponse {
    @Schema(description = "JWT authentication token", example = "eyJhbGciOiJIUzI1NiJ9...")
    private String token;

    @Schema(description = "Housing role of the user", example = "ROOMMATE")
    private HousingRole housingRole;

    @Schema(description = "Whether the user has completed their profile setup")
    private Boolean profileCompleted;
}
