package com.personal.marketnote.reward.service.point;

import com.personal.marketnote.reward.domain.point.PointAmount;
import com.personal.marketnote.reward.domain.point.UserPoint;
import com.personal.marketnote.reward.domain.point.UserPointChangeType;
import com.personal.marketnote.reward.domain.point.UserPointHistory;
import com.personal.marketnote.reward.domain.point.UserPointSnapshotState;
import com.personal.marketnote.reward.domain.point.UserPointSourceType;
import com.personal.marketnote.reward.port.in.command.point.ModifyPendingPointCommand;
import com.personal.marketnote.reward.port.in.usecase.point.GetUserPointUseCase;
import com.personal.marketnote.reward.port.in.usecase.point.ModifyPendingPointUseCase;
import com.personal.marketnote.reward.port.out.point.SaveUserPointHistoryPort;
import com.personal.marketnote.reward.port.out.point.UpdateUserPointPort;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.retry.annotation.EnableRetry;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@SpringJUnitConfig
@Import(ModifyPendingPointServiceRetryTest.TestConfig.class)
class ModifyPendingPointServiceRetryTest {

    private static final Long USER_ID = 1L;

    @EnableRetry
    @TestConfiguration
    static class TestConfig {

        @Bean
        GetUserPointUseCase getUserPointUseCase() {
            return mock(GetUserPointUseCase.class);
        }

        @Bean
        UpdateUserPointPort updateUserPointPort() {
            return mock(UpdateUserPointPort.class);
        }

        @Bean
        SaveUserPointHistoryPort saveUserPointHistoryPort() {
            return mock(SaveUserPointHistoryPort.class);
        }

        @Bean
        ModifyPendingPointUseCase modifyPendingPointService(
                GetUserPointUseCase getUserPointUseCase,
                UpdateUserPointPort updateUserPointPort,
                SaveUserPointHistoryPort saveUserPointHistoryPort
        ) {
            return new ModifyPendingPointService(
                    getUserPointUseCase, updateUserPointPort, saveUserPointHistoryPort
            );
        }
    }

    @Autowired
    private ModifyPendingPointUseCase modifyPendingPointService;

    @Autowired
    private GetUserPointUseCase getUserPointUseCase;

    @Autowired
    private UpdateUserPointPort updateUserPointPort;

    @Autowired
    private SaveUserPointHistoryPort saveUserPointHistoryPort;

    @AfterEach
    void resetMocks() {
        reset(getUserPointUseCase, updateUserPointPort, saveUserPointHistoryPort);
    }

    private UserPoint createUserPoint() {
        return UserPoint.from(UserPointSnapshotState.builder()
                .userId(USER_ID)
                .amount(PointAmount.of(1000L))
                .addExpectedAmount(PointAmount.zero())
                .expireExpectedAmount(PointAmount.zero())
                .createdAt(LocalDateTime.of(2026, 3, 5, 10, 0))
                .modifiedAt(LocalDateTime.of(2026, 3, 5, 10, 0))
                .build()
        );
    }

    private ModifyPendingPointCommand createAccrualCommand() {
        return ModifyPendingPointCommand.builder()
                .userId(USER_ID)
                .changeType(UserPointChangeType.ACCRUAL)
                .amount(500L)
                .sourceType(UserPointSourceType.ORDER)
                .sourceId(100L)
                .reason("주문 결제 적립 예정")
                .build();
    }

    @Test
    @DisplayName("적립 예정 변경 시 낙관적 잠금 충돌 2회 후 3번째 시도에 성공하면 최종 커밋된다")
    void shouldRetryOnOptimisticLockAndSucceedOnThirdAttempt() {
        // given
        ModifyPendingPointCommand command = createAccrualCommand();
        when(getUserPointUseCase.getUserPoint(USER_ID)).thenReturn(createUserPoint());
        when(updateUserPointPort.update(any(UserPoint.class)))
                .thenThrow(new ObjectOptimisticLockingFailureException(UserPoint.class, USER_ID))
                .thenThrow(new ObjectOptimisticLockingFailureException(UserPoint.class, USER_ID))
                .thenAnswer(invocation -> invocation.getArgument(0));
        when(saveUserPointHistoryPort.save(any(UserPointHistory.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        // when
        modifyPendingPointService.modifyPending(command);

        // then
        verify(updateUserPointPort, times(3)).update(any(UserPoint.class));
    }

    @Test
    @DisplayName("적립 예정 변경 시 낙관적 잠금 충돌이 3회 연속 발생하면 예외가 전파된다")
    void shouldThrowWhenOptimisticLockFailsAllRetries() {
        // given
        ModifyPendingPointCommand command = createAccrualCommand();
        when(getUserPointUseCase.getUserPoint(USER_ID)).thenReturn(createUserPoint());
        when(updateUserPointPort.update(any(UserPoint.class)))
                .thenThrow(new ObjectOptimisticLockingFailureException(UserPoint.class, USER_ID));

        // expect
        assertThatThrownBy(() -> modifyPendingPointService.modifyPending(command))
                .isInstanceOf(ObjectOptimisticLockingFailureException.class);

        verify(updateUserPointPort, times(3)).update(any(UserPoint.class));
    }

    @Test
    @DisplayName("적립 예정 변경 시 낙관적 잠금 이외의 예외는 재시도하지 않는다")
    void shouldNotRetryOnOtherExceptions() {
        // given
        ModifyPendingPointCommand command = createAccrualCommand();
        when(getUserPointUseCase.getUserPoint(USER_ID)).thenReturn(createUserPoint());
        when(updateUserPointPort.update(any(UserPoint.class)))
                .thenThrow(new IllegalStateException("other error"));

        // expect
        assertThatThrownBy(() -> modifyPendingPointService.modifyPending(command))
                .isInstanceOf(IllegalStateException.class);

        verify(updateUserPointPort, times(1)).update(any(UserPoint.class));
    }

    @Test
    @DisplayName("ModifyPendingPointService는 AOP 프록시로 생성되어 @Retryable이 실제로 적용된다")
    void shouldBeAopProxy() {
        assertThat(org.springframework.aop.support.AopUtils.isAopProxy(modifyPendingPointService))
                .as("@Retryable 적용을 위해 AOP 프록시여야 한다").isTrue();
    }
}
