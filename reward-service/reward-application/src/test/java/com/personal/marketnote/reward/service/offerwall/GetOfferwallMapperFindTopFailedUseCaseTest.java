package com.personal.marketnote.reward.service.offerwall;

import com.personal.marketnote.reward.domain.offerwall.OfferwallMapper;
import com.personal.marketnote.reward.domain.offerwall.OfferwallMapperSnapshotState;
import com.personal.marketnote.reward.domain.offerwall.OfferwallType;
import com.personal.marketnote.reward.port.out.offerwall.FindOfferwallMapperPort;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("GetOfferwallMapperUseCase.findTopFailedOfferwallMapper 테스트")
class GetOfferwallMapperFindTopFailedUseCaseTest {

    @InjectMocks
    private GetOfferwallMapperService getOfferwallMapperService;

    @Mock
    private FindOfferwallMapperPort findOfferwallMapperPort;

    @Test
    @DisplayName("실패한 오퍼월 매퍼가 존재하면 최근 실패 매퍼를 반환한다")
    void shouldReturnTopFailedMapper() {
        // given
        OfferwallType type = OfferwallType.ADPOPCORN;
        String rewardKey = "reward-123";
        OfferwallMapper failedMapper = OfferwallMapper.from(
                OfferwallMapperSnapshotState.builder()
                        .id(1L)
                        .offerwallType(type)
                        .rewardKey(rewardKey)
                        .userKey("user-key")
                        .isSuccess(false)
                        .failureCount((short) 2)
                        .build()
        );

        when(findOfferwallMapperPort.findTopFailedOfferwallMapper(type, rewardKey))
                .thenReturn(Optional.of(failedMapper));

        // when
        Optional<OfferwallMapper> result = getOfferwallMapperService.findTopFailedOfferwallMapper(type, rewardKey);

        // then
        assertThat(result).isPresent();
        assertThat(result.get().getFailureCount()).isEqualTo((short) 2);
        verify(findOfferwallMapperPort).findTopFailedOfferwallMapper(type, rewardKey);
    }

    @Test
    @DisplayName("실패한 오퍼월 매퍼가 없으면 빈 Optional을 반환한다")
    void shouldReturnEmptyWhenNoFailedMapper() {
        // given
        OfferwallType type = OfferwallType.ADPOPCORN;
        String rewardKey = "reward-456";

        when(findOfferwallMapperPort.findTopFailedOfferwallMapper(type, rewardKey))
                .thenReturn(Optional.empty());

        // when
        Optional<OfferwallMapper> result = getOfferwallMapperService.findTopFailedOfferwallMapper(type, rewardKey);

        // then
        assertThat(result).isEmpty();
        verify(findOfferwallMapperPort).findTopFailedOfferwallMapper(type, rewardKey);
    }
}
