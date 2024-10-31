package org.example.couponconsumer.repository;


import static org.example.couponcore.utils.CouponRedisUtils.getCouponIssueQueue;

import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Repository
@RequiredArgsConstructor
public class CouponRedisRepository {

    private final ReactiveRedisTemplate<String, String> reactiveRedisTemplate;

    public Mono<String> lIndex(String key, long index) {
        return reactiveRedisTemplate.opsForList().index(key, index);
    }

    public Mono<String> lPop(String key) {
        return reactiveRedisTemplate.opsForList().leftPop(key);
    }

    public Mono<Long> lSize(String key) {
        return reactiveRedisTemplate.opsForList().size(key);
    }

    public Mono<Void> setIndexToDeleted(String key, int index) {
        return reactiveRedisTemplate.opsForList().set(key, index, "__deleted__").then();
    }

    public Mono<List<String>> getAllItems(String key) {
        return reactiveRedisTemplate.opsForList().range(key, 0, -1).collectList();
    }

    public Mono<Void> replaceList(String key, List<String> items) {
        return reactiveRedisTemplate.delete(key) // 기존 리스트 삭제
            .then(reactiveRedisTemplate.opsForList().rightPushAll(key, items).then()); // 새로운 리스트로 저장
    }
}
