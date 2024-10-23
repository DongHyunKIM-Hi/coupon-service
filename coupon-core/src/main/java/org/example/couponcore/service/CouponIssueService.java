package org.example.couponcore.service;

import lombok.RequiredArgsConstructor;
import org.example.couponcore.model.entity.base.Coupon;
import org.example.couponcore.repository.CouponIssueRepository;
import org.example.couponcore.repository.CouponRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
@Service
public class CouponIssueService {

    private final CouponRepository couponRepository;
    private final CouponIssueRepository couponIssueRepository;


    @Transactional
    public void issue(long couponId, long userId) {
        Coupon coupon = findCoupon(couponId);
        coupon.issue();

    }

    @Transactional(readOnly = true)
    public Coupon findCoupon(long couponId){
        return couponRepository.findCouponById(couponId);
    };
}
