package com.personal.marketnote.commerce.adapter.in.scheduler;

import com.personal.marketnote.commerce.configuration.AutoConfirmSchedulerProperties;
import com.personal.marketnote.commerce.port.in.usecase.order.AutoConfirmDeliveredOrdersUseCase;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("AutoConfirmDeliveredOrdersScheduler 단위 테스트")
class AutoConfirmDeliveredOrdersSchedulerTest {

    @InjectMocks
    private AutoConfirmDeliveredOrdersScheduler scheduler;

    @Mock
    private AutoConfirmDeliveredOrdersUseCase autoConfirmDeliveredOrdersUseCase;

    @Mock
    private AutoConfirmSchedulerProperties properties;

    @Test
    @DisplayName("스케줄러 실행 시 properties의 autoConfirmDays를 UseCase에 위임한다")
    void shouldDelegateAutoConfirmDaysToUseCase() {
        // given
        when(properties.getAutoConfirmDays()).thenReturn(7L);

        // when
        scheduler.autoConfirmDeliveredOrders();

        // then
        verify(autoConfirmDeliveredOrdersUseCase).autoConfirmDeliveredOrders(7L);
    }

    @Test
    @DisplayName("UseCase에서 예외가 발생해도 스케줄러는 예외를 삼킨다")
    void shouldSwallowUseCaseException() {
        // given
        when(properties.getAutoConfirmDays()).thenReturn(7L);
        doThrow(new RuntimeException("usecase failed"))
                .when(autoConfirmDeliveredOrdersUseCase).autoConfirmDeliveredOrders(7L);

        // when & then
        assertThatCode(() -> scheduler.autoConfirmDeliveredOrders())
                .doesNotThrowAnyException();
    }
}
