package com.personal.marketnote.reward.service.offerwall;

import com.personal.marketnote.reward.domain.offerwall.OfferwallMapper;
import com.personal.marketnote.reward.domain.offerwall.OfferwallMapperSnapshotState;
import com.personal.marketnote.reward.domain.offerwall.OfferwallType;
import com.personal.marketnote.reward.domain.offerwall.UserDeviceType;
import com.personal.marketnote.reward.port.in.command.offerwall.RegisterOfferwallRewardCommand;
import com.personal.marketnote.reward.port.in.result.point.UpdateUserPointResult;
import com.personal.marketnote.reward.port.in.usecase.offerwall.GetOfferwallMapperUseCase;
import com.personal.marketnote.reward.port.in.usecase.offerwall.RegisterOfferwallRewardUseCase;
import com.personal.marketnote.reward.port.in.usecase.point.GetUserPointUseCase;
import com.personal.marketnote.reward.port.in.usecase.point.ModifyUserPointUseCase;
import com.personal.marketnote.reward.port.out.offerwall.SaveOfferwallMapperPort;
import com.personal.marketnote.reward.port.out.offerwall.ValidateOfferwallSignaturePort;
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

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@SpringJUnitConfig
@Import(RegisterOfferwallRewardServiceRetryTest.TestConfig.class)
class RegisterOfferwallRewardServiceRetryTest {

    @EnableRetry
    @TestConfiguration
    static class TestConfig {

        @Bean
        GetUserPointUseCase getUserPointUseCase() {
            return mock(GetUserPointUseCase.class);
        }

        @Bean
        ModifyUserPointUseCase modifyUserPointUseCase() {
            return mock(ModifyUserPointUseCase.class);
        }

        @Bean
        SaveOfferwallMapperPort saveOfferwallMapperPort() {
            return mock(SaveOfferwallMapperPort.class);
        }

        @Bean
        GetOfferwallMapperUseCase getOfferwallMapperUseCase() {
            return mock(GetOfferwallMapperUseCase.class);
        }

        @Bean
        ValidateOfferwallSignaturePort validateOfferwallSignaturePort() {
            return mock(ValidateOfferwallSignaturePort.class);
        }

        @Bean
        RegisterOfferwallRewardUseCase registerOfferwallRewardUseCase(
                GetUserPointUseCase getUserPointUseCase,
                ModifyUserPointUseCase modifyUserPointUseCase,
                SaveOfferwallMapperPort saveOfferwallMapperPort,
                GetOfferwallMapperUseCase getOfferwallMapperUseCase,
                ValidateOfferwallSignaturePort validateOfferwallSignaturePort
        ) {
            return new RegisterOfferwallRewardService(
                    getUserPointUseCase, modifyUserPointUseCase, saveOfferwallMapperPort,
                    getOfferwallMapperUseCase, validateOfferwallSignaturePort
            );
        }
    }

    @Autowired
    private RegisterOfferwallRewardUseCase registerOfferwallRewardUseCase;

    @Autowired
    private GetUserPointUseCase getUserPointUseCase;

    @Autowired
    private ModifyUserPointUseCase modifyUserPointUseCase;

    @Autowired
    private SaveOfferwallMapperPort saveOfferwallMapperPort;

    @Autowired
    private GetOfferwallMapperUseCase getOfferwallMapperUseCase;

    @Autowired
    private ValidateOfferwallSignaturePort validateOfferwallSignaturePort;

    @AfterEach
    void resetMocks() {
        reset(getUserPointUseCase, modifyUserPointUseCase, saveOfferwallMapperPort,
                getOfferwallMapperUseCase, validateOfferwallSignaturePort);
    }

    private RegisterOfferwallRewardCommand buildCommand() {
        return RegisterOfferwallRewardCommand.builder()
                .offerwallType(OfferwallType.ADPOPCORN)
                .userDeviceType(UserDeviceType.ANDROID)
                .userKey("user-key")
                .rewardKey("reward-key")
                .signedValue("signed-value")
                .quantity(100L)
                .campaignKey("campaign")
                .rewardUnit("UNIT")
                .build();
    }

    private OfferwallMapper savedMapper() {
        return OfferwallMapper.from(
                OfferwallMapperSnapshotState.builder()
                        .id(1L)
                        .offerwallType(OfferwallType.ADPOPCORN)
                        .rewardKey("reward-key")
                        .userKey("user-key")
                        .isSuccess(true)
                        .failureCount((short) 0)
                        .build()
        );
    }

    @Test
    @DisplayName("오퍼월 리워드 등록 시 ModifyUserPoint에서 낙관적 잠금 충돌 2회 후 성공하면 전체 use case가 재실행된다")
    void shouldRetryOuterUseCaseOnOptimisticLock() {
        // given
        RegisterOfferwallRewardCommand command = buildCommand();
        when(getUserPointUseCase.existsUserPoint("user-key")).thenReturn(true);
        when(getOfferwallMapperUseCase.existsSucceededOfferwallMapper(OfferwallType.ADPOPCORN, "reward-key"))
                .thenReturn(false);
        when(saveOfferwallMapperPort.save(any(OfferwallMapper.class))).thenReturn(savedMapper());
        when(modifyUserPointUseCase.modify(any()))
                .thenThrow(new ObjectOptimisticLockingFailureException(Object.class, 1L))
                .thenThrow(new ObjectOptimisticLockingFailureException(Object.class, 1L))
                .thenReturn(mock(UpdateUserPointResult.class));

        // when
        Long result = registerOfferwallRewardUseCase.register(command);

        // then
        assertThat(result).isEqualTo(1L);
        verify(modifyUserPointUseCase, times(3)).modify(any());
        verify(saveOfferwallMapperPort, times(3)).save(any(OfferwallMapper.class));
    }

    @Test
    @DisplayName("낙관적 잠금 충돌이 3회 연속 발생하면 예외가 전파된다")
    void shouldThrowWhenOptimisticLockFailsAllRetries() {
        // given
        RegisterOfferwallRewardCommand command = buildCommand();
        when(getUserPointUseCase.existsUserPoint("user-key")).thenReturn(true);
        when(getOfferwallMapperUseCase.existsSucceededOfferwallMapper(OfferwallType.ADPOPCORN, "reward-key"))
                .thenReturn(false);
        when(saveOfferwallMapperPort.save(any(OfferwallMapper.class))).thenReturn(savedMapper());
        when(modifyUserPointUseCase.modify(any()))
                .thenThrow(new ObjectOptimisticLockingFailureException(Object.class, 1L));

        // expect
        assertThatThrownBy(() -> registerOfferwallRewardUseCase.register(command))
                .isInstanceOf(ObjectOptimisticLockingFailureException.class);

        verify(modifyUserPointUseCase, times(3)).modify(any());
    }

    @Test
    @DisplayName("RegisterOfferwallRewardUseCase는 AOP 프록시로 생성되어 @Retryable이 실제로 적용된다")
    void shouldBeAopProxy() {
        assertThat(org.springframework.aop.support.AopUtils.isAopProxy(registerOfferwallRewardUseCase))
                .as("@Retryable 적용을 위해 AOP 프록시여야 한다").isTrue();
    }
}
