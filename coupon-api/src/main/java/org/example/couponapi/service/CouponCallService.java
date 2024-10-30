package org.example.couponapi.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.couponapi.model.request.CouponIssueRequestDto;
import org.example.couponapi.model.response.CouponIssueResponseDto;
import org.example.couponcore.component.LockExecutor;
import org.example.couponcore.service.CouponIssueRedisService;
import org.example.couponcore.service.CouponIssueV1Service;
import org.example.couponcore.service.CouponIssueV2Service;
import org.example.couponcore.service.CouponIssueV3Service;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
public class CouponCallService {

    private final CouponIssueV1Service couponIssueV1Service;
    private final CouponIssueV2Service couponIssueV2Service;
    private final CouponIssueV3Service couponIssueV3Service;

    public CouponIssueResponseDto issueRequestV1(CouponIssueRequestDto requestDto) {
        couponIssueV1Service.issue(requestDto.couponId(),requestDto.userId());
        return new CouponIssueResponseDto(true,null);
    }

    public CouponIssueResponseDto issueRequestV2(CouponIssueRequestDto requestDto) {
        couponIssueV2Service.issue(requestDto.couponId(),requestDto.userId());
        return new CouponIssueResponseDto(true,null);
    }

    public CouponIssueResponseDto issueRequestV3(CouponIssueRequestDto requestDto) {
        couponIssueV3Service.issue(requestDto.couponId(),requestDto.userId());
        return new CouponIssueResponseDto(true,null);
    }
}
