package com.sakny.user.client;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.Map;

@Component
@Slf4j
public class MlServiceClient {

    private final ObjectMapper objectMapper;
    private final HttpClient httpClient;

    @Value("${sakny.ml.base-url:http://localhost:8000}")
    private String mlBaseUrl;

    public MlServiceClient(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(5))
                .build();
    }

    public Map<String, Object> estimatePrice(Map<String, Object> profileData) {
        try {
            String body = objectMapper.writeValueAsString(profileData);
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(mlBaseUrl + "/api/v1/price/estimate"))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(body))
                    .timeout(Duration.ofSeconds(10))
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() == 200) {
                return objectMapper.readValue(response.body(), Map.class);
            } else {
                log.warn("ML price estimation failed with status {}: {}", response.statusCode(), response.body());
                return null;
            }
        } catch (Exception e) {
            log.warn("ML service unavailable for price estimation: {}", e.getMessage());
            return null;
        }
    }

    public Map<String, Object> getMatchScore(Map<String, Object> seeker, Map<String, Object> candidate) {
        try {
            Map<String, Object> payload = Map.of("seeker", seeker, "candidate", candidate);
            String body = objectMapper.writeValueAsString(payload);
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(mlBaseUrl + "/api/v1/match/score"))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(body))
                    .timeout(Duration.ofSeconds(10))
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() == 200) {
                return objectMapper.readValue(response.body(), Map.class);
            } else {
                log.warn("ML match scoring failed with status {}: {}", response.statusCode(), response.body());
                return null;
            }
        } catch (Exception e) {
            log.warn("ML service unavailable for match scoring: {}", e.getMessage());
            return null;
        }
    }

    public boolean isHealthy() {
        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(mlBaseUrl + "/health"))
                    .GET()
                    .timeout(Duration.ofSeconds(3))
                    .build();
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            return response.statusCode() == 200;
        } catch (Exception e) {
            return false;
        }
    }
}
