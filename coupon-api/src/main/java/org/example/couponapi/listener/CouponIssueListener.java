package org.example.couponapi.listener;

import static org.example.couponcore.utils.CouponRedisUtils.getCouponIssueQueue;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.couponcore.model.dto.request.CouponIssueRequestDto;
import org.example.couponcore.repository.CouponRedisRepository;
import org.example.couponcore.service.CouponIssueV1Service;
import org.example.couponcore.service.cache.CacheUpdatePublisher;
import org.example.couponcore.utils.JacksonUtils;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@RequiredArgsConstructor
@EnableScheduling
@Component
@Slf4j
public class CouponIssueListener {

    // TODO : webflux 처리량 극대화 시키기

    private final CouponRedisRepository couponRedisRepository;
    private final CouponIssueV1Service couponIssueService;
    private final CacheUpdatePublisher cacheUpdatePublisher;

    @Scheduled(fixedDelay = 10000)
    public void issue() {
        log.info("listen...");
        while (existCouponIssueTarget()) {
            CouponIssueRequestDto target = getIssueTarget();
            log.info("발급 시작 target: {}", target);
            couponIssueService.issue(target.couponId(), target.userId());
            log.info("발급 완료 target: {}", target);
            removeIssuedTarget(target);
        }
    }

    private CouponIssueRequestDto getIssueTarget(){
        return JacksonUtils.toModel(couponRedisRepository.lIndex(getCouponIssueQueue(), 0), CouponIssueRequestDto.class);
    }

    private boolean existCouponIssueTarget() {
        return couponRedisRepository.lSize(getCouponIssueQueue()) > 0;
    }

    private void removeIssuedTarget(CouponIssueRequestDto target) {
        couponRedisRepository.lPop(getCouponIssueQueue());
        // 발급이 완료 되면 로컬 캐시, Redis 캐시 초기화 -> 발급 수량 검증 하는 곳으로 로직을 옮겨야 할 듯.
        cacheUpdatePublisher.publishCacheUpdate(String.valueOf(target.couponId()));
    }
}
