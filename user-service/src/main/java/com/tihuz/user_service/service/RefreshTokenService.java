package com.tihuz.user_service.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
public class RefreshTokenService {

    private final RedisTemplate<String,String> redisTemplate;

    private static final String PREFIX = "refresh:";

    public void save(Long userId, String token, long ttl)
    {
        redisTemplate.opsForValue().set(PREFIX + userId, token, ttl, TimeUnit.SECONDS);
    }

    public String get(Long userId)
    {
        return redisTemplate.opsForValue()
                .get(PREFIX + userId);
    }

    public void delete(Long userId)
    {
        redisTemplate.delete(PREFIX + userId);
    }
}