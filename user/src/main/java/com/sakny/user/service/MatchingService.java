package com.sakny.user.service;

import com.sakny.common.model.*;
import com.sakny.user.entity.PreferredArea;
import com.sakny.user.entity.UserProfile;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
@Slf4j
public class MatchingService {

    private static final double WEIGHT_GENDER = 0.20;
    private static final double WEIGHT_BUDGET = 0.20;
    private static final double WEIGHT_SMOKING = 0.15;
    private static final double WEIGHT_LOCATION = 0.15;
    private static final double WEIGHT_SLEEP = 0.10;
    private static final double WEIGHT_CLEANLINESS = 0.10;
    private static final double WEIGHT_PETS = 0.05;
    private static final double WEIGHT_PERSONALITY = 0.05;

    private final ObjectMapper objectMapper = new ObjectMapper();

    public MatchScoreResult computeMatchScore(UserProfile seeker, UserProfile candidate) {
        Map<String, Double> breakdown = new LinkedHashMap<>();
        List<String> strengths = new ArrayList<>();
        List<String> conflicts = new ArrayList<>();

        // Gender compatibility (hard filter)
        double genderScore = computeGenderScore(seeker, candidate);
        breakdown.put("gender", genderScore);
        if (genderScore == 0.0) {
            conflicts.add("Gender preference mismatch");
            return new MatchScoreResult(0.0, breakdown, strengths, conflicts);
        } else {
            strengths.add("Gender preference compatible");
        }

        // Budget overlap
        double budgetScore = computeBudgetScore(seeker, candidate);
        breakdown.put("budget", budgetScore);
        if (budgetScore >= 0.7) {
            strengths.add("Budget ranges overlap well");
        } else if (budgetScore < 0.3) {
            conflicts.add("Budget ranges have little overlap");
        }

        // Smoking compatibility
        double smokingScore = computeSmokingScore(seeker, candidate);
        breakdown.put("smoking", smokingScore);
        if (smokingScore >= 0.8) {
            strengths.add("Smoking preferences align");
        } else if (smokingScore == 0.0) {
            conflicts.add("Smoking preference conflict");
        }

        // Location overlap
        double locationScore = computeLocationScore(seeker, candidate);
        breakdown.put("location", locationScore);
        if (locationScore >= 0.5) {
            strengths.add("Preferred locations overlap");
        } else if (locationScore == 0.0) {
            conflicts.add("No common preferred areas");
        }

        // Sleep schedule
        double sleepScore = computeSleepScore(seeker, candidate);
        breakdown.put("sleep", sleepScore);
        if (sleepScore >= 0.8) {
            strengths.add("Compatible sleep schedules");
        } else if (sleepScore <= 0.2) {
            conflicts.add("Different sleep schedules");
        }

        // Cleanliness
        double cleanlinessScore = computeCleanlinessScore(seeker, candidate);
        breakdown.put("cleanliness", cleanlinessScore);
        if (cleanlinessScore >= 0.8) {
            strengths.add("Similar cleanliness standards");
        } else if (cleanlinessScore < 0.5) {
            conflicts.add("Different cleanliness levels");
        }

        // Pets
        double petScore = computePetScore(seeker, candidate);
        breakdown.put("pets", petScore);
        if (petScore >= 0.8) {
            strengths.add("Pet preferences compatible");
        } else if (petScore == 0.0) {
            conflicts.add("Pet preference conflict");
        }

        // Personality
        double personalityScore = computePersonalityScore(seeker, candidate);
        breakdown.put("personality", personalityScore);
        if (personalityScore >= 0.5) {
            strengths.add("Shared personality traits");
        }

        // Weighted total
        double totalScore = (genderScore * WEIGHT_GENDER)
                + (budgetScore * WEIGHT_BUDGET)
                + (smokingScore * WEIGHT_SMOKING)
                + (locationScore * WEIGHT_LOCATION)
                + (sleepScore * WEIGHT_SLEEP)
                + (cleanlinessScore * WEIGHT_CLEANLINESS)
                + (petScore * WEIGHT_PETS)
                + (personalityScore * WEIGHT_PERSONALITY);

        double finalScore = Math.round(totalScore * 1000.0) / 10.0; // scale to 0-100 with 1 decimal

        return new MatchScoreResult(finalScore, breakdown, strengths, conflicts);
    }

    private double computeGenderScore(UserProfile seeker, UserProfile candidate) {
        Gender seekerPref = seeker.getRoommateGender();
        Gender candidateGender = candidate.getGender();

        // If seeker has no preference on roommate gender (null means any)
        if (seekerPref == null) {
            return 1.0;
        }

        // Check if candidate gender matches seeker's roommate gender preference
        return seekerPref.equals(candidateGender) ? 1.0 : 0.0;
    }

    private double computeBudgetScore(UserProfile seeker, UserProfile candidate) {
        if (seeker.getBudgetMin() == null || seeker.getBudgetMax() == null
                || candidate.getBudgetMin() == null || candidate.getBudgetMax() == null) {
            return 0.5; // unknown, neutral
        }

        double sMin = seeker.getBudgetMin();
        double sMax = seeker.getBudgetMax();
        double cMin = candidate.getBudgetMin();
        double cMax = candidate.getBudgetMax();

        // IoU: intersection over union
        double intersectionMin = Math.max(sMin, cMin);
        double intersectionMax = Math.min(sMax, cMax);
        double intersection = Math.max(0, intersectionMax - intersectionMin);

        double unionMin = Math.min(sMin, cMin);
        double unionMax = Math.max(sMax, cMax);
        double union = unionMax - unionMin;

        if (union == 0) {
            return 1.0; // both have same single-point budget
        }

        return intersection / union;
    }

    private double computeSmokingScore(UserProfile seeker, UserProfile candidate) {
        SmokingPreference seekerPref = seeker.getPrefSmoking();
        SmokingStatus candidateSmoking = candidate.getSmoking();

        if (seekerPref == null || candidateSmoking == null) {
            return 1.0; // no preference = compatible
        }

        switch (seekerPref) {
            case DONT_MIND:
                return 1.0;
            case NON_SMOKER_ONLY:
                if (candidateSmoking == SmokingStatus.NON_SMOKER) {
                    return 1.0;
                } else if (candidateSmoking == SmokingStatus.SOMETIMES) {
                    return 0.3;
                } else {
                    return 0.0;
                }
            default:
                return 1.0;
        }
    }

    private double computeLocationScore(UserProfile seeker, UserProfile candidate) {
        List<PreferredArea> seekerAreas = seeker.getPreferredAreas();
        List<PreferredArea> candidateAreas = candidate.getPreferredAreas();

        if (seekerAreas == null || seekerAreas.isEmpty()
                || candidateAreas == null || candidateAreas.isEmpty()) {
            return 0.5; // no data, neutral
        }

        // Jaccard similarity using (governorate_id, city_id) pairs
        Set<String> seekerSet = seekerAreas.stream()
                .map(a -> a.getGovernorate().getId() + ":" + a.getCity().getId())
                .collect(Collectors.toSet());

        Set<String> candidateSet = candidateAreas.stream()
                .map(a -> a.getGovernorate().getId() + ":" + a.getCity().getId())
                .collect(Collectors.toSet());

        Set<String> intersection = new HashSet<>(seekerSet);
        intersection.retainAll(candidateSet);

        Set<String> union = new HashSet<>(seekerSet);
        union.addAll(candidateSet);

        if (union.isEmpty()) {
            return 0.0;
        }

        return (double) intersection.size() / union.size();
    }

    private double computeSleepScore(UserProfile seeker, UserProfile candidate) {
        SleepSchedulePreference seekerPref = seeker.getPrefSleepSchedule();
        SleepSchedule candidateSleep = candidate.getSleepSchedule();

        if (seekerPref == null || candidateSleep == null) {
            return 0.8; // no preference
        }

        switch (seekerPref) {
            case DONT_MIND:
                return 1.0;
            case EARLY_BIRD:
                if (candidateSleep == SleepSchedule.EARLY_BIRD) return 1.0;
                if (candidateSleep == SleepSchedule.FLEXIBLE) return 0.8;
                return 0.2;
            case NIGHT_OWL:
                if (candidateSleep == SleepSchedule.NIGHT_OWL) return 1.0;
                if (candidateSleep == SleepSchedule.FLEXIBLE) return 0.8;
                return 0.2;
            default:
                return 0.8;
        }
    }

    private double computeCleanlinessScore(UserProfile seeker, UserProfile candidate) {
        Integer seekerCleanliness = seeker.getCleanliness();
        Integer candidateCleanliness = candidate.getCleanliness();

        if (seekerCleanliness == null || candidateCleanliness == null) {
            return 0.5;
        }

        // Scale 1-5: score = 1.0 - (abs(diff) / 4.0)
        return 1.0 - (Math.abs(seekerCleanliness - candidateCleanliness) / 4.0);
    }

    private double computePetScore(UserProfile seeker, UserProfile candidate) {
        PetPreference seekerPref = seeker.getPrefPets();
        PetStatus candidatePets = candidate.getPets();

        if (seekerPref == null || candidatePets == null) {
            return 1.0; // no preference
        }

        switch (seekerPref) {
            case OKAY_WITH_PETS:
                return 1.0;
            case NO_PETS_PREFERRED:
                return candidatePets == PetStatus.NO_PETS ? 1.0 : 0.0;
            default:
                return 1.0;
        }
    }

    private double computePersonalityScore(UserProfile seeker, UserProfile candidate) {
        List<String> seekerTraits = parsePersonalityTraits(seeker.getPersonalityTraits());
        List<String> candidateTraits = parsePersonalityTraits(candidate.getPersonalityTraits());

        if (seekerTraits.isEmpty() || candidateTraits.isEmpty()) {
            return 0.5; // neutral
        }

        // Jaccard similarity
        Set<String> seekerSet = new HashSet<>(seekerTraits.stream()
                .map(String::toLowerCase)
                .collect(Collectors.toSet()));
        Set<String> candidateSet = new HashSet<>(candidateTraits.stream()
                .map(String::toLowerCase)
                .collect(Collectors.toSet()));

        Set<String> intersection = new HashSet<>(seekerSet);
        intersection.retainAll(candidateSet);

        Set<String> union = new HashSet<>(seekerSet);
        union.addAll(candidateSet);

        if (union.isEmpty()) {
            return 0.0;
        }

        return (double) intersection.size() / union.size();
    }

    private List<String> parsePersonalityTraits(String json) {
        if (json == null || json.isBlank() || json.equals("[]")) {
            return Collections.emptyList();
        }
        try {
            return objectMapper.readValue(json, new TypeReference<List<String>>() {});
        } catch (Exception e) {
            log.warn("Failed to parse personality traits: {}", json);
            return Collections.emptyList();
        }
    }

    public record MatchScoreResult(
            double score,
            Map<String, Double> breakdown,
            List<String> strengths,
            List<String> conflicts
    ) {}
}
