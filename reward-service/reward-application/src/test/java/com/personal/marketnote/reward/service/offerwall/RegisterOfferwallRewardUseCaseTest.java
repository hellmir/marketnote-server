package com.personal.marketnote.reward.service.offerwall;

import com.personal.marketnote.common.exception.UserNotFoundException;
import com.personal.marketnote.reward.domain.offerwall.OfferwallMapper;
import com.personal.marketnote.reward.domain.attendance.RewardQuantity;
import com.personal.marketnote.reward.domain.offerwall.OfferwallMapperSnapshotState;
import com.personal.marketnote.reward.domain.attendance.RewardQuantity;
import com.personal.marketnote.reward.domain.offerwall.OfferwallType;
import com.personal.marketnote.reward.domain.attendance.RewardQuantity;
import com.personal.marketnote.reward.domain.offerwall.UserDeviceType;
import com.personal.marketnote.reward.domain.attendance.RewardQuantity;
import com.personal.marketnote.reward.exception.DuplicateOfferwallRewardException;
import com.personal.marketnote.reward.domain.attendance.RewardQuantity;
import com.personal.marketnote.reward.port.in.command.offerwall.RegisterOfferwallRewardCommand;
import com.personal.marketnote.reward.domain.attendance.RewardQuantity;
import com.personal.marketnote.reward.port.in.command.point.ModifyUserPointCommand;
import com.personal.marketnote.reward.domain.attendance.RewardQuantity;
import com.personal.marketnote.reward.port.in.usecase.offerwall.GetOfferwallMapperUseCase;
import com.personal.marketnote.reward.domain.attendance.RewardQuantity;
import com.personal.marketnote.reward.port.in.usecase.point.GetUserPointUseCase;
import com.personal.marketnote.reward.domain.attendance.RewardQuantity;
import com.personal.marketnote.reward.port.out.offerwall.SaveOfferwallMapperPort;
import com.personal.marketnote.reward.domain.attendance.RewardQuantity;
import com.personal.marketnote.reward.port.out.offerwall.ValidateOfferwallSignaturePort;
import com.personal.marketnote.reward.domain.attendance.RewardQuantity;
import com.personal.marketnote.reward.service.point.ModifyUserPointService;
import com.personal.marketnote.reward.domain.attendance.RewardQuantity;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("RegisterOfferwallRewardUseCase 테스트")
class RegisterOfferwallRewardUseCaseTest {

    @InjectMocks
    private RegisterOfferwallRewardService registerOfferwallRewardService;

    @Mock
    private GetUserPointUseCase getUserPointUseCase;

    @Mock
    private ModifyUserPointService modifyUserPointService;

    @Mock
    private SaveOfferwallMapperPort saveOfferwallMapperPort;

    @Mock
    private GetOfferwallMapperUseCase getOfferwallMapperUseCase;

    @Mock
    private ValidateOfferwallSignaturePort validateOfferwallSignaturePort;

    @Nested
    @DisplayName("등록 성공")
    class RegisterSuccessTest {

        @Test
        @DisplayName("유효한 오퍼월 리워드 등록 시 매퍼 저장 후 포인트 적립이 수행된다")
        void shouldRegisterOfferwallRewardAndAccruePoints() {
            // given
            RegisterOfferwallRewardCommand command = buildCommand();
            OfferwallMapper savedMapper = OfferwallMapper.from(
                    OfferwallMapperSnapshotState.builder()
                            .id(1L)
                            .offerwallType(OfferwallType.ADPOPCORN)
                            .rewardKey("reward-key")
                            .userKey("user-key")
                            .isSuccess(true)
                            .failureCount((short) 0)
                            .build()
            );

            when(getUserPointUseCase.existsUserPoint("user-key")).thenReturn(true);
            when(getOfferwallMapperUseCase.existsSucceededOfferwallMapper(OfferwallType.ADPOPCORN, "reward-key"))
                    .thenReturn(false);
            when(saveOfferwallMapperPort.save(any(OfferwallMapper.class))).thenReturn(savedMapper);

            // when
            Long result = registerOfferwallRewardService.register(command);

            // then
            assertThat(result).isEqualTo(1L);
            verify(validateOfferwallSignaturePort).validateSignature(
                    eq(OfferwallType.ADPOPCORN), eq(UserDeviceType.ANDROID), any(), any(), any(), any(), any(), any()
            );
            verify(saveOfferwallMapperPort).save(any(OfferwallMapper.class));
            verify(modifyUserPointService).modify(any(ModifyUserPointCommand.class));
        }
    }

    @Nested
    @DisplayName("등록 실패")
    class RegisterFailureTest {

        @Test
        @DisplayName("존재하지 않는 사용자이면 UserNotFoundException이 발생한다")
        void shouldThrowWhenUserNotFound() {
            // given
            RegisterOfferwallRewardCommand command = buildCommand();
            when(getUserPointUseCase.existsUserPoint("user-key")).thenReturn(false);

            // when & then
            assertThatThrownBy(() -> registerOfferwallRewardService.register(command))
                    .isInstanceOf(UserNotFoundException.class);

            verifyNoInteractions(saveOfferwallMapperPort, modifyUserPointService);
        }

        @Test
        @DisplayName("이미 성공한 리워드 키이면 DuplicateOfferwallRewardException이 발생한다")
        void shouldThrowWhenDuplicateRewardKey() {
            // given
            RegisterOfferwallRewardCommand command = buildCommand();
            when(getUserPointUseCase.existsUserPoint("user-key")).thenReturn(true);
            when(getOfferwallMapperUseCase.existsSucceededOfferwallMapper(OfferwallType.ADPOPCORN, "reward-key"))
                    .thenReturn(true);

            // when & then
            assertThatThrownBy(() -> registerOfferwallRewardService.register(command))
                    .isInstanceOf(DuplicateOfferwallRewardException.class);

            verifyNoInteractions(saveOfferwallMapperPort, modifyUserPointService);
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
                .rewardUnit("POINT")
                .build();
    }
}
