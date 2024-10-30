package org.example.couponcore.repository;

import static org.example.couponcore.utils.CouponRedisUtils.getCouponIssueQueue;
import static org.example.couponcore.utils.CouponRedisUtils.getCouponKey;

import java.util.List;
import lombok.RequiredArgsConstructor;
import org.example.couponcore.model.dto.request.CouponIssueRequestDto;
import org.example.couponcore.model.redis.CouponIssueRequestCode;
import org.example.couponcore.utils.JacksonUtils;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.script.RedisScript;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class CouponRedisRepository  {

    private final RedisTemplate<String, String> redisTemplate;

    private final RedisScript<String> issueScript = issueRequestScript();

    // 값이 없으면 sorted_set 에 추가  있으면 스킵
    public Boolean zAdd(String key, String value, double score) {
        return redisTemplate.opsForZSet().addIfAbsent(key,value,score);
    }

    public String lIndex(String key, long index) {
        return redisTemplate.opsForList().index(key, index);
    }

    public String lPop(String key) {
        return redisTemplate.opsForList().leftPop(key);
    }

    public Long sAdd(String key, String value) {
        return redisTemplate.opsForSet().add(key, value);
    }

    public Long sCard(String key) {
        return redisTemplate.opsForSet().size(key);
    }

    public Boolean sIsMember(String key, String value) {
        return redisTemplate.opsForSet().isMember(key, value);
    }

    public Long rPush(String key, String value) {
        return redisTemplate.opsForList().rightPush(key,value);
    }

    public Long lSize(String key) {
        return redisTemplate.opsForList().size(key);
    }

    private RedisScript<String> issueRequestScript() {
        String script = """
            if redis.call('SISMEMBER', KEYS[1], ARGV[1]) == 1 then
                return '2'
            end
                            
            if tonumber(ARGV[2]) > redis.call('SCARD', KEYS[1]) then
                redis.call('SADD', KEYS[1], ARGV[1])
                redis.call('RPUSH', KEYS[2], ARGV[3])
                return '1'
            end
                            
            return '3'
            """;
        return RedisScript.of(script, String.class);
    }

    public void issueRequest(long couponId, long userId, int totalIssueQuantity) {
        String issueRequestKey = getCouponKey(couponId);
        CouponIssueRequestDto requestDto = new CouponIssueRequestDto(couponId,userId);

        String result =  redisTemplate.execute(
            issueScript,
            List.of(issueRequestKey, getCouponIssueQueue()),
            String.valueOf(userId),
            String.valueOf(totalIssueQuantity),
            JacksonUtils.toString(requestDto)
            );
        CouponIssueRequestCode.checkRequestResult(CouponIssueRequestCode.find(result));
    }
}
