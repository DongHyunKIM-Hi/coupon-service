package org.example.couponcore.service;


import static org.example.couponcore.exception.ErrorCode.ALREADY_ISSUED;
import static org.example.couponcore.exception.ErrorCode.COUPON_NOT_EXIST;
import static org.example.couponcore.exception.ErrorCode.INVALID_COUPON_ISSUE_QUANTITY;
import static org.example.couponcore.utils.CouponRedisUtils.getCouponIssueQueue;
import static org.example.couponcore.utils.CouponRedisUtils.getCouponKey;

import java.util.Collection;
import java.util.stream.IntStream;
import org.example.couponcore.config.TestConfig;
import org.example.couponcore.data.MockData;
import org.example.couponcore.exception.CouponIssueException;
import org.example.couponcore.model.dto.request.CouponIssueRequestDto;
import org.example.couponcore.model.entity.base.Coupon;
import org.example.couponcore.repository.CouponJpaRepository;
import org.example.couponcore.service.cache.CouponCacheService;
import org.example.couponcore.utils.JacksonUtils;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;

class CouponIssueV2ServiceTest extends TestConfig {

    @Autowired
    CouponIssueV2Service couponIssueV2Service;

    @Autowired
    CouponCacheService couponCacheService;

    @Autowired
    RedisTemplate<String, String> redisTemplate;

    @Autowired
    CouponJpaRepository couponJpaRepository;

    @BeforeEach
    void setup() {
        Collection<String> redisKeys = redisTemplate.keys("*");
        redisTemplate.delete(redisKeys);
    }

    @Test
    @DisplayName("쿠폰 발급 - 성공")
    void issue_success() {
        long userId = 1;

        Coupon coupon = MockData.getCoupon();
        couponJpaRepository.save(coupon);

        CouponIssueRequestDto requestDto = new CouponIssueRequestDto(coupon.getId(),userId);

        couponIssueV2Service.issue(coupon.getId(),userId);

        String requestResult =  redisTemplate.opsForList().leftPop(getCouponIssueQueue());

        Assertions.assertEquals(JacksonUtils.toString(requestDto), requestResult);

    }

    @Test
    @DisplayName("쿠폰 발급 - 존재하지 않는 경우")
    void issue_fail_1() {
        long couponId = 1;
        long userId = 1;

        CouponIssueException exception = Assertions.assertThrows(CouponIssueException.class, () ->
            couponIssueV2Service.issue(couponId,userId));

        Assertions.assertEquals(exception.getErrorCode(), COUPON_NOT_EXIST);
    }

    @Test
    @DisplayName("쿠폰 발급 - 수량이 초과한 경우")
    void issue_fail_2() {

        long userId = 100;

        Coupon coupon = MockData.getOverIssuedCoupon();
        couponJpaRepository.save(coupon);

        IntStream.range(0, coupon.getTotalQuantity()).forEach(idx -> {
            redisTemplate.opsForSet().add(getCouponKey(coupon.getId()), String.valueOf(idx));
        });

        CouponIssueException exception = Assertions.assertThrows(CouponIssueException.class, () ->
            couponIssueV2Service.issue(coupon.getId(),userId));

        Assertions.assertEquals(exception.getErrorCode(), INVALID_COUPON_ISSUE_QUANTITY);
    }

    @Test
    @DisplayName("쿠폰 발급 - 이미 발급된 경우")
    void issue_fail_3() {
        long couponId = 1;
        long userId = 1;

        Coupon coupon = MockData.getOverIssuedCoupon();
        couponJpaRepository.save(coupon);

        redisTemplate.opsForSet().add(getCouponKey(coupon.getId()),String.valueOf(userId));

        CouponIssueException exception = Assertions.assertThrows(CouponIssueException.class, () ->
            couponIssueV2Service.issue(couponId,userId));

        Assertions.assertEquals(exception.getErrorCode(), ALREADY_ISSUED);
    }


}