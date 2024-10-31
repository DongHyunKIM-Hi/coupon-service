package org.example.couponconsumer.listener;

import static org.example.couponcore.utils.CouponRedisUtils.getCouponIssueQueue;

import jakarta.annotation.PostConstruct;
import java.time.Duration;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.couponconsumer.repository.CouponRedisRepository;
import org.example.couponconsumer.service.CouponIssueService;
import org.example.couponcore.model.dto.request.CouponIssueRequestDto;
import org.example.couponcore.utils.JacksonUtils;
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
                .flatMap(index -> issueSingleCoupon(index).thenReturn(index)) // 성공 인덱스 반환
                .sequential()) // 병렬 처리된 작업을 단일 스트림으로 결합
            .collectList() // 성공한 인덱스 수집
            .flatMap(this::removeIssuedTargets) // 수집된 인덱스에 해당하는 항목 삭제
            .then();
    }

    // 성공적으로 쿠폰 발급 요청이 처리된 후 인덱스를 수집하는 메서드
    private Mono<Void> issueSingleCoupon(int index) {
        return Mono.defer(() -> couponRedisRepository.lIndex(getCouponIssueQueue(), index))
            .map(data -> JacksonUtils.toModel(data, CouponIssueRequestDto.class))
            .flatMap(target -> {
                log.info("발급 시작 target: {}", target);
                return couponIssueService.issue(target.couponId(), target.userId())
                    .doOnSuccess(v -> log.info("발급 완료 target: {}", target));
            });
    }

    // 수집된 인덱스에 해당하는 항목들을 대기열에서 제거
    private Mono<Void> removeIssuedTargets(List<Integer> indexList) {
        return Flux.fromIterable(indexList)
            .flatMap(index -> couponRedisRepository.setIndexToDeleted(getCouponIssueQueue(), index)) // 항목 무효화
            .then(Mono.defer(() -> // 무효화된 항목을 제외한 리스트로 재구성
                couponRedisRepository.getAllItems(getCouponIssueQueue())
                    .map(items -> {
                        items.removeIf("__deleted__"::equals); // "__deleted__" 항목을 제외
                        return items;
                    })
                    .flatMap(items -> couponRedisRepository.replaceList(getCouponIssueQueue(), items))
            ));
    }
}
