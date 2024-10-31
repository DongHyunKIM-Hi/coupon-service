package org.example.couponwebflux.repository;


import org.example.couponwebflux.exception.CouponIssueException;
import org.example.couponwebflux.exception.ErrorCode;
import org.example.couponwebflux.model.entity.Coupon;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Mono;

public interface CouponR2DBRepository extends ReactiveCrudRepository<Coupon, Long> {


    default Mono<Coupon> findCouponById(Long couponId) {
        return findById(couponId)
            .switchIfEmpty(Mono.error(new CouponIssueException(ErrorCode.COUPON_NOT_EXIST,
                "해당 쿠폰이 존재하지 않습니다. 검색한 쿠폰 아이디 : %s".formatted(couponId))));
    }
}
