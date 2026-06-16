package com.sakny.user.service;

import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class MatchExplanationService {

    public String generateExplanation(double score, Map<String, Double> breakdown, String candidateName) {
        List<String> positives = new ArrayList<>();
        List<String> negatives = new ArrayList<>();

        for (Map.Entry<String, Double> entry : breakdown.entrySet()) {
            String factor = entry.getKey();
            double value = entry.getValue();
            categorize(factor, value, candidateName, positives, negatives);
        }

        StringBuilder sb = new StringBuilder();

        if (score >= 80) {
            sb.append("Excellent match! ");
        } else if (score >= 60) {
            sb.append("Good compatibility. ");
        } else {
            sb.append("Some compatibility found. ");
        }

        if (!positives.isEmpty()) {
            sb.append(String.join(". ", positives.subList(0, Math.min(3, positives.size()))));
            sb.append(". ");
        }

        if (!negatives.isEmpty() && score < 85) {
            sb.append("However, ");
            sb.append(String.join(", and ", negatives.subList(0, Math.min(2, negatives.size()))));
            sb.append(".");
        }

        return sb.toString().trim();
    }

    public List<String> generateDiscussionTopics(Map<String, Double> breakdown) {
        List<String> topics = new ArrayList<>();

        Double budget = breakdown.get("budget");
        if (budget != null && budget < 70) {
            topics.add("Discuss exact budget split and what's included (utilities, internet, cleaning)");
        }

        Double smoking = breakdown.get("smoking");
        if (smoking != null && smoking < 80) {
            topics.add("Clarify smoking rules: indoor/outdoor, balcony, frequency");
        }

        Double sleep = breakdown.get("sleep");
        if (sleep != null && sleep < 80) {
            topics.add("Talk about quiet hours, noise tolerance, and weekend routines");
        }

        Double cleanliness = breakdown.get("cleanliness");
        if (cleanliness != null && cleanliness < 70) {
            topics.add("Agree on cleaning responsibilities and standards for shared spaces");
        }

        Double pets = breakdown.get("pets");
        if (pets != null && pets < 80) {
            topics.add("Discuss pet rules: feeding, noise, allergies, shared space access");
        }

        Double location = breakdown.get("location");
        if (location != null && location < 50) {
            topics.add("You prefer different areas — discuss which neighborhoods work for both");
        }

        // Always include these general ones
        topics.add("Set expectations for guests and overnight visitors");
        topics.add("Agree on shared expenses and how to split bills");

        return topics.subList(0, Math.min(5, topics.size()));
    }

    private void categorize(String factor, double value, String name, List<String> positives, List<String> negatives) {
        switch (factor) {
            case "budget" -> {
                if (value >= 80) positives.add("Your budgets align well");
                else if (value < 40) negatives.add("your budget ranges don't overlap much");
            }
            case "gender" -> {
                if (value >= 100) positives.add("Gender preference matches");
            }
            case "smoking" -> {
                if (value >= 80) positives.add("Compatible smoking preferences");
                else if (value < 40) negatives.add("smoking preferences may conflict");
            }
            case "location" -> {
                if (value >= 80) positives.add("You both prefer similar areas");
                else if (value < 30) negatives.add("you prefer different locations");
            }
            case "sleep" -> {
                if (value >= 80) positives.add("Similar sleep schedules");
                else if (value < 40) negatives.add("different sleep patterns");
            }
            case "cleanliness" -> {
                if (value >= 80) positives.add("Similar cleanliness standards");
                else if (value < 50) negatives.add("different cleanliness expectations");
            }
            case "pets" -> {
                if (value >= 80) positives.add("Pet compatibility is good");
                else if (value < 50) negatives.add("pet preferences differ");
            }
            case "personality" -> {
                if (value >= 70) positives.add("You share personality traits");
            }
        }
    }
}
