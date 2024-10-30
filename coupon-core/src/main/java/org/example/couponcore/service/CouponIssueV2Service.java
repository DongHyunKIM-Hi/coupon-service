package org.example.couponcore.service;

import static org.example.couponcore.utils.CouponRedisUtils.getCouponIssueQueue;
import static org.example.couponcore.utils.CouponRedisUtils.getCouponKey;

import lombok.RequiredArgsConstructor;
import org.example.couponcore.component.LockExecutor;
import org.example.couponcore.exception.CouponIssueException;
import org.example.couponcore.exception.ErrorCode;
import org.example.couponcore.model.dto.request.CouponIssueRequestDto;
import org.example.couponcore.model.entity.base.Coupon;
import org.example.couponcore.model.entity.base.CouponIssue;
import org.example.couponcore.model.redis.CouponRedisEntity;
import org.example.couponcore.repository.CouponIssueJpaRepository;
import org.example.couponcore.repository.CouponJpaRepository;
import org.example.couponcore.repository.CouponRepository;
import org.example.couponcore.utils.JacksonUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
