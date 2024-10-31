package org.example.couponwebflux.utils;

public class CouponRedisUtils {

    public static String getCouponKey(long couponId) {
        return "issue:couponId:%s".formatted(couponId);
    }

    public static String getCouponIssueQueue() {
        return "issue:queue";
    }
}