package org.example.couponcore.repository;

import org.example.couponcore.exception.CouponIssueException;
import org.example.couponcore.exception.ErrorCode;
import org.example.couponcore.model.entity.base.Coupon;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CouponJpaRepository extends JpaRepository<Coupon, Long> {

    default Coupon findCouponById(Long couponId) {
        return findById(couponId).orElseThrow(()
            -> new CouponIssueException(ErrorCode.COUPON_NOT_EXIST, "해당 쿠폰이 존재하지 않습니다. 검색한 쿠폰 아이디 : %s".formatted(couponId)));
    }
}
