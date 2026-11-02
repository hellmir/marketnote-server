package com.personal.marketnote.community.service.servicecommunication;

import com.personal.marketnote.community.domain.servicecommunication.CommunityServiceCommunicationHistory;
import com.personal.marketnote.community.domain.servicecommunication.CommunityServiceCommunicationSenderType;
import com.personal.marketnote.community.domain.servicecommunication.CommunityServiceCommunicationTargetType;
import com.personal.marketnote.community.domain.servicecommunication.CommunityServiceCommunicationType;
import com.personal.marketnote.community.port.in.command.servicecommunication.CommunityServiceCommunicationHistoryCommand;
import com.personal.marketnote.community.port.out.servicecommunication.SaveCommunityServiceCommunicationHistoryPort;
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
@DisplayName("RecordCommunityServiceCommunicationHistoryUseCase 테스트")
class RecordCommunityServiceCommunicationHistoryUseCaseTest {

    @InjectMocks
    private RecordCommunityServiceCommunicationHistoryService recordCommunityServiceCommunicationHistoryService;

    @Mock
    private SaveCommunityServiceCommunicationHistoryPort saveServiceCommunicationHistoryPort;

    @Test
    @DisplayName("통신 기록 저장 시 Command를 CreateState로 매핑하여 저장하고 결과를 반환한다")
    void shouldRecordCommunicationHistorySuccessfully() {
        // given
        CommunityServiceCommunicationHistoryCommand command = CommunityServiceCommunicationHistoryCommand.builder()
                .targetType(CommunityServiceCommunicationTargetType.PRODUCT_INFO)
                .targetId("product-123")
                .communicationType(CommunityServiceCommunicationType.REQUEST)
                .sender(CommunityServiceCommunicationSenderType.COMMERCE)
                .payload("{\"productId\": 123}")
                .build();

        when(saveServiceCommunicationHistoryPort.save(any(CommunityServiceCommunicationHistory.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        // when
        CommunityServiceCommunicationHistory result = recordCommunityServiceCommunicationHistoryService.record(command);

        // then
        assertThat(result.getTargetType()).isEqualTo(CommunityServiceCommunicationTargetType.PRODUCT_INFO);
        assertThat(result.getTargetId()).isEqualTo("product-123");
        assertThat(result.getCommunicationType()).isEqualTo(CommunityServiceCommunicationType.REQUEST);
        assertThat(result.getSender()).isEqualTo(CommunityServiceCommunicationSenderType.COMMERCE);
        assertThat(result.getPayload()).isEqualTo("{\"productId\": 123}");
        verify(saveServiceCommunicationHistoryPort).save(any(CommunityServiceCommunicationHistory.class));
    }
}
