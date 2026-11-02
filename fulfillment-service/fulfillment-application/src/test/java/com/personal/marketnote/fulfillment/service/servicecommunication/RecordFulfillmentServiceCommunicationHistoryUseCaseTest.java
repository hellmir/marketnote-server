package com.personal.marketnote.fulfillment.service.servicecommunication;

import com.personal.marketnote.fulfillment.domain.servicecommunication.FulfillmentServiceCommunicationHistory;
import com.personal.marketnote.fulfillment.domain.servicecommunication.FulfillmentServiceCommunicationSenderType;
import com.personal.marketnote.fulfillment.domain.servicecommunication.FulfillmentServiceCommunicationTargetType;
import com.personal.marketnote.fulfillment.domain.servicecommunication.FulfillmentServiceCommunicationType;
import com.personal.marketnote.fulfillment.port.in.command.servicecommunication.FulfillmentServiceCommunicationHistoryCommand;
import com.personal.marketnote.fulfillment.port.out.servicecommunication.SaveFulfillmentServiceCommunicationHistoryPort;
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
@DisplayName("RecordFulfillmentServiceCommunicationHistoryService 테스트")
class RecordFulfillmentServiceCommunicationHistoryUseCaseTest {
    @InjectMocks
    private RecordFulfillmentServiceCommunicationHistoryService recordFulfillmentServiceCommunicationHistoryService;

    @Mock
    private SaveFulfillmentServiceCommunicationHistoryPort saveServiceCommunicationHistoryPort;

    @Test
    @DisplayName("서비스 간 통신 기록 커맨드를 전달하면 포트를 통해 저장된 기록을 반환한다")
    void shouldReturnSavedCommunicationHistoryFromPort() {
        // given
        FulfillmentServiceCommunicationHistoryCommand command = FulfillmentServiceCommunicationHistoryCommand.builder()
                .targetType(FulfillmentServiceCommunicationTargetType.GENERAL)
                .targetId("TARGET-001")
                .communicationType(FulfillmentServiceCommunicationType.REQUEST)
                .sender(FulfillmentServiceCommunicationSenderType.COMMERCE)
                .exception(null)
                .payload("{\"key\":\"value\"}")
                .payloadJson(null)
                .build();
        when(saveServiceCommunicationHistoryPort.save(any(FulfillmentServiceCommunicationHistory.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        // when
        FulfillmentServiceCommunicationHistory result = recordFulfillmentServiceCommunicationHistoryService.record(command);

        // then
        assertThat(result).isNotNull();
        assertThat(result.getTargetType()).isEqualTo(FulfillmentServiceCommunicationTargetType.GENERAL);
        assertThat(result.getCommunicationType()).isEqualTo(FulfillmentServiceCommunicationType.REQUEST);
        assertThat(result.getSender()).isEqualTo(FulfillmentServiceCommunicationSenderType.COMMERCE);
        verify(saveServiceCommunicationHistoryPort).save(any(FulfillmentServiceCommunicationHistory.class));
    }
}
