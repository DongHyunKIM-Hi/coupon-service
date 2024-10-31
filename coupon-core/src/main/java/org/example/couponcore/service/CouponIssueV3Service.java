package org.example.couponcore.service;

import lombok.RequiredArgsConstructor;
import org.example.couponcore.model.redis.CouponRedisEntity;
import org.example.couponcore.repository.CouponReactiveRedisRepository;
import org.example.couponcore.service.cache.CouponCacheService;
import org.example.couponcore.service.cache.CouponIssueRedisService;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service
public class CouponIssueV3Service {

    private final CouponCacheService couponCacheService;
    private final CouponIssueRedisService couponIssueRedisService;
    private final CouponReactiveRedisRepository couponReactiveRedisRepository;

    public void issue(long couponId, long userId) {             // 발급 요청 대기큐에 넣는 행위를 함
        // 쿠폰 정보를 가져오는데 1차로 로컬 캐시에서 가져오고
        // 값이 없다면 2차로 Redis에서 가져오고
        // 값이 없다면 DB를 통해서 값을 가져온다.
        CouponRedisEntity coupon = couponCacheService.getCouponLocalCache(couponId);
        couponIssueRedisService.checkValidateToIssueCoupon(couponId,userId,coupon);
        issueRequest(couponId, userId, coupon.totalQuantity());
    }

    public void issueRequest(long couponId, long userId, Integer totalQuantity) {
        if(totalQuantity == null) {
            couponReactiveRedisRepository.issueRequest(couponId,userId,Integer.MAX_VALUE);
        }
        couponReactiveRedisRepository.issueRequest(couponId,userId,totalQuantity);
    }
}
