package com.sakny.common.dto;

import com.sakny.common.model.*;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Request object for creating or completing a user profile (Wizard steps)")
public class ProfileRequest {

    // === Step 1: Basics ===

    @NotNull(message = "Age is required")
    @Min(value = 18, message = "Age must be at least 18")
    @Max(value = 100, message = "Age must be at most 100")
    @Schema(description = "User age", example = "25")
    private Integer age;

    @NotNull(message = "Gender is required")
    @Schema(description = "User gender", example = "MALE")
    private Gender gender;

    @Schema(description = "User occupation", example = "Software Engineer")
    private String occupation;

    @Schema(description = "University or School name", example = "Cairo University")
    private String universityOrSchool;

    @Schema(description = "Company Name", example = "Tech Solutions")
    private String companyName;

    @Schema(description = "Governorate ID for current home location", example = "1")
    private Integer currentGovernorateId;

    @Schema(description = "City ID for current home location", example = "101")
    private Integer currentCityId;

    @Size(max = 500, message = "Bio must be at most 500 characters")
    @Schema(description = "Brief user bio", example = "I am a quiet person who loves books.")
    private String bio;

    @Schema(description = "Instagram username", example = "john_doe")
    private String instagram;

    @Schema(description = "LinkedIn profile URL or username", example = "john-doe-123")
    private String linkedin;

    // === Step 2: Personality ===

    @NotEmpty(message = "At least one personality trait is required")
    @Schema(description = "List of personality traits", example = "[\"QUIET\", \"SOCIAL\", \"ORGANIZED\"]")
    private List<String> personalityTraits;

    // === Step 3: Lifestyle ===

    @NotNull(message = "Smoking status is required")
    @Schema(description = "Smoking status", example = "NON_SMOKER")
    private SmokingStatus smoking;

    @NotNull(message = "Pet status is required")
    @Schema(description = "Pet ownership status", example = "NO_PETS")
    private PetStatus pets;

    @NotNull(message = "Sleep schedule is required")
    @Schema(description = "Sleep schedule habits", example = "EARLY_BIRD")
    private SleepSchedule sleepSchedule;

    @Min(value = 1, message = "Cleanliness must be between 1 and 5")
    @Max(value = 5, message = "Cleanliness must be between 1 and 5")
    @Builder.Default
    @Schema(description = "Cleanliness rating from 1 (Messy) to 5 (Extremely Clean)", example = "4")
    private Integer cleanliness = 3;

    // === Step 4: Budget ===

    @NotNull(message = "Minimum budget is required")
    @Min(value = 500, message = "Minimum budget is 500 EGP")
    @Schema(description = "Minimum monthly budget in EGP", example = "2000")
    private Integer budgetMin;

    @NotNull(message = "Maximum budget is required")
    @Max(value = 20000, message = "Maximum budget is 20000 EGP")
    @Schema(description = "Maximum monthly budget in EGP", example = "5000")
    private Integer budgetMax;

    // === Step 5: Location ===

    @NotEmpty(message = "At least one preferred area is required")
    @Size(max = 5, message = "Maximum 5 preferred areas allowed")
    @Valid
    @Schema(description = "List of areas where the user would like to live")
    private List<PreferredAreaRequest> preferredAreas;

    // === Step 6: Roommate Preferences ===

    @NotNull(message = "Roommate type preference is required")
    @Schema(description = "Preferred roommate type (e.g., SAME_GENDER, MIXED)", example = "SAME_GENDER")
    private RoommateType roommateType;

    @Schema(description = "Preference regarding smoking roommates")
    private SmokingPreference prefSmoking;

    @Schema(description = "Preference regarding roommates with pets")
    private PetPreference prefPets;

    @Schema(description = "Preference regarding roommates' sleep schedule")
    private SleepSchedulePreference prefSleepSchedule;

    @Schema(description = "Preference regarding roommates' cleanliness level")
    private CleanlinessPreference prefCleanliness;

    @Size(max = 300, message = "Additional notes must be at most 300 characters")
    @Schema(description = "Any extra notes about roommate preferences", example = "Looking for someone who respects privacy.")
    private String additionalNotes;
}
