package org.example.couponconsumer.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.couponconsumer.repository.CouponIssueR2DBRepository;
import org.example.couponconsumer.repository.CouponR2DBRepository;
import org.example.couponconsumer.repository.CouponR2DBRepositoryClient;
import org.example.couponcore.exception.CouponIssueException;
import org.example.couponcore.exception.ErrorCode;
import org.example.couponcore.model.entity.base.Coupon;
import org.example.couponcore.model.entity.base.CouponIssue;
import org.example.couponcore.model.event.CouponIssueCompleteEvent;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Mono;


@Service
@RequiredArgsConstructor
public class CouponIssueService {

    private final CouponR2DBRepository couponR2DBRepository;
    private final CouponR2DBRepositoryClient couponR2DBRepositoryClient;
    private final CouponIssueR2DBRepository couponIssueR2DBRepository;
    private final ApplicationEventPublisher applicationEventPublisher;

    @Transactional
    public Mono<Void> issue(long couponId, long userId) {
        return findCoupon(couponId)
            .flatMap(coupon -> {
                try {
                    coupon.issue(); // 쿠폰 발급 시도 (예외가 발생하면 Mono.error로 처리)
                    return saveCouponIssue(couponId, userId)
                        .doOnSuccess(savedIssue -> publishCouponEvent(coupon)) // 발급 완료 시 이벤트 발행
                        .then();
                } catch (CouponIssueException e) {
                    return Mono.error(e); // 이미 발급된 경우 예외를 Mono.error로 반환
                }
            });
    }

    @Transactional
    public Mono<CouponIssue> saveCouponIssue(long couponId, long userId) {
        return checkAlreadyIssuance(couponId, userId)
            .switchIfEmpty(Mono.defer(() -> couponIssueR2DBRepository.save(CouponIssue.build(couponId, userId))));
    }

    private Mono<CouponIssue> checkAlreadyIssuance(long couponId, long userId) {
        return couponR2DBRepositoryClient.findFirstCouponIssue(couponId, userId)
            .flatMap(issue -> {
                if (issue != null) {
                    return Mono.error(new CouponIssueException(ErrorCode.ALREADY_ISSUED, "이미 쿠폰이 발급되었습니다."));
                }
                return Mono.empty(); // 기존 발급 이력이 없으면 빈 Mono 반환
            });
    }

    @Transactional(readOnly = true)
    public Mono<Coupon> findCoupon(long couponId) {
        return couponR2DBRepository.findById(couponId);
    }



    private void publishCouponEvent(Coupon coupon) {
        if (coupon.isIssueComplete()) {
            applicationEventPublisher.publishEvent(new CouponIssueCompleteEvent(coupon.getId()));
        }
    }
}

