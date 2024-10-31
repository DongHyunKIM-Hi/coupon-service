package org.example.couponcore.service.cache;

import static org.example.couponcore.utils.CouponRedisUtils.getCouponIssueQueue;
import static org.example.couponcore.utils.CouponRedisUtils.getCouponKey;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.couponcore.exception.CouponIssueException;
import org.example.couponcore.exception.ErrorCode;
import org.example.couponcore.model.redis.CouponRedisEntity;
import org.example.couponcore.utils.JacksonUtils;
import org.example.couponcore.model.dto.request.CouponIssueRequestDto;
import org.example.couponcore.repository.CouponReactiveRedisRepository;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class CouponIssueRedisService {

    private final CouponReactiveRedisRepository couponReactiveRedisRepository;


    public void issueBySortedSet(long couponId, long userId) {
        String key = "issue:sorted_set:couponId:%s".formatted(couponId);
        couponReactiveRedisRepository.zAdd(key, String.valueOf(userId), System.currentTimeMillis());
    }

    public void issueRequest(long couponId, long userId) {
        CouponIssueRequestDto requestDto = new CouponIssueRequestDto(couponId,userId);
        couponReactiveRedisRepository.sAdd(getCouponKey(requestDto.couponId()), String.valueOf(requestDto.userId())); // 발급 수량 제어의 목적
        couponReactiveRedisRepository.rPush(getCouponIssueQueue(), JacksonUtils.toString(requestDto)); // 쿠폰 발급 리스트에 추가
    }

    public void checkValidateToIssueCoupon(long couponId, long userId, CouponRedisEntity coupon) {

       coupon.checkIssuableCoupon(); // 발급 수량 && 발급 기간 검증

        if(!isAvailableQuantity(couponId,coupon.totalQuantity())) { // 발급 수량 검증 : redis 캐시 업데이트 처리 전에 요청이 들어올 수 있으니 한번 더 검증
            throw new CouponIssueException(ErrorCode.INVALID_COUPON_ISSUE_QUANTITY, "발급 수량을 초과했습니다. couponId: %s".formatted(couponId));
        }

        if(!isAvailableIssue(couponId,userId)) { // 중복 발급 검증
            throw new CouponIssueException(ErrorCode.ALREADY_ISSUED, "이미 발급 처리가 되었습니다. userId : %s, couponId : %s".formatted(userId,couponId));
        }
    }

    private boolean isAvailableQuantity(long couponId, Integer totalQuantity) {
        if (totalQuantity == null) {
            return true;
        }

        String key = getCouponKey(couponId);
        return totalQuantity > couponReactiveRedisRepository.sCard(key);
    }

    private boolean isAvailableIssue(long couponId, long userId) {
        String key = getCouponKey(couponId);
        return !couponReactiveRedisRepository.sIsMember(key, String.valueOf(userId));
    }
}