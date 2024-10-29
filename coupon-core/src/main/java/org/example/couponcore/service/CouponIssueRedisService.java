package org.example.couponcore.service;

import static org.example.couponcore.utils.CouponRedisUtils.getCouponIssueQueue;
import static org.example.couponcore.utils.CouponRedisUtils.getCouponKey;

import java.time.LocalDate;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.couponcore.config.JacksonUtils;
import org.example.couponcore.exception.CouponIssueException;
import org.example.couponcore.exception.ErrorCode;
import org.example.couponcore.model.dto.request.CouponIssueRequestDto;
import org.example.couponcore.model.entity.base.Coupon;
import org.example.couponcore.repository.CouponRedisRepository;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class CouponIssueRedisService {

    private final CouponRedisRepository couponRedisRepository;
    private final CouponIssueService couponIssueService;


    public void issueBySortedSet(long couponId, long userId) {
        String key = "issue:sorted_set:couponId:%s".formatted(couponId);
        couponRedisRepository.zAdd(key, String.valueOf(userId), System.currentTimeMillis());
    }

    public void issue(long couponId, long userId) {

        Coupon coupon = couponIssueService.findCoupon(couponId);
        checkValidateToIssueCoupon(couponId,userId,coupon);
        issueRequest(couponId,userId);

    }

    private void checkValidateToIssueCoupon(long couponId, long userId, Coupon coupon) {

        if(!coupon.availableIssueDate()) {
            throw new CouponIssueException(ErrorCode.INVALID_COUPON_ISSUE_DATETIME, "발급 가능 일자가 지났습니다. 요청일자 %s, 발급 가능 시작일 : %s, 발급 가능 종료일 : %s".formatted(
                LocalDate.now(), coupon.getIssueStartDateTime(), coupon.getIssueEndDateTime()
            ));
        }
        if(!isAvailableQuantity(couponId,coupon.getTotalQuantity())) {
            throw new CouponIssueException(ErrorCode.INVALID_COUPON_ISSUE_QUANTITY, "발급 수량을 초과했습니다. couponId: %s".formatted(couponId));
        }
        if(!isAvailableIssue(couponId,userId)) {
            throw new CouponIssueException(ErrorCode.ALREADY_ISSUED, "이미 발급 처리가 되었습니다. userId : %s, couponId : %s".formatted(userId,couponId));
        }
    }

    private void issueRequest(long couponId, long userId) {
        couponRedisRepository.sAdd(getCouponKey(couponId), String.valueOf(userId)); // 발급 수량 제어의 목적
        CouponIssueRequestDto requestDto = new CouponIssueRequestDto(couponId,userId);
        couponRedisRepository.rPush(getCouponIssueQueue(), JacksonUtils.toString(requestDto)); // 쿠폰 발급 리스트에 추가
    }

    private boolean isAvailableQuantity(long couponId, Integer totalQuantity) {
        if (totalQuantity == null) {
            return true;
        }

        String key = getCouponKey(couponId);
        return totalQuantity > couponRedisRepository.sCard(key);
    }

    private boolean isAvailableIssue(long couponId, long userId) {
        String key = getCouponKey(couponId);
        return !couponRedisRepository.sIsMember(key, String.valueOf(userId));
    }

}