package org.example.couponcore.service;


import lombok.RequiredArgsConstructor;
import org.example.couponcore.model.entity.base.Coupon;
import org.example.couponcore.model.redis.CouponRedisEntity;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service
public class CouponCacheService {


    private final CouponIssueV2Service couponIssueV2Service;

    @Cacheable(cacheNames = "coupon")
    public CouponRedisEntity getCouponCache(long couponId) {
        Coupon coupon = couponIssueV2Service.findCoupon(couponId);
        return new CouponRedisEntity(coupon);
    }
}
