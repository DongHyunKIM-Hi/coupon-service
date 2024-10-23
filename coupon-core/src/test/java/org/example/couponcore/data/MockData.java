package org.example.couponcore.data;

import java.time.LocalDate;
import java.time.LocalDateTime;
import org.example.couponcore.model.entity.base.Coupon;
import org.example.couponcore.model.entity.base.CouponIssue;
import org.example.couponcore.model.enums.CouponType;

public class MockData {

    public static Coupon getCoupon() {
        return Coupon.builder()
            .couponType(CouponType.FIRST_COME_FIRST_SERVED)
            .title("선착순 쿠폰 테스트 발급")
            .totalQuantity(10)
            .issuedQuantity(0)
            .issueStartDateTime(LocalDateTime.now().minusDays(1))
            .issueEndDateTime(LocalDateTime.now().plusDays(1))
            .build();
    };

    public static Coupon getOverIssuedCoupon() {
        return Coupon.builder()
            .couponType(CouponType.FIRST_COME_FIRST_SERVED)
            .title("선착순 쿠폰 테스트 발급")
            .totalQuantity(10)
            .issuedQuantity(10)
            .issueStartDateTime(LocalDateTime.now().minusDays(1))
            .issueEndDateTime(LocalDateTime.now().plusDays(1))
            .build();
    };

    public static Coupon getOverDateCoupon() {
        return Coupon.builder()
            .couponType(CouponType.FIRST_COME_FIRST_SERVED)
            .title("선착순 쿠폰 테스트 발급")
            .totalQuantity(10)
            .issuedQuantity(0)
            .issueStartDateTime(LocalDateTime.now().minusDays(2))
            .issueEndDateTime(LocalDateTime.now().minusDays(1))
            .build();
    };

    public static CouponIssue getCouponIssue() {
        return  CouponIssue.builder()
            .couponId(1L)
            .userId(1L)
            .build();
    }
}
