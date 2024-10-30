package org.example.couponcore.service.cache;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CacheUpdatePublisher {

    private final RedisTemplate<String, String> redisTemplate;

    public void publishCacheUpdate(String cacheKey) {
        redisTemplate.convertAndSend("cacheUpdateChannel", cacheKey);
    }
}
