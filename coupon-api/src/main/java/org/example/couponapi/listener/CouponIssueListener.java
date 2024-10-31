package org.example.couponapi.listener;

import static org.example.couponcore.utils.CouponRedisUtils.getCouponIssueQueue;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.couponcore.model.dto.request.CouponIssueRequestDto;
import org.example.couponcore.repository.CouponReactiveRedisRepository;
import org.example.couponcore.service.CouponIssueV1Service;
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

    private final CouponReactiveRedisRepository couponReactiveRedisRepository;
    private final CouponIssueV1Service couponIssueService;

    @Scheduled(fixedDelay = 10000)
    public void issue() {
        log.info("listen...");
        while (existCouponIssueTarget()) {
            CouponIssueRequestDto target = getIssueTarget();
            log.info("발급 시작 target: {}", target);
            couponIssueService.issue(target.couponId(), target.userId());
            log.info("발급 완료 target: {}", target);
            removeIssuedTarget();
        }
    }

    private CouponIssueRequestDto getIssueTarget(){
        return JacksonUtils.toModel(couponReactiveRedisRepository.lIndex(getCouponIssueQueue(), 0), CouponIssueRequestDto.class);
    }

    private boolean existCouponIssueTarget() {
        return couponReactiveRedisRepository.lSize(getCouponIssueQueue()) > 0;
    }

    private void removeIssuedTarget() {
        couponReactiveRedisRepository.lPop(getCouponIssueQueue());
    }
}
