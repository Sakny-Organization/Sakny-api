package com.sakny.user.client;

import com.sakny.common.config.ShuftiProProperties;
import com.sakny.common.exception.BusinessException;
import com.sakny.common.exception.VerificationErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.Base64;
import java.util.List;
import java.util.Map;

@Component
@RequiredArgsConstructor
@Slf4j
public class ShuftiProClient {

    private final ShuftiProProperties properties;
    private final RestTemplate restTemplate;

    /**
     * Submits the three images to Shufti Pro for Egyptian National ID verification.
     *
     * @param frontIdBase64 base64-encoded front of ID card
     * @param backIdBase64  base64-encoded back of ID card
     * @param selfieBase64  base64-encoded selfie
     * @param reference     unique reference string for this submission
     * @return Shufti Pro event string: "verification.accepted", "verification.declined", or "request.pending"
     */
    public String submitVerification(
            String frontIdBase64,
            String backIdBase64,
            String selfieBase64,
            String reference) {

        Map<String, Object> payload = buildPayload(frontIdBase64, backIdBase64, selfieBase64, reference);

        String credentials = properties.getClientId() + ":" + properties.getSecretKey();
        String basicAuth = Base64.getEncoder().encodeToString(credentials.getBytes());

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("Authorization", "Basic " + basicAuth);

        try {
            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(payload, headers);
            ResponseEntity<Map> response = restTemplate.exchange(
                    properties.getApiUrl(),
                    HttpMethod.POST,
                    entity,
                    Map.class);

            if (response.getBody() == null) {
                throw new BusinessException(VerificationErrorCode.VERIFICATION_EXTERNAL_API_ERROR,
                        "Shufti Pro returned empty response");
            }

            Object eventObj = response.getBody().get("event");
            String event = eventObj != null ? eventObj.toString() : "request.pending";
            log.info("Shufti Pro response for reference {}: event={}", reference, event);
            return event;

        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.error("Shufti Pro API call failed for reference {}: {}", reference, e.getMessage(), e);
            throw new BusinessException(VerificationErrorCode.VERIFICATION_EXTERNAL_API_ERROR,
                    "Verification provider error: " + e.getMessage());
        }
    }

    private Map<String, Object> buildPayload(
            String frontIdBase64, String backIdBase64,
            String selfieBase64, String reference) {

        return Map.of(
                "reference", reference,
                "callback_url", properties.getCallbackUrl() != null ? properties.getCallbackUrl() : "",
                "country", "EG",
                "language", "en",
                "document", Map.of(
                        "supported_types", List.of("id_card"),
                        "country", "EG",
                        "proof", frontIdBase64,
                        "back_proof", backIdBase64
                ),
                "face", Map.of(
                        "proof", selfieBase64
                )
        );
    }
}
