package com.personal.marketnote.product.adapter.in.scheduler;

import com.personal.marketnote.product.port.in.usecase.popularity.UpdatePopularityUseCase;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PopularitySchedulerTest {
    @InjectMocks
    private PopularityScheduler popularityScheduler;

    @Mock
    private UpdatePopularityUseCase updatePopularityUseCase;

    @Test
    @DisplayName("주간 인기도 갱신 스케줄러 실행 시 UseCase가 정확히 한 번 호출된다")
    void updateWeeklyPopularity_callsUseCaseOnce() {
        // when
        popularityScheduler.updateWeeklyPopularity();

        // then
        verify(updatePopularityUseCase, times(1)).updateWeeklyPopularity();
        verifyNoMoreInteractions(updatePopularityUseCase);
    }

    @Test
    @DisplayName("주간 인기도 갱신 중 예외 발생 시 예외가 전파되지 않는다")
    void updateWeeklyPopularity_unexpectedException_doesNotPropagate() {
        // given
        doThrow(new RuntimeException("DB 연결 실패"))
                .when(updatePopularityUseCase).updateWeeklyPopularity();

        // when & then
        popularityScheduler.updateWeeklyPopularity();

        verify(updatePopularityUseCase).updateWeeklyPopularity();
    }
}
