package com.example.spring_redis_implementation.service;

import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import lombok.AllArgsConstructor;
import java.time.Duration;
import java.util.UUID;

@Service
@AllArgsConstructor
public class RateLimiterService {

    private final StringRedisTemplate redisTemplate;
    public boolean isAllowed(String clientKey, int maxRequests, int windowSeconds){
        String keys = "rate_limit:" + clientKey;
        Long request = redisTemplate.opsForValue().increment(keys);
        
        if(request != null && request == 1){
            redisTemplate.expire(keys, Duration.ofSeconds(windowSeconds));
        }
        return request != null && request <= maxRequests;
    }

    public boolean isAllowedSlidingWindow(String clientKey, int maxRequests, int windowSeconds){
        String key = "rate_limit_sliding:" + clientKey;
        long currentTimeMs = System.currentTimeMillis();
        long windowStartMs = currentTimeMs - (windowSeconds * 1000L);
        
        /* 
        // 1. Remove all requests older than the sliding window start time
        redisTemplate.opsForZSet().removeRangeByScore(key, 0, windowStartMs);
        // 2. Add current request timestamp (using a unique value to avoid ZSET member collision)
        String requestId = currentTimeMs + "-" + UUID.randomUUID().toString().substring(0, 5);
        redisTemplate.opsForZSet().add(key, requestId, currentTimeMs);
        // 3. Count remaining requests in the sliding window
        Long currentRequests = redisTemplate.opsForZSet().zCard(key);
        // 4. Set TTL on the ZSET key so inactive users get cleaned up automatically
        redisTemplate.expire(key, Duration.ofSeconds(windowSeconds));
        return currentRequests != null && currentRequests <= maxRequests; */

        // 1. Remove stale requests older than the sliding window
        redisTemplate.opsForZSet().removeRangeByScore(key, 0, windowStartMs);
        // 2. Count existing valid requests BEFORE adding the new one
        Long currentRequests = redisTemplate.opsForZSet().zCard(key);
        // 3. Early Exit: If already at or over limit, reject without polluting Redis memory!
        if (currentRequests != null && currentRequests >= maxRequests) {
            redisTemplate.expire(key, Duration.ofSeconds(windowSeconds)); // Keep key alive for window
            return false;
        }
        // 4. Only add to ZSET if under the limit
        String requestId = currentTimeMs + "-" + UUID.randomUUID().toString().substring(0, 5);
        redisTemplate.opsForZSet().add(key, requestId, currentTimeMs);
        // 5. Refresh key expiration TTL
        redisTemplate.expire(key, Duration.ofSeconds(windowSeconds));

        return true;
    }
        
        
    
    
}
