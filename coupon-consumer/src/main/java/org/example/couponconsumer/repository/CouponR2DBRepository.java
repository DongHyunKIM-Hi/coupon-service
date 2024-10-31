package org.example.couponconsumer.repository;

import org.example.couponconsumer.entity.R2dbCoupon;
import org.example.couponcore.model.entity.base.Coupon;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Mono;

public interface CouponR2DBRepository extends ReactiveCrudRepository<R2dbCoupon, Long> {

    Mono<R2dbCoupon> findById(Long couponId);
    /*
    default Mono<Coupon> findCouponById(Long couponId) {
        return findById(couponId)
            .switchIfEmpty(Mono.error(new CouponIssueException(ErrorCode.COUPON_NOT_EXIST,
                "해당 쿠폰이 존재하지 않습니다. 검색한 쿠폰 아이디 : %s".formatted(couponId))));
    }*/
}
