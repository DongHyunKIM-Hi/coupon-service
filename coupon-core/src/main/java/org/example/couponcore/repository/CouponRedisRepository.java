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

    public Long sAdd(String key, String value) {
        return redisTemplate.opsForSet().add(key, value);
    }

    public Long sCard(String key) {
        return redisTemplate.opsForZSet().size(key);
    }

    public Boolean sIsMember(String key, String value) {
        return redisTemplate.opsForSet().isMember(key,value);
    }

    public Long rPush(String key, String value) {
        return redisTemplate.opsForList().rightPush(key,value);
    }

}
