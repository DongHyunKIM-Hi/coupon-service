package org.example.couponcore.service;

import lombok.RequiredArgsConstructor;
import org.example.couponcore.component.LockExecutor;
import org.example.couponcore.model.redis.CouponRedisEntity;
import org.example.couponcore.repository.CouponRedisRepository;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service
public class CouponIssueV3Service {

    private final CouponCacheService couponCacheService;
    private final CouponIssueRedisService couponIssueRedisService;
    private final LockExecutor lockExecutor;
    private final CouponRedisRepository couponRedisRepository;

    public void issue(long couponId, long userId) {
        CouponRedisEntity coupon = couponCacheService.getCouponCache(couponId);
        couponIssueRedisService.checkValidateToIssueCoupon(couponId,userId,coupon);
        issueRequest(couponId, userId, coupon.totalQuantity());
    }

    public void issueRequest(long couponId, long userId, Integer totalQuantity) {
        if(totalQuantity == null) {
            couponRedisRepository.issueRequest(couponId,userId,Integer.MAX_VALUE);
        }
        couponRedisRepository.issueRequest(couponId,userId,totalQuantity);
    }
}
