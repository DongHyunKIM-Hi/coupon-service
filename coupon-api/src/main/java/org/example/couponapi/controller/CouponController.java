package org.example.couponapi.controller;

import lombok.RequiredArgsConstructor;
import org.example.couponapi.model.request.CouponIssueRequestDto;
import org.example.couponapi.model.response.CouponIssueResponseDto;
import org.example.couponapi.service.CouponCallService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class CouponController {

    private final CouponCallService couponCallService;

    @PostMapping("/v1/issue")
    public ResponseEntity<CouponIssueResponseDto> couponIssue(@RequestBody CouponIssueRequestDto requestDto) {
        return ResponseEntity.ok(couponCallService.issueRequest(requestDto));
    }

    @PostMapping("/v2/issue")
    public ResponseEntity<CouponIssueResponseDto> couponIssueBySortedSet(@RequestBody CouponIssueRequestDto requestDto) {
        return ResponseEntity.ok(couponCallService.issueRequestBySortedSet(requestDto));
    }

    @PostMapping("/v3/issue")
    public ResponseEntity<CouponIssueResponseDto> couponIssueBySet(@RequestBody CouponIssueRequestDto requestDto) {
        return ResponseEntity.ok(couponCallService.issueRequestBySet(requestDto));
    }
}
