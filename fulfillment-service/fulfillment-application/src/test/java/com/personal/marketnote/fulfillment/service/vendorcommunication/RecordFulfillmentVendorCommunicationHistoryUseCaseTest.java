package com.personal.marketnote.fulfillment.service.vendorcommunication;

import com.personal.marketnote.fulfillment.domain.vendorcommunication.FulfillmentVendorCommunicationHistory;
import com.personal.marketnote.fulfillment.domain.vendorcommunication.FulfillmentVendorCommunicationSenderType;
import com.personal.marketnote.fulfillment.domain.vendorcommunication.FulfillmentVendorCommunicationTargetType;
import com.personal.marketnote.fulfillment.domain.vendorcommunication.FulfillmentVendorCommunicationType;
import com.personal.marketnote.fulfillment.domain.vendorcommunication.FulfillmentVendorName;
import com.personal.marketnote.fulfillment.port.in.command.vendorcommunication.FulfillmentVendorCommunicationHistoryCommand;
import com.personal.marketnote.fulfillment.port.out.vendorcommunication.SaveFulfillmentVendorCommunicationHistoryPort;
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
@DisplayName("풀필먼트 벤더 통신 기록 저장 테스트")
class RecordFulfillmentVendorCommunicationHistoryUseCaseTest {
    @InjectMocks
    private RecordFulfillmentVendorCommunicationHistoryService service;
    @Mock
    private SaveFulfillmentVendorCommunicationHistoryPort saveVendorCommunicationHistoryPort;

    @Test
    @DisplayName("Command를 Port에 위임하여 결과를 반환한다")
    void shouldDelegateToPort() {
        // given
        FulfillmentVendorCommunicationHistoryCommand command = FulfillmentVendorCommunicationHistoryCommand.builder()
                .targetType(FulfillmentVendorCommunicationTargetType.DELIVERY)
                .targetId("delivery-123")
                .vendorName(FulfillmentVendorName.FASSTO)
                .communicationType(FulfillmentVendorCommunicationType.REQUEST)
                .sender(FulfillmentVendorCommunicationSenderType.SERVER)
                .payload("{}")
                .build();
        when(saveVendorCommunicationHistoryPort.save(any(FulfillmentVendorCommunicationHistory.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));
        // when
        FulfillmentVendorCommunicationHistory result = service.record(command);
        // then
        assertThat(result).isNotNull();
        assertThat(result.getTargetType()).isEqualTo(FulfillmentVendorCommunicationTargetType.DELIVERY);
        assertThat(result.getTargetId()).isEqualTo("delivery-123");
        assertThat(result.getVendorName()).isEqualTo(FulfillmentVendorName.FASSTO);
        assertThat(result.getCommunicationType()).isEqualTo(FulfillmentVendorCommunicationType.REQUEST);
        assertThat(result.getSender()).isEqualTo(FulfillmentVendorCommunicationSenderType.SERVER);
        assertThat(result.getPayload()).isEqualTo("{}");
        verify(saveVendorCommunicationHistoryPort).save(any(FulfillmentVendorCommunicationHistory.class));
    }
}
