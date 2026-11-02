package com.personal.marketnote.fulfillment.service.vendor;

import com.personal.marketnote.fulfillment.port.in.command.vendor.CompleteFulfillmentDeliveryIcsCommand;
import com.personal.marketnote.fulfillment.port.in.command.vendor.CompleteFulfillmentDeliveryIcsItemCommand;
import com.personal.marketnote.fulfillment.port.in.result.vendor.CompleteFulfillmentDeliveryIcsResult;
import com.personal.marketnote.fulfillment.port.out.vendor.CompleteFulfillmentDeliveryIcsPort;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("CompleteFulfillmentDeliveryIcsService 테스트")
class CompleteFulfillmentDeliveryIcsUseCaseTest {
    @InjectMocks
    private CompleteFulfillmentDeliveryIcsService completeFulfillmentDeliveryIcsService;

    @Mock
    private CompleteFulfillmentDeliveryIcsPort completeFulfillmentDeliveryIcsPort;

    @Test
    @DisplayName("출고 ICS 완료 커맨드를 전달하면 포트를 통해 완료 결과를 반환한다")
    void shouldReturnCompleteDeliveryIcsResultFromPort() {
        // given
        CompleteFulfillmentDeliveryIcsItemCommand itemCommand = CompleteFulfillmentDeliveryIcsItemCommand.of(
                List.of("ORD001")
        );
        CompleteFulfillmentDeliveryIcsCommand command = CompleteFulfillmentDeliveryIcsCommand.of(
                "CUST001", "token", List.of(itemCommand)
        );
        CompleteFulfillmentDeliveryIcsResult expectedResult = CompleteFulfillmentDeliveryIcsResult.of(0, List.of());
        when(completeFulfillmentDeliveryIcsPort.completeDeliveryIcs(any())).thenReturn(expectedResult);

        // when
        CompleteFulfillmentDeliveryIcsResult result = completeFulfillmentDeliveryIcsService.completeDeliveryIcs(command);

        // then
        assertThat(result).isEqualTo(expectedResult);
        verify(completeFulfillmentDeliveryIcsPort).completeDeliveryIcs(any());
    }
}
