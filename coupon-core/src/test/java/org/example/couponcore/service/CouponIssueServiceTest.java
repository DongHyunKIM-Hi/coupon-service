package org.example.couponcore.service;

import static org.junit.jupiter.api.Assertions.*;

import org.example.couponcore.config.TestConfig;
import org.example.couponcore.data.MockData;
import org.example.couponcore.exception.CouponIssueException;
import org.example.couponcore.exception.ErrorCode;
import org.example.couponcore.model.entity.base.Coupon;
import org.example.couponcore.model.entity.base.CouponIssue;
import org.example.couponcore.repository.CouponIssueJpaRepository;
import org.example.couponcore.repository.CouponJpaRepository;
import org.example.couponcore.repository.CouponRepository;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

class CouponIssueServiceTest extends TestConfig {

    @Autowired
    CouponIssueService service;

    @Autowired
    CouponIssueJpaRepository couponIssueJpaRepository;

    @Autowired
    CouponRepository couponRepository;

    @Autowired
    CouponJpaRepository couponJpaRepository;

    @BeforeEach
    void setup() {
        couponJpaRepository.deleteAll();
        couponIssueJpaRepository.deleteAll();
    }

    @Test
    @DisplayName("쿠폰 검증 로직이 성공적으로 마무리되면 쿠폰 발급")
    void issue_success_1() {

        long userId = 1;
        Coupon coupon = MockData.getCoupon();

        couponJpaRepository.save(coupon);

        // when
        service.issue(coupon.getId(), userId);

        // then
        Coupon couponResult = couponJpaRepository.findCouponById(coupon.getId());

        Assertions.assertEquals(couponResult.getIssuedQuantity(),1);

        CouponIssue couponIssueResult = couponRepository.findFirstCouponIssue(coupon.getId(), userId);
        Assertions.assertNotNull(couponIssueResult);
    }

    @Test
    @DisplayName("발급 가능 수량이 초과한 경우")
    void issue_fail_1() {

        long userId = 1;
        Coupon coupon = MockData.getOverIssuedCoupon();

        couponJpaRepository.save(coupon);


        // when
        CouponIssueException exception = Assertions.assertThrows(CouponIssueException.class, () ->{
            service.issue(coupon.getId(), userId);
        });

        // then
        Assertions.assertEquals(exception.getErrorCode(), ErrorCode.INVALID_COUPON_ISSUE_QUANTITY);
    }

    @Test
    @DisplayName("발급 가능 기간이 초과한 경우")
    void issue_fail_2() {

        long userId = 1;
        Coupon coupon = MockData.getOverDateCoupon();

        couponJpaRepository.save(coupon);


        // when
        CouponIssueException exception = Assertions.assertThrows(CouponIssueException.class, () ->{
            service.issue(coupon.getId(), userId);
        });

        // then
        Assertions.assertEquals(exception.getErrorCode(), ErrorCode.INVALID_COUPON_ISSUE_DATETIME);
    }


    @Test
    @DisplayName("쿠폰 발급이 정상적으로 처리되면 발급 이력 기록")
    void saveCouponIssue_success_1() {

        CouponIssue couponIssue = MockData.getCouponIssue();

        // when
        CouponIssue issue = service.saveCouponIssue(couponIssue.getCouponId(), couponIssue.getUserId());

        // then
        Assertions.assertEquals(issue.getCouponId(), couponIssue.getCouponId());
        Assertions.assertEquals(issue.getUserId(), couponIssue.getUserId());

    }

    @Test
    @DisplayName("발급 이력이 있는 경우 예외를 반환한다.")
    void saveCouponIssue_fail_1() {

        CouponIssue couponIssue = MockData.getCouponIssue();

        couponIssueJpaRepository.save(couponIssue);

        // when
        CouponIssueException exception = Assertions.assertThrows(CouponIssueException.class, () ->{
            service.saveCouponIssue(couponIssue.getCouponId(), couponIssue.getUserId());
        });

        // then
        Assertions.assertEquals(exception.getErrorCode(), ErrorCode.ALREADY_ISSUED);

    }
}