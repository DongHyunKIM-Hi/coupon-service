package org.example.couponcore.service;

import java.time.LocalDateTime;
import lombok.RequiredArgsConstructor;
import org.example.couponcore.exception.CouponIssueException;
import org.example.couponcore.exception.ErrorCode;
import org.example.couponcore.model.entity.base.Coupon;
import org.example.couponcore.model.entity.base.CouponIssue;
import org.example.couponcore.model.enums.CouponType;
import org.example.couponcore.repository.CouponIssueJpaRepository;
import org.example.couponcore.repository.CouponJpaRepository;
import org.example.couponcore.repository.CouponRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
@Service
public class CouponIssueService {

    private final CouponJpaRepository couponJpaRepository;
    private final CouponRepository couponRepository;
    private final CouponIssueJpaRepository couponIssueJpaRepository;


    @Transactional
    public void issue(long couponId, long userId) {
        Coupon coupon = findCoupon(couponId);
        coupon.issue();
        saveCouponIssue(couponId,userId);

    }

    @Transactional
    public CouponIssue saveCouponIssue(long couponId, long userId) {
        checkAlreadyIssuance(couponId,userId);

        CouponIssue issue = CouponIssue.builder()
            .couponId(couponId)
            .userId(userId)
            .build();

        return couponIssueJpaRepository.save(issue);
    }

    @Transactional(readOnly = true)
    public Coupon findCoupon(long couponId){
        return couponJpaRepository.findCouponById(couponId);
    }

    private void checkAlreadyIssuance(long couponId, long userId ) {
        CouponIssue issue = couponRepository.findFirstCouponIssue(couponId,userId);
        if (issue != null) {
            throw new CouponIssueException(ErrorCode.ALREADY_ISSUED, "이미 쿠폰이 발급 되었습니다.");
        }
    }

}
