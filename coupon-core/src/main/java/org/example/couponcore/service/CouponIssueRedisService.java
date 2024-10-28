package org.example.couponcore.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.couponcore.repository.CouponRedisRepository;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class CouponIssueRedisService {

    private final CouponRedisRepository couponRedisRepository;


    public void issueBySortedSet(long couponId, long userId) {
        String key = "issue:sorted_set:couponId:%s".formatted(couponId);
        couponRedisRepository.zAdd(key, String.valueOf(userId), System.currentTimeMillis());
    }
}
