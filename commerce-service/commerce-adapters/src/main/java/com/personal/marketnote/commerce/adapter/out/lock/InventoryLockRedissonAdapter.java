package com.personal.marketnote.commerce.adapter.out.lock;

import com.personal.marketnote.commerce.exception.InventoryLockAcquisitionException;
import com.personal.marketnote.commerce.exception.InventoryLockInterruptedException;
import com.personal.marketnote.commerce.port.out.inventory.InventoryLockPort;
import lombok.RequiredArgsConstructor;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;

@Repository
@RequiredArgsConstructor
public class InventoryLockRedissonAdapter implements InventoryLockPort {
    private static final String LOCK_KEY_PREFIX = "lock:inventory:price-policy:";
    private static final long WAIT_TIME_SECONDS = 3L;
    private static final long LEASE_TIME_SECONDS = 10L;

    private final RedissonClient redissonClient;

    @Override
    public void executeWithLock(Set<Long> pricePolicyIds, Runnable task) {
        List<RLock> acquiredLocks = new ArrayList<>();
        List<Long> sortedIds = pricePolicyIds.stream()
                .sorted()
                .toList();

        try {
            for (Long pricePolicyId : sortedIds) {
                RLock lock = redissonClient.getLock(buildKey(pricePolicyId));
                boolean locked = lock.tryLock(WAIT_TIME_SECONDS, LEASE_TIME_SECONDS, TimeUnit.SECONDS);
                if (!locked) {
                    throw new InventoryLockAcquisitionException(pricePolicyId);
                }
                acquiredLocks.add(lock);
            }

            task.run();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new InventoryLockInterruptedException(e);
        } finally {
            for (int i = acquiredLocks.size() - 1; i >= 0; i--) {
                RLock lock = acquiredLocks.get(i);
                if (lock.isHeldByCurrentThread()) {
                    lock.unlock();
                }
            }
        }
    }

    private String buildKey(Long pricePolicyId) {
        return LOCK_KEY_PREFIX + pricePolicyId;
    }
}

