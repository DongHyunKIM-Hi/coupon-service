package org.example.couponcore.service.cache;


import lombok.RequiredArgsConstructor;
import org.example.couponcore.model.entity.base.Coupon;
import org.example.couponcore.model.redis.CouponRedisEntity;
import org.example.couponcore.repository.CouponJpaRepository;
import org.springframework.aop.framework.AopContext;
import org.springframework.cache.annotation.CachePut;
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

    @CachePut(cacheNames = "coupon")
    public CouponRedisEntity putCouponCache(long couponId) {
        return getCouponCache(couponId);
    }

    @Cacheable(cacheNames = "coupon", cacheManager = "localCacheManager")
    public CouponRedisEntity getCouponLocalCache(long couponId) {
        return proxy().getCouponCache(couponId);
    }

    @CachePut(cacheNames = "coupon", cacheManager = "localCacheManager")
    public CouponRedisEntity putCouponLocalCache(long couponId) {
        return getCouponLocalCache(couponId);
    }

    private CouponCacheService proxy() {
        return (CouponCacheService) AopContext.currentProxy();
    }
}
