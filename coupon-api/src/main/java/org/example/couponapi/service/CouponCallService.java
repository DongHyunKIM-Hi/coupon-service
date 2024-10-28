package org.example.couponapi.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.couponapi.model.request.CouponIssueRequestDto;
import org.example.couponapi.model.response.CouponIssueResponseDto;
import org.example.couponcore.service.CouponIssueService;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
public class CouponCallService {

    private final CouponIssueService couponIssueService;

    public CouponIssueResponseDto issueRequest(CouponIssueRequestDto requestDto) {
        couponIssueService.issue(requestDto.couponId(),requestDto.userId());
        log.info("쿠폰 발급 완료 :: 쿠폰 ID : %s , 유저 ID : %s".formatted(requestDto.couponId(),requestDto.couponId()));
        return new CouponIssueResponseDto(true,null);
    }

}