package com.sakny.user.entity;

import com.sakny.common.model.*;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "user_profiles")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserProfile {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User user;

    @Column(nullable = false)
    private Integer age;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private Gender gender;

    @Column(length = 50)
    private String occupation;

    @Column(name = "university_or_school")
    private String universityOrSchool;

    @Column(name = "company_name")
    private String companyName;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "current_governorate_id")
    private Governorate currentGovernorate;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "current_city_id")
    private City currentCity;

    @Column(name = "profile_photo_url", length = 500)
    private String profilePhotoUrl;

    @Column(length = 500)
    private String bio;

    @Column(length = 100)
    private String instagram;

    @Column(length = 255)
    private String linkedin;

    @Column(name = "personality_traits", nullable = false, columnDefinition = "TEXT")
    private String personalityTraits;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private SmokingStatus smoking;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private PetStatus pets;

    @Enumerated(EnumType.STRING)
    @Column(name = "sleep_schedule", nullable = false, length = 20)
    private SleepSchedule sleepSchedule;

    @Column(nullable = false)
    @Builder.Default
    private Integer cleanliness = 3;

    @Column(name = "budget_min", nullable = false)
    private Integer budgetMin;

    @Column(name = "budget_max", nullable = false)
    private Integer budgetMax;

    @Enumerated(EnumType.STRING)
    @Column(name = "roommate_gender", nullable = false, length = 10)
    private Gender roommateGender;

    @Enumerated(EnumType.STRING)
    @Column(name = "roommate_type", nullable = false, length = 30)
    private RoommateType roommateType;

    @Enumerated(EnumType.STRING)
    @Column(name = "pref_smoking", length = 30)
    private SmokingPreference prefSmoking;

    @Enumerated(EnumType.STRING)
    @Column(name = "pref_pets", length = 30)
    private PetPreference prefPets;

    @Enumerated(EnumType.STRING)
    @Column(name = "pref_sleep_schedule", length = 20)
    private SleepSchedulePreference prefSleepSchedule;

    @Enumerated(EnumType.STRING)
    @Column(name = "pref_cleanliness", length = 20)
    private CleanlinessPreference prefCleanliness;

    @Column(name = "additional_notes", length = 300)
    private String additionalNotes;

    @Column(name = "is_complete", nullable = false)
    @Builder.Default
    private Boolean isComplete = false;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @OneToMany(mappedBy = "profile", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @Builder.Default
    private List<PreferredArea> preferredAreas = new ArrayList<>();

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    public void addPreferredArea(PreferredArea area) {
        preferredAreas.add(area);
        area.setProfile(this);
    }

    public void clearPreferredAreas() {
        preferredAreas.clear();
    }
}
