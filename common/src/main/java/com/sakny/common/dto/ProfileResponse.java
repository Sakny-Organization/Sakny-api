
package com.sakny.common.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProfileResponse {
    private Long id;
    private Long userId;
    private String name;
    private String profilePhotoUrl;
    private Integer age;
    private String gender;
    private String occupation;
    private String universityOrSchool;
    private String companyName;
    private LocationDto currentGovernorate;
    private LocationDto currentCity;
    private String bio;
    private String instagram;
    private String linkedin;
    private List<String> personalityTraits;
    private String smoking;
    private String pets;
    private String sleepSchedule;
    private Integer cleanliness;
    private Integer budgetMin;
    private Integer budgetMax;
    private List<PreferredAreaResponse> preferredAreas;
    private String roommateGender;
    private String roommateType;
    private String prefSmoking;
    private String prefPets;
    private String prefSleepSchedule;
    private String prefCleanliness;
    private String additionalNotes;
    private Boolean isComplete;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
