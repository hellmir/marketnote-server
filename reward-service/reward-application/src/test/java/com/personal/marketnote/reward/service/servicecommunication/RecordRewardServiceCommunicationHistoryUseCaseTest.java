package com.personal.marketnote.reward.service.servicecommunication;

import com.personal.marketnote.reward.domain.servicecommunication.RewardServiceCommunicationHistory;
import com.personal.marketnote.reward.domain.servicecommunication.RewardServiceCommunicationSenderType;
import com.personal.marketnote.reward.domain.servicecommunication.RewardServiceCommunicationTargetType;
import com.personal.marketnote.reward.domain.servicecommunication.RewardServiceCommunicationType;
import com.personal.marketnote.reward.port.in.command.servicecommunication.RewardServiceCommunicationHistoryCommand;
import com.personal.marketnote.reward.port.out.servicecommunication.SaveRewardServiceCommunicationHistoryPort;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("RecordRewardServiceCommunicationHistoryUseCase 테스트")
class RecordRewardServiceCommunicationHistoryUseCaseTest {

    @InjectMocks
    private RecordRewardServiceCommunicationHistoryService recordService;

    @Mock
    private SaveRewardServiceCommunicationHistoryPort saveServiceCommunicationHistoryPort;

    @Test
    @DisplayName("통신 기록 저장 시 Command를 CreateState로 매핑하여 저장하고 결과를 반환한다")
    void shouldRecordCommunicationHistorySuccessfully() {
        // given
        RewardServiceCommunicationHistoryCommand command = RewardServiceCommunicationHistoryCommand.builder()
                .targetType(RewardServiceCommunicationTargetType.GENERAL)
                .targetId("target-123")
                .communicationType(RewardServiceCommunicationType.REQUEST)
                .sender(RewardServiceCommunicationSenderType.COMMERCE)
                .payload("{\"key\": \"value\"}")
                .build();

        when(saveServiceCommunicationHistoryPort.save(any(RewardServiceCommunicationHistory.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        // when
        RewardServiceCommunicationHistory result = recordService.record(command);

        // then
        assertThat(result.getTargetType()).isEqualTo(RewardServiceCommunicationTargetType.GENERAL);
        assertThat(result.getTargetId()).isEqualTo("target-123");
        assertThat(result.getCommunicationType()).isEqualTo(RewardServiceCommunicationType.REQUEST);
        assertThat(result.getSender()).isEqualTo(RewardServiceCommunicationSenderType.COMMERCE);
        verify(saveServiceCommunicationHistoryPort).save(any(RewardServiceCommunicationHistory.class));
    }
}
