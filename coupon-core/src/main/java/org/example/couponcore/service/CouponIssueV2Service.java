package org.example.couponcore.service;

import lombok.RequiredArgsConstructor;
import org.example.couponcore.component.LockExecutor;
import org.example.couponcore.model.redis.CouponRedisEntity;
import org.example.couponcore.service.cache.CouponCacheService;
import org.example.couponcore.service.cache.CouponIssueRedisService;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service
public class CouponIssueV2Service {

    private final CouponCacheService couponCacheService;
    private final CouponIssueRedisService couponIssueRedisService;
    private final LockExecutor lockExecutor;


    public void issue(long couponId, long userId) {

        CouponRedisEntity coupon = couponCacheService.getCouponCache(couponId); // redis에서 발급 대상 쿠폰 조회
        String lockName = "lock_%s".formatted(couponId);

        lockExecutor.execute(lockName, 3000,3000, ()-> {         // 락을 걸어서 동시성 문제 해결
            couponIssueRedisService.checkValidateToIssueCoupon(couponId,userId,coupon); // 발급 가능한 쿠폰인지 검증
            couponIssueRedisService.issueRequest(couponId,userId);                 // 발급을 위한 대기큐 적재 요청
        });
    }

}
