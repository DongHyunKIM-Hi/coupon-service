package org.example.couponcore.repository;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class CouponRedisRepository  {

    private final RedisTemplate<String, String> redisTemplate;

    // 값이 없으면 sorted_set 에 추가  있으면 스킵
    public Boolean zAdd(String key, String value, double score) {
        return redisTemplate.opsForZSet().addIfAbsent(key,value,score);
    }
}
