package org.example.couponcore.model.redis;

import static org.example.couponcore.exception.ErrorCode.INVALID_COUPON_ISSUE_DATETIME;
import static org.example.couponcore.exception.ErrorCode.INVALID_COUPON_ISSUE_QUANTITY;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateTimeDeserializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer;
import java.time.LocalDateTime;
import org.example.couponcore.exception.CouponIssueException;
import org.example.couponcore.model.entity.base.Coupon;
import org.example.couponcore.model.enums.CouponType;

public record CouponRedisEntity(
    Long id,
    CouponType couponType,
    Integer totalQuantity,
    boolean availableIssueQuantity,
    @JsonSerialize(using = LocalDateTimeSerializer.class)
    @JsonDeserialize(using = LocalDateTimeDeserializer.class)
    LocalDateTime issueStartDateTime,
    @JsonSerialize(using = LocalDateTimeSerializer.class)
    @JsonDeserialize(using = LocalDateTimeDeserializer.class)
    LocalDateTime issueEndDateTime
) {

    public CouponRedisEntity(Coupon coupon) {
        this(
            coupon.getId(),
            coupon.getCouponType(),
            coupon.getTotalQuantity(),
            coupon.availableIssueQuantity(),
            coupon.getIssueStartDateTime(),
            coupon.getIssueEndDateTime()
        );
    }


    public boolean availableIssueDate() {
        LocalDateTime now = LocalDateTime.now();
        return issueStartDateTime.isBefore(now) && issueEndDateTime.isAfter(now);
    }

    public void checkIssuableCoupon() {
        if (!availableIssueQuantity) {
            throw new CouponIssueException(INVALID_COUPON_ISSUE_QUANTITY, "모든 발급 수량이 소진되었습니다. coupon_id : %s".formatted(id));
        }
        if (!availableIssueDate()) {
            throw new CouponIssueException(INVALID_COUPON_ISSUE_DATETIME, "발급 가능한 일자가 아닙니다. request : %s, issueStart: %s, issueEnd: %s".formatted(LocalDateTime.now(), issueStartDateTime, issueEndDateTime));
        }
    }
}
