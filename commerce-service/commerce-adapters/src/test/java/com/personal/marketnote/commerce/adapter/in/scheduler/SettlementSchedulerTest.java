package com.personal.marketnote.commerce.adapter.in.scheduler;

import com.personal.marketnote.commerce.configuration.SettlementSchedulerProperties;
import com.personal.marketnote.commerce.exception.NoUnsettledAllocationException;
import com.personal.marketnote.commerce.port.in.command.settlement.ExecuteSettlementCommand;
import com.personal.marketnote.commerce.port.in.usecase.settlement.ExecuteSettlementUseCase;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SettlementSchedulerTest {
    @InjectMocks
    private SettlementScheduler settlementScheduler;

    @Mock
    private ExecuteSettlementUseCase executeSettlementUseCase;

    @Mock
    private SettlementSchedulerProperties properties;

    @Mock(name = "commerceClock")
    private Clock commerceClock;

    private void setUpClock(String instant) {
        Clock fixedClock = Clock.fixed(Instant.parse(instant), ZoneId.of("Asia/Seoul"));
        when(commerceClock.instant()).thenReturn(fixedClock.instant());
        when(commerceClock.getZone()).thenReturn(fixedClock.getZone());
    }

    private void setUpProperties(int pgFeeRate, int platformFeeRate) {
        when(properties.getPgFeeRate()).thenReturn(pgFeeRate);
        when(properties.getPlatformFeeRate()).thenReturn(platformFeeRate);
    }

    @Test
    @DisplayName("월별 자동 정산 실행 시 전월의 연/월로 정산 커맨드가 생성된다")
    void executeMonthlySettlement_createsCommandWithPreviousMonth() {
        // given
        setUpClock("2026-03-01T02:00:00Z");
        setUpProperties(300, 500);

        // when
        settlementScheduler.executeMonthlySettlement();

        // then
        ArgumentCaptor<ExecuteSettlementCommand> captor = ArgumentCaptor.forClass(ExecuteSettlementCommand.class);
        verify(executeSettlementUseCase).executeSettlement(captor.capture());

        ExecuteSettlementCommand command = captor.getValue();
        assertThat(command.year()).isEqualTo(2026);
        assertThat(command.month()).isEqualTo(2);
        assertThat(command.pgFeeRate()).isEqualTo(300);
        assertThat(command.platformFeeRate()).isEqualTo(500);
    }

    @Test
    @DisplayName("1월에 실행하면 전년 12월 정산 커맨드가 생성된다")
    void executeMonthlySettlement_januaryCreatesDecemberOfPreviousYear() {
        // given
        setUpClock("2026-01-01T02:00:00Z");
        setUpProperties(300, 500);

        // when
        settlementScheduler.executeMonthlySettlement();

        // then
        ArgumentCaptor<ExecuteSettlementCommand> captor = ArgumentCaptor.forClass(ExecuteSettlementCommand.class);
        verify(executeSettlementUseCase).executeSettlement(captor.capture());

        ExecuteSettlementCommand command = captor.getValue();
        assertThat(command.year()).isEqualTo(2025);
        assertThat(command.month()).isEqualTo(12);
    }

    @Test
    @DisplayName("Properties에 설정된 수수료율이 커맨드에 정확히 전달된다")
    void executeMonthlySettlement_feeRatesFromProperties() {
        // given
        setUpClock("2026-03-01T02:00:00Z");
        setUpProperties(250, 700);

        // when
        settlementScheduler.executeMonthlySettlement();

        // then
        ArgumentCaptor<ExecuteSettlementCommand> captor = ArgumentCaptor.forClass(ExecuteSettlementCommand.class);
        verify(executeSettlementUseCase).executeSettlement(captor.capture());

        ExecuteSettlementCommand command = captor.getValue();
        assertThat(command.pgFeeRate()).isEqualTo(250);
        assertThat(command.platformFeeRate()).isEqualTo(700);
    }

    @Test
    @DisplayName("미정산 배분이 없으면 예외가 전파되지 않는다")
    void executeMonthlySettlement_noUnsettledAllocation_doesNotPropagate() {
        // given
        setUpClock("2026-03-01T02:00:00Z");
        setUpProperties(300, 500);
        doThrow(new NoUnsettledAllocationException())
                .when(executeSettlementUseCase).executeSettlement(any(ExecuteSettlementCommand.class));

        // when & then
        settlementScheduler.executeMonthlySettlement();

        verify(executeSettlementUseCase).executeSettlement(any(ExecuteSettlementCommand.class));
    }

    @Test
    @DisplayName("예상치 못한 예외 발생 시에도 예외가 전파되지 않는다")
    void executeMonthlySettlement_unexpectedException_doesNotPropagate() {
        // given
        setUpClock("2026-03-01T02:00:00Z");
        setUpProperties(300, 500);
        doThrow(new RuntimeException("DB 연결 실패"))
                .when(executeSettlementUseCase).executeSettlement(any(ExecuteSettlementCommand.class));

        // when & then
        settlementScheduler.executeMonthlySettlement();

        verify(executeSettlementUseCase).executeSettlement(any(ExecuteSettlementCommand.class));
    }

    @Test
    @DisplayName("정산 실행이 성공하면 UseCase가 정확히 한 번 호출된다")
    void executeMonthlySettlement_success_callsUseCaseOnce() {
        // given
        setUpClock("2026-06-01T02:00:00Z");
        setUpProperties(300, 500);

        // when
        settlementScheduler.executeMonthlySettlement();

        // then
        verify(executeSettlementUseCase, times(1)).executeSettlement(any(ExecuteSettlementCommand.class));
        verifyNoMoreInteractions(executeSettlementUseCase);
    }
}
