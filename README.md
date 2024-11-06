# 선착순 쿠폰 발급 프로젝트

### 목적 : 최대한 빠르게 많은 처리를 받는 구조를 만들자!

## 구조
![image](https://github.com/user-attachments/assets/f0c91e8d-513d-4039-8046-b0b2b44b3162)



## V1 분산락 적용
동시성 해결을 위해서 분산락으로 걸었을 떄 
생각보다 처리량이 떨어짐
![스크린샷 2024-10-30 오후 2 44 41](https://github.com/user-attachments/assets/419fe908-63ac-463f-963d-ec78b3d3f481)
![image](https://github.com/user-attachments/assets/64c0e15f-30b8-4375-9f98-365a6cb3091e)
![스크린샷 2024-10-30 오후 1 56 14](https://github.com/user-attachments/assets/aa8647a9-ca08-4679-8718-0d4a702a819f)

## V2 일반적인 레디스 싱글 쓰레드를 믿고 감
락을 걸지 않고 처리를 했을 때 처리량은 높음.
처리 속도는 올라갔지만 정해진 수량인 600개를 초과하는 동시성 문제가 발생함.
![image](https://github.com/user-attachments/assets/e3a9e179-053e-4d6c-b17b-58c6dae402ca)
![image](https://github.com/user-attachments/assets/4cf756f1-288b-489f-b94b-322c334696d3)

## V3 레디스 스크립트를 통한 방법
Redis 스크립트를 통한 해결 방안.
처리 속도도 락을 걸지 않은 상태랑 비슷하고 결과는 락을 걸어서 처리한 것 처럼 정해진 수량 만큼 만 발급되는 것을 확인함.
![image](https://github.com/user-attachments/assets/cebd0e0d-f327-4d0b-ab2a-faf4484b87b9)
![image](https://github.com/user-attachments/assets/fe6a710d-28c3-432f-bff8-709357cd34dc)

**그럼 왜 V2, V3에서 차이가 나는 것일까?** <br>
- 원자적 실행: Lua 스크립트는 Redis 서버 내에서 한 번의 요청으로 실행되므로, 네트워크 왕복 시간이 줄어들어 성능이 개선됨
- 병렬 처리 지원: 여러 명령을 하나의 스크립트로 처리하므로, 추가적인 락 해제나 재시도가 필요 없어 오버헤드가 줄어듬

## V4 로컬 캐시를 사용하여 레디스 네트워크 통신을 최대한 줄인 방법
로컬 캐시를 사용하여 Redis 네트워크 통신을 최대한 줄인 방안
V2와 V3를 비교하면서 Redis 서비스 네트워크 통신 비용 때문에 성능 차이가 나는 것을 발견하였다.
그럼 로컬 캐시를 사용하여 Redis 네트워크 통신을 좀 더 줄이면 성능이 좀 더 개선 되지 않을까? 하는 생각에 진행하였고 결과는 성공적이였음
![image](https://github.com/user-attachments/assets/cae3ec97-de90-48fa-a2d5-c9f1ef02cd9e)

## 흐름

1. 쿠폰에 발급 요청이 들어온다.
2. 쿠폰 발급이 가능한지 검증한다.
   1. 검증할 때 로컬 캐시를 먼저 조회하여 현재 요청한 유저가 해당 쿠폰을 발급 할 수 있는지 검증한다.
   2. 로컬 캐시가 없다면 Redis 캐시를 통해서 데이터를 가져와서 검증한다.
   3. Redis 캐시가 없다면 DB에서 정보를 가져와서 검증한다.
3. 검증 내용으로는 발급 가능한 수량인지, 발급 가능한 일자인지, 기존에 발급한 이력이 있는지를 검사한다.
4. 검증이 완료가 되면 쿠폰 발급 대기 큐에 넣어준다.

1. 1초에 1번씩 발급 대기 큐에 있는 데이터를 뽑아서 쿠폰을 발급해주는 스케쥴러가 실행이 된다.
2. 큐에 있는 데이터를 뽑고 쿠폰을 발급해준다.
3. 발급이 완료되면 로컬 캐시와 Redis 캐시를 업데이트 한다. -> 발급 가능 수량 업데이트 

## V5 Spring Webflux를 통하여 처리하는 속도를 비동기로 더 빠르게 처리하게 수정함.

![image](https://github.com/user-attachments/assets/50ad577c-58b6-4f3b-ae40-be03973ba18b)
![1](https://github.com/user-attachments/assets/c29deba0-9d8d-490e-83dd-8570fc5c11e8)
<img width="787" alt="2" src="https://github.com/user-attachments/assets/d2336b5f-e6fe-45a8-899a-ed5f6402d7ef">


## 결론 : MVC 보다 3배 정도 처리량이 높아 졌다.

10초 마다 작업큐에 있는 메시지 가져와서 쿠폰을 발급해줌

발급에 성공하면 작업큐에서 제거.

1회차 로직
- flatMap을 통해서 쿠폰발급 처리를 실행함.
- 쿠폰에서 발급이 성공하면 redis에서 pop()
문제 발생 : pop() 해서 로직을 처리하게되면 항상 순차적으로 동작하지 않을 수 있는 문제가 발생함.

2회차 로직
- 쿠폰 발급 메서드 실행을 parallel() 통해서 병렬적으로 처리하게 수정함.
- 쿠폰 발급에 성공한 index를 가지고 redis에서 해당 index를 제거하려고함.
- 한번에 여러개의 redis 데이터를 index 기준으로 삭제 시키는 기능이 없음
   - 앞에서 부터 index를 제거하면 뒤의 index가 다 변경이됨 그래서 뒤의 인덱스 부터 제거하도록 수정함.
문제 발생 가능성 : 쿠폰 발급 처리량이 많아서 10초 안에 작업이 끝나지 않으면 뒤의 작업에 영향을 줄 수 있음.

3회차 로직
- 쿠폰 발급에 성공하면 바로 삭제하는 것이 아니라 일단 처리가 완료 되었음의 표식을 남김.
- 표식을 남긴 후 redis에 있는 데이터를 가져오고 그 데이터에 표식이 있는 데이터를 제외하고 발급 대상이였지만 발급 실패한 데이터만 남김.
문제 발생 가능성 : 표식을 남긴 데이터가 제거되기 전에 다음 요청이 들어오면 파싱 문제가 발생함.

 ```
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
```

4회차 (최종 버전)

- 정책적으로 실패한 쿠폰 발급은 수동으로 혹은 이후 실패한 쿠폰을 대상으로 재발급 시도해주는 로직을 두기로함.
- 쿠폰 발급 요청이 들어오면 일단 redis에서 데이터를 pop()을 통해서 꺼냄
- 꺼낸 데이터를 기준으로 파싱 및 쿠폰 발급 처리를 수행함.
- 파싱에서 에러가 나거나 혹은 쿠폰 발급 과정에서 에러가 발생하면 이를 로그로 남겨서 별도로 관리함
   - 이후 실패한 내용에 대해서 @Retryable 혹은 쿠폰 발급 실패 큐를 만들어서 재시도하는 로직 등으로 처리


```
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

```


## 흐름

1. 쿠폰에 발급 요청이 들어온다.
2. 쿠폰 발급이 가능한지 검증한다.
   1. 검증할 때 로컬 캐시를 먼저 조회하여 현재 요청한 유저가 해당 쿠폰을 발급 할 수 있는지 검증한다.
   2. 로컬 캐시가 없다면 Redis 캐시를 통해서 데이터를 가져와서 검증한다.
   3. Redis 캐시가 없다면 DB에서 정보를 가져와서 검증한다.
3. 검증 내용으로는 발급 가능한 수량인지, 발급 가능한 일자인지, 기존에 발급한 이력이 있는지를 검사한다.
4. 검증이 완료가 되면 쿠폰 발급 대기 큐에 넣어준다.

Webflux로 쿠폰 발급에 대한 처리를 비동기로 처리함. feat. reactiveRedis, R2DBC 비동기 모듈 사용하여 동기로 걸리는 부분을 제거함.

1. 10초에 1번씩 발급 대기 큐에 있는 데이터를 뽑아서 쿠폰을 발급해주는 스케쥴러가 실행이 된다.
     - 이는 싱글 쓰레드에서 처리되는 것이 아니라 병렬적으로 처리가 된다. 
2. 큐에 있는 데이터를 뽑고 해당 데이터를 기준으로 쿠폰 발급 요청을 전송한다.
3. 쿠폰 발급 요청시 실패한 경우 로그 및 알람을 통해서 실패에 대한 모니터링을 진행한다.
