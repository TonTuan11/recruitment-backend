package com.tihuz.common.redis;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
public class BlacklistTokenService
{

    private final RedisTemplate<String, String> redisTemplate;

    private static final String PREFIX = "blacklist:";

    public void save(String token, long ttl)
    {
        redisTemplate.opsForValue()
                .set(PREFIX + token, "logout", ttl, TimeUnit.SECONDS);
    }

    public boolean exists(String token)
    {
        return Boolean.TRUE.equals(redisTemplate.hasKey(PREFIX + token));
    }

    public void delete(String token)
    {
        redisTemplate.delete(PREFIX + token);
    }
}