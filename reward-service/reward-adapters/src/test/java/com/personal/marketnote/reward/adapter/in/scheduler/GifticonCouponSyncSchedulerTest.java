package com.personal.marketnote.reward.adapter.in.scheduler;

import com.personal.marketnote.reward.port.in.usecase.gifticon.SyncGifticonCouponStatusUseCase;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
@DisplayName("GifticonCouponSyncScheduler 테스트")
class GifticonCouponSyncSchedulerTest {

    @Mock
    private SyncGifticonCouponStatusUseCase syncGifticonCouponStatusUseCase;

    @InjectMocks
    private GifticonCouponSyncScheduler scheduler;

    @Test
    @DisplayName("정상 실행 시 syncCouponStatuses UseCase를 호출한다")
    void shouldCallSyncCouponStatuses() {
        // when
        scheduler.syncGifticonCouponStatuses();

        // then
        verify(syncGifticonCouponStatusUseCase).syncCouponStatuses();
    }

    @Test
    @DisplayName("UseCase 호출 중 예외가 발생해도 스케줄러는 예외를 전파하지 않는다")
    void shouldSwallowExceptionWhenUseCaseThrows() {
        // given
        doThrow(new RuntimeException("vendor down")).when(syncGifticonCouponStatusUseCase).syncCouponStatuses();

        // when & then
        assertThatCode(() -> scheduler.syncGifticonCouponStatuses()).doesNotThrowAnyException();
    }
}
