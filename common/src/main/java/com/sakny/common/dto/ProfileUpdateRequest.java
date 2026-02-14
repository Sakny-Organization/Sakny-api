package com.sakny.common.dto;

import com.sakny.common.model.*;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * DTO for partial profile updates.
 * All fields are optional - only provided fields will be updated.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProfileUpdateRequest {

    // === Step 1: Basics ===

    @Min(value = 18, message = "Age must be at least 18")
    @Max(value = 100, message = "Age must be at most 100")
    private Integer age;

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

    private List<String> personalityTraits;

    // === Step 3: Lifestyle ===

    private SmokingStatus smoking;

    private PetStatus pets;

    private SleepSchedule sleepSchedule;

    @Min(value = 1, message = "Cleanliness must be between 1 and 5")
    @Max(value = 5, message = "Cleanliness must be between 1 and 5")
    private Integer cleanliness;

    // === Step 4: Budget ===

    @Min(value = 500, message = "Minimum budget is 500 EGP")
    private Integer budgetMin;

    @Max(value = 20000, message = "Maximum budget is 20000 EGP")
    private Integer budgetMax;

    // === Step 5: Location ===

    @Size(max = 5, message = "Maximum 5 preferred areas allowed")
    @Valid
    private List<PreferredAreaRequest> preferredAreas;

    // === Step 6: Roommate Preferences ===

    private RoommateType roommateType;

    private SmokingPreference prefSmoking;

    private PetPreference prefPets;

    private SleepSchedulePreference prefSleepSchedule;

    private CleanlinessPreference prefCleanliness;

    @Size(max = 300, message = "Additional notes must be at most 300 characters")
    private String additionalNotes;
}

