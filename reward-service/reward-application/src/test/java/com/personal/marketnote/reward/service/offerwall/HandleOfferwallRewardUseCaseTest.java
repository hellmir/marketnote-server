package com.personal.marketnote.reward.service.offerwall;

import com.personal.marketnote.reward.domain.offerwall.OfferwallMapper;
import com.personal.marketnote.reward.domain.offerwall.OfferwallMapperSnapshotState;
import com.personal.marketnote.reward.domain.offerwall.OfferwallType;
import com.personal.marketnote.reward.domain.offerwall.UserDeviceType;
import com.personal.marketnote.reward.port.in.command.offerwall.RegisterOfferwallRewardCommand;
import com.personal.marketnote.reward.port.in.usecase.offerwall.GetOfferwallMapperUseCase;
import com.personal.marketnote.reward.port.in.usecase.offerwall.RegisterOfferwallRewardUseCase;
import com.personal.marketnote.reward.port.out.offerwall.SaveOfferwallMapperPort;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("HandleOfferwallRewardUseCase 테스트")
class HandleOfferwallRewardUseCaseTest {

    @InjectMocks
    private HandleOfferwallRewardService handleOfferwallRewardService;

    @Mock
    private RegisterOfferwallRewardUseCase registerOfferwallRewardUseCase;

    @Mock
    private GetOfferwallMapperUseCase getOfferwallMapperUseCase;

    @Mock
    private SaveOfferwallMapperPort saveOfferwallMapperPort;

    @Nested
    @DisplayName("정상 처리")
    class SuccessTest {

        @Test
        @DisplayName("오퍼월 리워드 등록이 성공하면 등록된 ID를 반환한다")
        void shouldReturnIdWhenRegistrationSucceeds() {
            // given
            RegisterOfferwallRewardCommand command = buildCommand();
            when(registerOfferwallRewardUseCase.register(command)).thenReturn(1L);

            // when
            Long result = handleOfferwallRewardService.handle(command);

            // then
            assertThat(result).isEqualTo(1L);
            verify(registerOfferwallRewardUseCase).register(command);
            verifyNoInteractions(getOfferwallMapperUseCase, saveOfferwallMapperPort);
        }
    }

    @Nested
    @DisplayName("실패 처리")
    class FailureTest {

        @Test
        @DisplayName("오퍼월 리워드 등록 실패 시 이전 실패 매퍼가 있으면 실패 횟수를 이어서 기록하고 예외를 다시 던진다")
        void shouldSaveFailedMapperWithPreviousCountAndRethrow() {
            // given
            RegisterOfferwallRewardCommand command = buildCommand();
            RuntimeException exception = new RuntimeException("등록 실패");
            OfferwallMapper previousFailed = OfferwallMapper.from(
                    OfferwallMapperSnapshotState.builder()
                            .id(10L)
                            .offerwallType(OfferwallType.ADPOPCORN)
                            .rewardKey("reward-key")
                            .userKey("user-key")
                            .isSuccess(false)
                            .failureCount((short) 3)
                            .build()
            );

            when(registerOfferwallRewardUseCase.register(command)).thenThrow(exception);
            when(getOfferwallMapperUseCase.findTopFailedOfferwallMapper(
                    command.offerwallType(), command.rewardKey()
            )).thenReturn(Optional.of(previousFailed));

            // when & then
            assertThatThrownBy(() -> handleOfferwallRewardService.handle(command))
                    .isInstanceOf(RuntimeException.class);

            verify(saveOfferwallMapperPort).save(any(OfferwallMapper.class));
        }

        @Test
        @DisplayName("오퍼월 리워드 등록 실패 시 이전 실패 매퍼가 없으면 실패 횟수 0으로 기록하고 예외를 다시 던진다")
        void shouldSaveFailedMapperWithZeroCountAndRethrow() {
            // given
            RegisterOfferwallRewardCommand command = buildCommand();
            RuntimeException exception = new RuntimeException("등록 실패");

            when(registerOfferwallRewardUseCase.register(command)).thenThrow(exception);
            when(getOfferwallMapperUseCase.findTopFailedOfferwallMapper(
                    command.offerwallType(), command.rewardKey()
            )).thenReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> handleOfferwallRewardService.handle(command))
                    .isInstanceOf(RuntimeException.class);

            verify(saveOfferwallMapperPort).save(any(OfferwallMapper.class));
        }
    }

    private RegisterOfferwallRewardCommand buildCommand() {
        return RegisterOfferwallRewardCommand.builder()
                .offerwallType(OfferwallType.ADPOPCORN)
                .rewardKey("reward-key")
                .userKey("user-key")
                .userDeviceType(UserDeviceType.ANDROID)
                .campaignKey("campaign-1")
                .quantity(100L)
                .build();
    }
}
