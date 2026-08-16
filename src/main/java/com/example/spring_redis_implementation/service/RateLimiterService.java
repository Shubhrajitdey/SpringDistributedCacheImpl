package com.example.spring_redis_implementation.service;

import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import lombok.AllArgsConstructor;
import java.time.Duration;

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
    
}
