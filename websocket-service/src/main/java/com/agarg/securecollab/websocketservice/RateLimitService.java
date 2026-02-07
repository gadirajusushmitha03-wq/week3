package com.agarg.securecollab.websocketservice;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import java.time.Duration;
import java.util.concurrent.TimeUnit;

/**
 * Simple Redis-backed rate limiter using increment+expire.
 * Counts events per user per window and enforces a max.
 */
@Service
public class RateLimitService {
    private static final String RATE_KEY_PREFIX = "rate:";
    private final RedisTemplate<String, String> redisTemplate;
    private final int defaultLimit = 20; // messages
    private final Duration window = Duration.ofSeconds(10);

    public RateLimitService(RedisTemplate<String, String> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    public boolean tryConsume(String userId) {
        return tryConsume(userId, defaultLimit, window);
    }

    public boolean tryConsume(String userId, int limit, Duration window) {
        String key = RATE_KEY_PREFIX + userId + ":" + (System.currentTimeMillis() / window.toMillis());
        Long count = redisTemplate.opsForValue().increment(key);
        if (count != null && count == 1L) {
            redisTemplate.expire(key, window.toMillis(), TimeUnit.MILLISECONDS);
        }
        return count != null && count <= limit;
    }
}
