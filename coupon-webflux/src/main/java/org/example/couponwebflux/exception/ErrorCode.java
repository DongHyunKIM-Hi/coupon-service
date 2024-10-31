package org.example.couponwebflux.exception;

public enum ErrorCode {

    INVALID_COUPON_ISSUE_QUANTITY("수량 검증 실패"),
    INVALID_COUPON_ISSUE_DATETIME("유효 기간 검증 실패"),
    COUPON_NOT_EXIST("해당 쿠폰이 존재하지 않습니다"),
    ALREADY_ISSUED("이미 쿠폰이 발급 되었습니다."),
    ;

    public final String message;

    ErrorCode(String message) {
        this.message = message;
    }
}
