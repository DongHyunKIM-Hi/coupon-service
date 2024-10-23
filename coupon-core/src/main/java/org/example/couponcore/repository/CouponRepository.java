package org.example.couponcore.repository;

import static org.example.couponcore.model.entity.base.QCouponIssue.couponIssue;

import com.querydsl.jpa.JPQLQueryFactory;
import lombok.RequiredArgsConstructor;
import org.example.couponcore.model.entity.base.CouponIssue;
import org.springframework.stereotype.Repository;

@RequiredArgsConstructor
@Repository
public class CouponRepository {

    private final JPQLQueryFactory queryFactory;

    public CouponIssue findFirstCouponIssue(long couponId, long userId) {
        return queryFactory.selectFrom(couponIssue)
            .where(couponIssue.couponId.eq(couponId))
            .where(couponIssue.userId.eq(userId))
            .fetchFirst();
    }
}
