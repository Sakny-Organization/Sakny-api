package com.sakny.common.filter;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sakny.common.dto.ApiResponse;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.lang.NonNull;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Map;

@Slf4j
public class RateLimitFilter extends OncePerRequestFilter {

    private final StringRedisTemplate redisTemplate;
    private final ObjectMapper objectMapper;

    private static final Map<String, RateLimit> ENDPOINT_LIMITS = Map.of(
            "/v1/auth/authenticate", new RateLimit(10, Duration.ofMinutes(15)),
            "/v1/auth/register", new RateLimit(5, Duration.ofMinutes(15)),
            "/v1/auth/send-otp", new RateLimit(3, Duration.ofHours(1)),
            "/v1/auth/verify-otp", new RateLimit(5, Duration.ofMinutes(10)),
            "/v1/auth/reset-password", new RateLimit(3, Duration.ofMinutes(15))
    );

    private static final RateLimit DEFAULT_AUTHENTICATED_LIMIT = new RateLimit(120, Duration.ofMinutes(1));

    public RateLimitFilter(StringRedisTemplate redisTemplate, ObjectMapper objectMapper) {
        this.redisTemplate = redisTemplate;
        this.objectMapper = objectMapper;
    }

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain) throws ServletException, IOException {

        String path = request.getServletPath();
        String clientIp = extractClientIp(request);

        RateLimit limit = ENDPOINT_LIMITS.get(path);
        String key;

        if (limit != null) {
            key = "rate:" + path + ":" + clientIp;
        } else if (path.startsWith("/v1/auth/")) {
            filterChain.doFilter(request, response);
            return;
        } else {
            limit = DEFAULT_AUTHENTICATED_LIMIT;
            key = "rate:global:" + clientIp;
        }

        if (isRateLimited(key, limit)) {
            log.warn("Rate limit exceeded for {} on path {}", clientIp, path);
            response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
            response.setContentType(MediaType.APPLICATION_JSON_VALUE);
            response.setHeader("Retry-After", String.valueOf(limit.window().getSeconds()));
            ApiResponse<Void> body = ApiResponse.error("Too many requests. Please try again later.");
            objectMapper.writeValue(response.getOutputStream(), body);
            return;
        }

        filterChain.doFilter(request, response);
    }

    private boolean isRateLimited(String key, RateLimit limit) {
        try {
            Long count = redisTemplate.opsForValue().increment(key);
            if (count == null) {
                return false;
            }
            if (count == 1L) {
                redisTemplate.expire(key, limit.window());
            }
            return count > limit.maxRequests();
        } catch (Exception e) {
            log.error("Redis rate limit check failed, allowing request: {}", e.getMessage());
            return false;
        }
    }

    private String extractClientIp(HttpServletRequest request) {
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
            return xForwardedFor.split(",")[0].trim();
        }
        String xRealIp = request.getHeader("X-Real-IP");
        if (xRealIp != null && !xRealIp.isEmpty()) {
            return xRealIp;
        }
        return request.getRemoteAddr();
    }

    private record RateLimit(int maxRequests, Duration window) {}
}
