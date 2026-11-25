package com.personal.marketnote.commerce.adapter.in.scheduler;

import com.personal.marketnote.commerce.port.in.usecase.returntracker.PollReturnInspectionUseCase;
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
@DisplayName("ReturnInspectionPollingScheduler 단위 테스트")
class ReturnInspectionPollingSchedulerTest {

    @InjectMocks
    private ReturnInspectionPollingScheduler scheduler;

    @Mock
    private PollReturnInspectionUseCase pollReturnInspectionUseCase;

    @Test
    @DisplayName("스케줄러 실행 시 UseCase의 pollPendingInspections를 위임 호출한다")
    void shouldDelegateToUseCase() {
        scheduler.pollReturnInspections();

        verify(pollReturnInspectionUseCase).pollPendingInspections();
    }

    @Test
    @DisplayName("UseCase에서 예외가 발생해도 스케줄러는 예외를 삼킨다")
    void shouldSwallowUseCaseException() {
        doThrow(new RuntimeException("usecase failed"))
                .when(pollReturnInspectionUseCase).pollPendingInspections();

        assertThatCode(() -> scheduler.pollReturnInspections())
                .doesNotThrowAnyException();
        verify(pollReturnInspectionUseCase).pollPendingInspections();
    }
}
