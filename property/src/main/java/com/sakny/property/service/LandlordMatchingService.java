package com.sakny.property.service;

import com.sakny.common.model.*;
import com.sakny.property.entity.Property;
import com.sakny.user.entity.PreferredArea;
import com.sakny.user.entity.UserProfile;
import com.sakny.user.service.MatchingService.MatchScoreResult;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.*;

@Service
@Slf4j
public class LandlordMatchingService {

    private static final double WEIGHT_GENDER = 0.20;
    private static final double WEIGHT_BUDGET = 0.20;
    private static final double WEIGHT_SMOKING = 0.15;
    private static final double WEIGHT_LOCATION = 0.15;
    private static final double WEIGHT_SLEEP = 0.10;
    private static final double WEIGHT_CLEANLINESS = 0.10;
    private static final double WEIGHT_PETS = 0.05;
    private static final double WEIGHT_TENANT_TYPE = 0.05;

    public MatchScoreResult computePropertyMatchScore(Property property, UserProfile candidate) {
        Map<String, Double> breakdown = new LinkedHashMap<>();
        List<String> strengths = new ArrayList<>();
        List<String> conflicts = new ArrayList<>();

        double genderScore = computeGenderScore(property, candidate);
        breakdown.put("gender", genderScore);
        if (genderScore == 0.0) {
            conflicts.add("Gender preference mismatch");
            return new MatchScoreResult(0.0, breakdown, strengths, conflicts);
        } else {
            strengths.add("Gender preference compatible");
        }

        double budgetScore = computeBudgetScore(property, candidate);
        breakdown.put("budget", budgetScore);
        if (budgetScore >= 0.7) {
            strengths.add("Property fits tenant's budget");
        } else if (budgetScore < 0.3) {
            conflicts.add("Property outside tenant's budget range");
        }

        double smokingScore = computeSmokingScore(property, candidate);
        breakdown.put("smoking", smokingScore);
        if (smokingScore >= 0.8) {
            strengths.add("Smoking preferences align");
        } else if (smokingScore == 0.0) {
            conflicts.add("Smoking preference conflict");
        }

        double locationScore = computeLocationScore(property, candidate);
        breakdown.put("location", locationScore);
        if (locationScore >= 0.5) {
            strengths.add("Property is in tenant's preferred area");
        } else if (locationScore == 0.0) {
            conflicts.add("Property location not in tenant's preferred areas");
        }

        double sleepScore = computeSleepScore(property, candidate);
        breakdown.put("sleep", sleepScore);
        if (sleepScore >= 0.8) {
            strengths.add("Compatible sleep schedules");
        } else if (sleepScore <= 0.2) {
            conflicts.add("Different sleep schedules");
        }

        double cleanlinessScore = computeCleanlinessScore(property, candidate);
        breakdown.put("cleanliness", cleanlinessScore);
        if (cleanlinessScore >= 0.8) {
            strengths.add("Cleanliness standards match");
        } else if (cleanlinessScore < 0.5) {
            conflicts.add("Different cleanliness expectations");
        }

        double petScore = computePetScore(property, candidate);
        breakdown.put("pets", petScore);
        if (petScore >= 0.8) {
            strengths.add("Pet preferences compatible");
        } else if (petScore == 0.0) {
            conflicts.add("Pet preference conflict");
        }

        double tenantTypeScore = computeTenantTypeScore(property, candidate);
        breakdown.put("tenantType", tenantTypeScore);
        if (tenantTypeScore >= 0.8) {
            strengths.add("Tenant type matches preference");
        } else if (tenantTypeScore == 0.0) {
            conflicts.add("Tenant type doesn't match preference");
        }

        double totalScore = (genderScore * WEIGHT_GENDER)
                + (budgetScore * WEIGHT_BUDGET)
                + (smokingScore * WEIGHT_SMOKING)
                + (locationScore * WEIGHT_LOCATION)
                + (sleepScore * WEIGHT_SLEEP)
                + (cleanlinessScore * WEIGHT_CLEANLINESS)
                + (petScore * WEIGHT_PETS)
                + (tenantTypeScore * WEIGHT_TENANT_TYPE);

        double finalScore = Math.round(totalScore * 1000.0) / 10.0;

        return new MatchScoreResult(finalScore, breakdown, strengths, conflicts);
    }

    private double computeGenderScore(Property property, UserProfile candidate) {
        Gender pref = property.getPrefTenantGender();
        if (pref == null) return 1.0;
        Gender candidateGender = candidate.getGender();
        if (candidateGender == null) return 0.5;
        return pref.equals(candidateGender) ? 1.0 : 0.0;
    }

    private double computeBudgetScore(Property property, UserProfile candidate) {
        BigDecimal price = property.getPrice();
        Integer budgetMin = candidate.getBudgetMin();
        Integer budgetMax = candidate.getBudgetMax();

        if (price == null || budgetMin == null || budgetMax == null) return 0.5;

        double priceVal = price.doubleValue();
        double min = budgetMin;
        double max = budgetMax;

        if (priceVal >= min && priceVal <= max) return 1.0;

        double range = max - min;
        if (range <= 0) range = 1;

        if (priceVal < min) {
            return Math.max(0, 1.0 - (min - priceVal) / range);
        } else {
            return Math.max(0, 1.0 - (priceVal - max) / range);
        }
    }

    private double computeSmokingScore(Property property, UserProfile candidate) {
        SmokingPreference pref = property.getPrefSmoking();
        SmokingStatus candidateSmoking = candidate.getSmoking();

        if (pref == null || candidateSmoking == null) return 1.0;

        return switch (pref) {
            case DONT_MIND -> 1.0;
            case NON_SMOKER_ONLY -> switch (candidateSmoking) {
                case NON_SMOKER -> 1.0;
                case SOMETIMES -> 0.3;
                case SMOKE_OFTEN -> 0.0;
            };
        };
    }

    private double computeLocationScore(Property property, UserProfile candidate) {
        List<PreferredArea> candidateAreas = candidate.getPreferredAreas();
        if (candidateAreas == null || candidateAreas.isEmpty()) return 0.5;

        Integer propertyGovId = property.getGovernorate() != null ? property.getGovernorate().getId() : null;
        Integer propertyCityId = property.getCity() != null ? property.getCity().getId() : null;

        if (propertyGovId == null) return 0.5;

        for (PreferredArea area : candidateAreas) {
            Integer areaGovId = area.getGovernorate() != null ? area.getGovernorate().getId() : null;
            Integer areaCityId = area.getCity() != null ? area.getCity().getId() : null;

            if (propertyCityId != null && propertyCityId.equals(areaCityId)) return 1.0;
            if (propertyGovId.equals(areaGovId)) return 0.7;
        }

        return 0.0;
    }

    private double computeSleepScore(Property property, UserProfile candidate) {
        SleepSchedulePreference pref = property.getPrefSleepSchedule();
        SleepSchedule candidateSleep = candidate.getSleepSchedule();

        if (pref == null || candidateSleep == null) return 0.8;

        return switch (pref) {
            case DONT_MIND -> 1.0;
            case EARLY_BIRD -> switch (candidateSleep) {
                case EARLY_BIRD -> 1.0;
                case FLEXIBLE -> 0.8;
                case NIGHT_OWL -> 0.2;
            };
            case NIGHT_OWL -> switch (candidateSleep) {
                case NIGHT_OWL -> 1.0;
                case FLEXIBLE -> 0.8;
                case EARLY_BIRD -> 0.2;
            };
        };
    }

    private double computeCleanlinessScore(Property property, UserProfile candidate) {
        CleanlinessPreference pref = property.getPrefCleanliness();
        Integer candidateCleanliness = candidate.getCleanliness();

        if (pref == null || candidateCleanliness == null) return 0.8;

        return switch (pref) {
            case DONT_MIND -> 1.0;
            case VERY_CLEAN -> candidateCleanliness >= 4 ? 1.0 : candidateCleanliness == 3 ? 0.5 : 0.2;
            case AVERAGE_OR_ABOVE -> candidateCleanliness >= 3 ? 1.0 : 0.4;
        };
    }

    private double computePetScore(Property property, UserProfile candidate) {
        PetPreference pref = property.getPrefPets();
        PetStatus candidatePets = candidate.getPets();

        if (pref == null || candidatePets == null) return 1.0;

        return switch (pref) {
            case OKAY_WITH_PETS -> 1.0;
            case NO_PETS_PREFERRED -> candidatePets == PetStatus.NO_PETS ? 1.0 : 0.0;
        };
    }

    private double computeTenantTypeScore(Property property, UserProfile candidate) {
        RoommateType pref = property.getPrefTenantType();
        if (pref == null || pref == RoommateType.DONT_MIND) return 1.0;

        String occupation = candidate.getOccupation();
        String university = candidate.getUniversityOrSchool();
        String company = candidate.getCompanyName();

        boolean isStudent = (university != null && !university.isBlank())
                || "student".equalsIgnoreCase(occupation);
        boolean isProfessional = (company != null && !company.isBlank())
                || "working_professional".equalsIgnoreCase(occupation)
                || "professional".equalsIgnoreCase(occupation);

        return switch (pref) {
            case STUDENT -> isStudent ? 1.0 : (isProfessional ? 0.2 : 0.5);
            case WORKING_PROFESSIONAL -> isProfessional ? 1.0 : (isStudent ? 0.2 : 0.5);
            case DONT_MIND -> 1.0;
        };
    }
}
