package com.agarg.securecollab.websocketservice;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.concurrent.TimeUnit;

/**
 * PresenceService — stores simple online presence in Redis with TTL
 */
@Service
public class PresenceService {
    private static final String PRESENCE_KEY_PREFIX = "presence:";
    private final RedisTemplate<String, String> redisTemplate;
    private final Duration ttl = Duration.ofMinutes(10);

    public PresenceService(RedisTemplate<String, String> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    public void markOnline(String userId, String sessionId) {
        String key = PRESENCE_KEY_PREFIX + userId;
        redisTemplate.opsForValue().set(key, sessionId, ttl.toMillis(), TimeUnit.MILLISECONDS);
    }

    public void markOffline(String userId) {
        String key = PRESENCE_KEY_PREFIX + userId;
        redisTemplate.delete(key);
    }

    public boolean isOnline(String userId) {
        String key = PRESENCE_KEY_PREFIX + userId;
        return redisTemplate.hasKey(key);
    }

    public String getSessionId(String userId) {
        String key = PRESENCE_KEY_PREFIX + userId;
        return redisTemplate.opsForValue().get(key);
    }
}
