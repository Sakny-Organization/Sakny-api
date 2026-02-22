package com.sakny.common.dto;

import com.sakny.common.model.*;
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
public class ProfileRequest {

    // === Step 1: Basics ===

    @NotNull(message = "Age is required")
    @Min(value = 18, message = "Age must be at least 18")
    @Max(value = 100, message = "Age must be at most 100")
    private Integer age;

    @NotNull(message = "Gender is required")
    private Gender gender;

    private String occupation;

    private String universityOrSchool;

    private String companyName;

    private Integer currentGovernorateId;

    private Integer currentCityId;

    @Size(max = 500, message = "Bio must be at most 500 characters")
    private String bio;

    private String instagram;

    private String linkedin;

    // === Step 2: Personality ===

    @NotEmpty(message = "At least one personality trait is required")
    private List<String> personalityTraits;

    // === Step 3: Lifestyle ===

    @NotNull(message = "Smoking status is required")
    private SmokingStatus smoking;

    @NotNull(message = "Pet status is required")
    private PetStatus pets;

    @NotNull(message = "Sleep schedule is required")
    private SleepSchedule sleepSchedule;

    @Min(value = 1, message = "Cleanliness must be between 1 and 5")
    @Max(value = 5, message = "Cleanliness must be between 1 and 5")
    @Builder.Default
    private Integer cleanliness = 3;

    // === Step 4: Budget ===

    @NotNull(message = "Minimum budget is required")
    @Min(value = 500, message = "Minimum budget is 500 EGP")
    private Integer budgetMin;

    @NotNull(message = "Maximum budget is required")
    @Max(value = 20000, message = "Maximum budget is 20000 EGP")
    private Integer budgetMax;

    // === Step 5: Location ===

    @NotEmpty(message = "At least one preferred area is required")
    @Size(max = 5, message = "Maximum 5 preferred areas allowed")
    @Valid
    private List<PreferredAreaRequest> preferredAreas;

    // === Step 6: Roommate Preferences ===

    @NotNull(message = "Roommate type preference is required")
    private RoommateType roommateType;

    private SmokingPreference prefSmoking;

    private PetPreference prefPets;

    private SleepSchedulePreference prefSleepSchedule;

    private CleanlinessPreference prefCleanliness;

    @Size(max = 300, message = "Additional notes must be at most 300 characters")
    private String additionalNotes;
}
