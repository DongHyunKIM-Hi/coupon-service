package org.example.couponcore.service;


import lombok.RequiredArgsConstructor;
import org.example.couponcore.model.entity.base.Coupon;
import org.example.couponcore.model.redis.CouponRedisEntity;
import org.example.couponcore.repository.CouponJpaRepository;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service
public class CouponCacheService {

    private final CouponJpaRepository couponJpaRepository;


    @Cacheable(cacheNames = "coupon")
    public CouponRedisEntity getCouponCache(long couponId) {
        Coupon coupon = couponJpaRepository.findCouponById(couponId);
        return new CouponRedisEntity(coupon);
    }
}
