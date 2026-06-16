package com.sakny.common.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MatchScoreResponse {
    private Long userId;
    private double score;
    private Map<String, Double> breakdown;
    private List<String> strengths;
    private List<String> conflicts;
    private String explanation;
    private List<String> discussionTopics;
    private ProfileResponse profile;
}
