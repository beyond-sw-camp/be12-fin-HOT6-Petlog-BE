package com.hot6.backend.redis;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Repository;

import java.time.Duration;

@Repository
public class RefreshTokenRepository {
    private final RedisTemplate<String, String> redisTemplate;

    public RefreshTokenRepository(RedisTemplate<String, String> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    public void save(String userId, String refreshToken, long duration) {
        redisTemplate.opsForValue().set(userId, refreshToken, Duration.ofMillis(duration));
    }

    public String findById(String userId) {
        return redisTemplate.opsForValue().get(userId);
    }

    public void delete(String userId) {
        redisTemplate.delete(userId);
    }
}
