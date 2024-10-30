package org.example.couponcore.model.redis;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateDeserializer;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateTimeDeserializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer;
import java.time.LocalDate;
import java.time.LocalDateTime;
import org.example.couponcore.exception.CouponIssueException;
import org.example.couponcore.exception.ErrorCode;
import org.example.couponcore.model.entity.base.Coupon;
import org.example.couponcore.model.enums.CouponType;

public record CouponRedisEntity(
    Long id,
    CouponType couponType,
    Integer totalQuantity,

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
            coupon.getIssueStartDateTime(),
            coupon.getIssueEndDateTime()
        );
    }


    public boolean availableIssueDate() {
        LocalDateTime now = LocalDateTime.now();
        return issueStartDateTime.isBefore(now) && issueEndDateTime.isAfter(now);
    }
}
