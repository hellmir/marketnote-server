package com.personal.marketnote.reward.service.point;

import com.personal.marketnote.reward.domain.point.PointAmount;
import com.personal.marketnote.reward.domain.point.UserPoint;
import com.personal.marketnote.reward.domain.point.UserPointChangeType;
import com.personal.marketnote.reward.domain.point.UserPointHistory;
import com.personal.marketnote.reward.domain.point.UserPointSnapshotState;
import com.personal.marketnote.reward.domain.point.UserPointSourceType;
import com.personal.marketnote.reward.port.in.command.point.ModifyUserPointCommand;
import com.personal.marketnote.reward.port.in.usecase.point.GetUserPointUseCase;
import com.personal.marketnote.reward.port.in.usecase.point.ModifyUserPointUseCase;
import com.personal.marketnote.reward.port.out.point.FindUserPointPort;
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
@Import(ModifyUserPointServiceRetryTest.TestConfig.class)
class ModifyUserPointServiceRetryTest {

    private static final Long USER_ID = 1L;
    private static final String USER_KEY = "test-user-key";

    @EnableRetry
    @TestConfiguration
    static class TestConfig {

        @Bean
        GetUserPointUseCase getUserPointUseCase() {
            return mock(GetUserPointUseCase.class);
        }

        @Bean
        FindUserPointPort findUserPointPort() {
            return mock(FindUserPointPort.class);
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
        ModifyUserPointUseCase modifyUserPointService(
                GetUserPointUseCase getUserPointUseCase,
                FindUserPointPort findUserPointPort,
                UpdateUserPointPort updateUserPointPort,
                SaveUserPointHistoryPort saveUserPointHistoryPort
        ) {
            return new ModifyUserPointService(
                    getUserPointUseCase, findUserPointPort, updateUserPointPort, saveUserPointHistoryPort
            );
        }
    }

    @Autowired
    private ModifyUserPointUseCase modifyUserPointService;

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
                .userKey(USER_KEY)
                .amount(PointAmount.of(1000L))
                .addExpectedAmount(PointAmount.zero())
                .expireExpectedAmount(PointAmount.zero())
                .createdAt(LocalDateTime.of(2026, 3, 5, 10, 0))
                .modifiedAt(LocalDateTime.of(2026, 3, 5, 10, 0))
                .build()
        );
    }

    private ModifyUserPointCommand createAccrualCommand() {
        return ModifyUserPointCommand.builder()
                .userId(USER_ID)
                .changeType(UserPointChangeType.ACCRUAL)
                .amount(500L)
                .sourceType(UserPointSourceType.ORDER)
                .sourceId(100L)
                .reason("상품 구매 적립")
                .build();
    }

    @Test
    @DisplayName("적립 경로에서 낙관적 잠금 충돌 2회 후 3번째 시도에 성공하면 최종 커밋된다")
    void shouldRetryOnOptimisticLockAndSucceedOnThirdAttempt() {
        // given
        ModifyUserPointCommand command = createAccrualCommand();
        when(getUserPointUseCase.getUserPoint(USER_ID)).thenReturn(createUserPoint());
        when(updateUserPointPort.update(any(UserPoint.class)))
                .thenThrow(new ObjectOptimisticLockingFailureException(UserPoint.class, USER_ID))
                .thenThrow(new ObjectOptimisticLockingFailureException(UserPoint.class, USER_ID))
                .thenAnswer(invocation -> invocation.getArgument(0));
        when(saveUserPointHistoryPort.save(any(UserPointHistory.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        // when
        modifyUserPointService.modify(command);

        // then
        verify(updateUserPointPort, times(3)).update(any(UserPoint.class));
    }

    @Test
    @DisplayName("적립 경로에서 낙관적 잠금 충돌이 3회 연속 발생하면 ObjectOptimisticLockingFailureException이 전파된다")
    void shouldThrowWhenOptimisticLockFailsAllRetries() {
        // given
        ModifyUserPointCommand command = createAccrualCommand();
        when(getUserPointUseCase.getUserPoint(USER_ID)).thenReturn(createUserPoint());
        when(updateUserPointPort.update(any(UserPoint.class)))
                .thenThrow(new ObjectOptimisticLockingFailureException(UserPoint.class, USER_ID));

        // expect
        assertThatThrownBy(() -> modifyUserPointService.modify(command))
                .isInstanceOf(ObjectOptimisticLockingFailureException.class);

        verify(updateUserPointPort, times(3)).update(any(UserPoint.class));
    }

    @Test
    @DisplayName("적립 경로에서 낙관적 잠금 이외의 예외는 재시도하지 않고 즉시 전파된다")
    void shouldNotRetryOnOtherExceptions() {
        // given
        ModifyUserPointCommand command = createAccrualCommand();
        when(getUserPointUseCase.getUserPoint(USER_ID)).thenReturn(createUserPoint());
        when(updateUserPointPort.update(any(UserPoint.class)))
                .thenThrow(new IllegalStateException("other error"));

        // expect
        assertThatThrownBy(() -> modifyUserPointService.modify(command))
                .isInstanceOf(IllegalStateException.class);

        verify(updateUserPointPort, times(1)).update(any(UserPoint.class));
    }

    @Test
    @DisplayName("ModifyUserPointService는 AOP 프록시로 생성되어 @Retryable이 실제로 적용된다")
    void shouldBeAopProxy() {
        assertThat(org.springframework.aop.support.AopUtils.isAopProxy(modifyUserPointService))
                .as("@Retryable 적용을 위해 AOP 프록시여야 한다").isTrue();
    }
}
