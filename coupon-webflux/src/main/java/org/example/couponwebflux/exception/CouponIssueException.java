package org.example.couponwebflux.exception;

import lombok.Getter;

@Getter
public class CouponIssueException extends RuntimeException {

    private final ErrorCode errorCode;
    private final String message;

    public CouponIssueException(ErrorCode errorCode, String message) {
        this.errorCode = errorCode;
        this.message = message;
    }
}
