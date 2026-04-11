package com.personal.marketnote.reward.service.vendorcommunication;

import com.personal.marketnote.reward.domain.vendorcommunication.*;
import com.personal.marketnote.reward.port.in.command.vendorcommunication.RewardVendorCommunicationHistoryCommand;
import com.personal.marketnote.reward.port.out.vendorcommunication.RewardSaveVendorCommunicationHistoryPort;
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
@DisplayName("RewardRecordVendorCommunicationHistoryUseCase 테스트")
class RewardRecordVendorCommunicationHistoryUseCaseTest {

    @InjectMocks
    private RewardRecordVendorCommunicationHistoryService rewardRecordVendorCommunicationHistoryService;

    @Mock
    private RewardSaveVendorCommunicationHistoryPort saveVendorCommunicationHistoryPort;

    @Test
    @DisplayName("벤더 통신 기록 저장 시 Command를 CreateState로 매핑하여 저장하고 결과를 반환한다")
    void shouldRecordVendorCommunicationHistorySuccessfully() {
        // given
        RewardVendorCommunicationHistoryCommand command = RewardVendorCommunicationHistoryCommand.builder()
                .targetType(RewardVendorCommunicationTargetType.OFFERWALL)
                .targetId("offerwall-123")
                .vendorName(RewardVendorName.ADPOPCORN)
                .communicationType(RewardVendorCommunicationType.REQUEST)
                .sender(RewardVendorCommunicationSenderType.SERVER)
                .payload("{\"data\": \"value\"}")
                .build();

        when(saveVendorCommunicationHistoryPort.save(any(RewardVendorCommunicationHistory.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        // when
        RewardVendorCommunicationHistory result = rewardRecordVendorCommunicationHistoryService.record(command);

        // then
        assertThat(result.getTargetType()).isEqualTo(RewardVendorCommunicationTargetType.OFFERWALL);
        assertThat(result.getTargetId()).isEqualTo("offerwall-123");
        assertThat(result.getVendorName()).isEqualTo(RewardVendorName.ADPOPCORN);
        assertThat(result.getCommunicationType()).isEqualTo(RewardVendorCommunicationType.REQUEST);
        verify(saveVendorCommunicationHistoryPort).save(any(RewardVendorCommunicationHistory.class));
    }
}
