package org.example.couponcore.component;

import java.util.concurrent.TimeUnit;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class LockExecutor {

    private final RedissonClient redissonClient;

    public void execute(String lockName, long waitMs, long leaseMs ,Runnable logic) {
        RLock lock = redissonClient.getLock(lockName);
        // 락 획득을 시도하고 락 획득에 성공하면 로직 실행, 락 획득에 실패하면 예외처리 진행
        try {
            boolean isLocked = lock.tryLock(waitMs,leaseMs, TimeUnit.MICROSECONDS);
            if (!isLocked) {
                throw new IllegalStateException("[ %s ] lock 획득에 실패 했습니다.".formatted(lockName));
            }
            logic.run();
        } catch (InterruptedException e) {
            log.info(e.getMessage(), e);
            throw new RuntimeException(e);
        } finally {
            if(lock.isHeldByCurrentThread()) { // 락을 점유하고 있는 쓰레드가 있는지 검사
                lock.unlock();
            }
        }

    }
}
