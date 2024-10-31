package org.example.couponwebflux.listener;


import static org.example.couponwebflux.utils.CouponRedisUtils.getCouponIssueQueue;

import jakarta.annotation.PostConstruct;
import java.time.Duration;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.couponwebflux.model.dto.request.CouponIssueRequestDto;
import org.example.couponwebflux.repository.CouponRedisRepository;
import org.example.couponwebflux.service.CouponIssueService;
import org.example.couponwebflux.utils.JacksonUtils;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

@RequiredArgsConstructor
@EnableScheduling
@Component
@Slf4j
public class CouponIssueListener {

    private final CouponRedisRepository couponRedisRepository;
    private final CouponIssueService couponIssueService;

    @PostConstruct
    public void scheduleCouponIssuing() {
        Flux.interval(Duration.ofSeconds(10))
            .flatMap(tick -> processCouponIssues())
            .subscribeOn(Schedulers.boundedElastic())
            .subscribe();
    }

    // 쿠폰 발급 대기열의 크기를 확인하고 발급 대상을 병렬로 처리하여 쿠폰 발급 요청 메서드를 호출하는 역할
    public Mono<Void> processCouponIssues() {
        return Flux.defer(() -> couponRedisRepository.lSize(getCouponIssueQueue()))
            .filter(size -> size > 0) // 대기열 큐가 0이면 스킵
            .flatMap(size -> Flux.range(0, size.intValue()) // 대기열 크기만큼 병렬 처리
                .parallel() // 병렬로 처리
                .runOn(Schedulers.parallel()) // 병렬 스케줄러 설정
                .flatMap(index -> issueSingleCoupon()) // 성공 인덱스 반환
                .sequential()) // 병렬 처리된 작업을 단일 스트림으로 결합
            .then();
    }

    // 성공적으로 쿠폰 발급 요청이 처리된 후 인덱스를 수집하는 메서드
    private Mono<Void> issueSingleCoupon() {
        return Mono.defer(() -> couponRedisRepository.lPop(getCouponIssueQueue()))
            .flatMap(data -> {
                try {
                    CouponIssueRequestDto target = JacksonUtils.toModel(data, CouponIssueRequestDto.class);
                    log.info("발급 시작 target: {}", target);
                    return couponIssueService.issue(target.couponId(), target.userId())
                        .doOnSuccess(v -> log.info("발급 완료 target: {}", target))
                        .onErrorResume(e -> handleIssueError(e, target)); // 발급 중 에러 처리
                } catch (Exception e) {
                    return handleParseError(e, data); // JSON 파싱 에러 처리
                }
            });
    }

    // JSON 파싱 중 에러가 발생했을 때 호출될 메서드
    private Mono<Void> handleParseError(Throwable e, String data) {
        log.error("JSON 파싱 중 오류 발생, data: {}, 오류: {}", data, e.getMessage());
        // 추가적인 에러 처리 로직
        return Mono.empty(); // 에러 처리 후 빈 Mono 반환
    }

    // 발급 중 에러가 발생했을 때 호출될 메서드
    private Mono<Void> handleIssueError(Throwable e, CouponIssueRequestDto target) {
        log.error("쿠폰 발급 중 오류 발생 target: {}, 오류: {}", target, e.getMessage());
        // 추가적인 에러 처리 로직
        return Mono.empty(); // 에러 처리 후 빈 Mono 반환
    }
}
