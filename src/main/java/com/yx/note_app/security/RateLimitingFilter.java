package com.yx.note_app.security;

import com.yx.note_app.enums.ResponseOutcome;
import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import io.github.bucket4j.Refill;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class RateLimitingFilter extends OncePerRequestFilter {

    private static final Logger logger = LoggerFactory.getLogger(RateLimitingFilter.class);

    // Store buckets per IP address
    private final Map<String, Bucket> buckets = new ConcurrentHashMap<>();

    // Rate limit: 10 requests per minute for auth endpoints
    private static final int AUTH_REQUESTS_PER_MINUTE = 10;

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getServletPath();
        // Only apply rate limiting to auth endpoints
        return !path.equals("/api/users/signup") && !path.equals("/api/users/login");
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain)
            throws ServletException, IOException {

        String clientIP = getClientIP(request);
        Bucket bucket = buckets.computeIfAbsent(clientIP, this::createBucket);

        if (bucket.tryConsume(1)) {
            filterChain.doFilter(request, response);
        } else {
            logger.warn("Rate limit exceeded for IP: {}", clientIP);
            response.setStatus(429); // HTTP 429 Too Many Requests
            response.setContentType("application/json");
            response.getWriter().write(
                    "{\"responseOutcome\":\"" + ResponseOutcome.RATE_LIMIT_EXCEEDED.getCode() +
                    "\",\"message\":\"" + ResponseOutcome.RATE_LIMIT_EXCEEDED.getDesc() + "\"}"
            );
        }
    }

    private Bucket createBucket(String key) {
        Bandwidth limit = Bandwidth.classic(
                AUTH_REQUESTS_PER_MINUTE,
                Refill.greedy(AUTH_REQUESTS_PER_MINUTE, Duration.ofMinutes(1))
        );
        return Bucket.builder().addLimit(limit).build();
    }

    private String getClientIP(HttpServletRequest request) {
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
            return xForwardedFor.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }
}
