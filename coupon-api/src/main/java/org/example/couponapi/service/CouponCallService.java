package org.example.couponapi.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.couponapi.model.request.CouponIssueRequestDto;
import org.example.couponapi.model.response.CouponIssueResponseDto;
import org.example.couponcore.component.LockExecutor;
import org.example.couponcore.service.CouponIssueRedisService;
import org.example.couponcore.service.CouponIssueV1Service;
import org.example.couponcore.service.CouponIssueV2Service;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
public class CouponCallService {

    private final CouponIssueV1Service couponIssueV1Service;
    private final CouponIssueV2Service couponIssueV2Service;
    private final CouponIssueRedisService couponIssueRedisService;
    private final LockExecutor lockExecutor;

    public CouponIssueResponseDto issueRequest(CouponIssueRequestDto requestDto) {
        String lockName = "lock_%s".formatted(requestDto.couponId());
        lockExecutor.execute(lockName ,10000,10000, () -> {
            couponIssueV1Service.issue(requestDto.couponId(),requestDto.userId());
        });
        log.info("쿠폰 발급 완료 :: 쿠폰 ID : %s , 유저 ID : %s".formatted(requestDto.couponId(),requestDto.couponId()));
        return new CouponIssueResponseDto(true,null);
    }

    public CouponIssueResponseDto issueRequestBySortedSet(CouponIssueRequestDto requestDto) {
        couponIssueRedisService.issueBySortedSet(requestDto.couponId(),requestDto.userId());
        return new CouponIssueResponseDto(true,null);
    }

    public CouponIssueResponseDto issueRequestBySet(CouponIssueRequestDto requestDto) {
        couponIssueV2Service.issue(requestDto.couponId(),requestDto.userId());
        return new CouponIssueResponseDto(true,null);
    }
}
