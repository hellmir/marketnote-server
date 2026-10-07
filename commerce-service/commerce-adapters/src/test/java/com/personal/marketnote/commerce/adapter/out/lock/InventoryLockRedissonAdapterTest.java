package com.personal.marketnote.commerce.adapter.out.lock;

import com.personal.marketnote.commerce.exception.InventoryLockAcquisitionException;
import com.personal.marketnote.commerce.exception.InventoryLockInterruptedException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;

import java.util.LinkedHashSet;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("InventoryLockRedissonAdapter 단위 테스트")
class InventoryLockRedissonAdapterTest {

    @InjectMocks
    private InventoryLockRedissonAdapter adapter;

    @Mock
    private RedissonClient redissonClient;

    @Mock
    private RLock lock1;

    @Mock
    private RLock lock2;

    @BeforeEach
    void setUp() {
        // no-op
    }

    @Test
    @DisplayName("정렬된 가격정책 ID 순서로 락을 획득하고 task 실행 후 역순으로 해제한다")
    void shouldAcquireLocksAndRunTaskInOrder() throws Exception {
        // given
        when(redissonClient.getLock("lock:inventory:price-policy:1")).thenReturn(lock1);
        when(redissonClient.getLock("lock:inventory:price-policy:2")).thenReturn(lock2);
        when(lock1.tryLock(anyLong(), anyLong(), eq(TimeUnit.SECONDS))).thenReturn(true);
        when(lock2.tryLock(anyLong(), anyLong(), eq(TimeUnit.SECONDS))).thenReturn(true);
        when(lock1.isHeldByCurrentThread()).thenReturn(true);
        when(lock2.isHeldByCurrentThread()).thenReturn(true);

        AtomicBoolean executed = new AtomicBoolean(false);

        // when
        adapter.executeWithLock(new LinkedHashSet<>(Set.of(2L, 1L)), () -> executed.set(true));

        // then
        assertThat(executed.get()).isTrue();
        verify(lock1, times(1)).tryLock(anyLong(), anyLong(), eq(TimeUnit.SECONDS));
        verify(lock2, times(1)).tryLock(anyLong(), anyLong(), eq(TimeUnit.SECONDS));
        verify(lock1, times(1)).unlock();
        verify(lock2, times(1)).unlock();
    }

    @Test
    @DisplayName("락 획득 실패 시 InventoryLockAcquisitionException을 던지고 task는 실행되지 않는다")
    void shouldThrowWhenLockNotAcquired() throws Exception {
        // given
        when(redissonClient.getLock("lock:inventory:price-policy:1")).thenReturn(lock1);
        when(lock1.tryLock(anyLong(), anyLong(), eq(TimeUnit.SECONDS))).thenReturn(false);
        AtomicBoolean executed = new AtomicBoolean(false);

        // when & then
        assertThatThrownBy(() -> adapter.executeWithLock(Set.of(1L), () -> executed.set(true)))
                .isInstanceOf(InventoryLockAcquisitionException.class);
        assertThat(executed.get()).isFalse();
    }

    @Test
    @DisplayName("InterruptedException 발생 시 InventoryLockInterruptedException으로 변환한다")
    void shouldWrapInterruptedException() throws Exception {
        // given
        when(redissonClient.getLock("lock:inventory:price-policy:1")).thenReturn(lock1);
        when(lock1.tryLock(anyLong(), anyLong(), eq(TimeUnit.SECONDS))).thenThrow(new InterruptedException("interrupted"));

        // when & then
        assertThatThrownBy(() -> adapter.executeWithLock(Set.of(1L), () -> {}))
                .isInstanceOf(InventoryLockInterruptedException.class);
        Thread.interrupted();
    }

    @Test
    @DisplayName("예외 발생 후에도 이미 획득한 락은 모두 해제한다")
    void shouldReleaseAcquiredLocksWhenLaterLockFails() throws Exception {
        // given
        when(redissonClient.getLock("lock:inventory:price-policy:1")).thenReturn(lock1);
        when(redissonClient.getLock("lock:inventory:price-policy:2")).thenReturn(lock2);
        when(lock1.tryLock(anyLong(), anyLong(), eq(TimeUnit.SECONDS))).thenReturn(true);
        when(lock2.tryLock(anyLong(), anyLong(), eq(TimeUnit.SECONDS))).thenReturn(false);
        when(lock1.isHeldByCurrentThread()).thenReturn(true);

        // when & then
        assertThatThrownBy(() -> adapter.executeWithLock(Set.of(1L, 2L), () -> {}))
                .isInstanceOf(InventoryLockAcquisitionException.class);
        verify(lock1).unlock();
        verify(lock2, times(0)).unlock();
    }
}
