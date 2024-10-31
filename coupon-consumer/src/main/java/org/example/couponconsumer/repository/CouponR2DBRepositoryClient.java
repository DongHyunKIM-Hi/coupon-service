package org.example.couponconsumer.repository;

import lombok.RequiredArgsConstructor;
import org.example.couponcore.model.entity.base.CouponIssue;
import org.springframework.r2dbc.core.DatabaseClient;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Mono;

@RequiredArgsConstructor
@Repository
public class CouponR2DBRepositoryClient {

    private final DatabaseClient databaseClient;

    public Mono<CouponIssue> findFirstCouponIssue(long couponId, long userId) {
        return databaseClient
            .sql("SELECT * FROM coupon_issue WHERE coupon_id = :couponId AND user_id = :userId LIMIT 1")
            .bind("couponId", couponId)
            .bind("userId", userId)
            .map((row, metadata) -> CouponIssue.builder()
                .id(row.get("id", Long.class))
                .couponId(row.get("coupon_id", Long.class))
                .userId(row.get("user_id", Long.class))
                .build())
            .one();
    }
}
