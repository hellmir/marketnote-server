package com.personal.marketnote.reward.service.offerwall;

import com.personal.marketnote.reward.domain.offerwall.OfferwallType;
import com.personal.marketnote.reward.port.out.offerwall.FindOfferwallMapperPort;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("GetOfferwallMapperUseCase.existsSucceededOfferwallMapper 테스트")
class GetOfferwallMapperExistsSucceededUseCaseTest {

    @InjectMocks
    private GetOfferwallMapperService getOfferwallMapperService;

    @Mock
    private FindOfferwallMapperPort findOfferwallMapperPort;

    @Test
    @DisplayName("성공한 오퍼월 매퍼가 존재하면 true를 반환한다")
    void shouldReturnTrueWhenSucceededMapperExists() {
        // given
        OfferwallType type = OfferwallType.ADPOPCORN;
        String rewardKey = "reward-123";

        when(findOfferwallMapperPort.existsByOfferwallTypeAndRewardKeyAndIsSuccess(type, rewardKey, true))
                .thenReturn(true);

        // when
        boolean result = getOfferwallMapperService.existsSucceededOfferwallMapper(type, rewardKey);

        // then
        assertThat(result).isTrue();
        verify(findOfferwallMapperPort).existsByOfferwallTypeAndRewardKeyAndIsSuccess(type, rewardKey, true);
    }

    @Test
    @DisplayName("성공한 오퍼월 매퍼가 존재하지 않으면 false를 반환한다")
    void shouldReturnFalseWhenNoSucceededMapper() {
        // given
        OfferwallType type = OfferwallType.ADPOPCORN;
        String rewardKey = "reward-456";

        when(findOfferwallMapperPort.existsByOfferwallTypeAndRewardKeyAndIsSuccess(type, rewardKey, true))
                .thenReturn(false);

        // when
        boolean result = getOfferwallMapperService.existsSucceededOfferwallMapper(type, rewardKey);

        // then
        assertThat(result).isFalse();
        verify(findOfferwallMapperPort).existsByOfferwallTypeAndRewardKeyAndIsSuccess(type, rewardKey, true);
    }
}
