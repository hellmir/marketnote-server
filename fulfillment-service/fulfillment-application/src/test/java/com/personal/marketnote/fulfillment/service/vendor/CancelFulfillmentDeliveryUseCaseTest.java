package com.personal.marketnote.fulfillment.service.vendor;

import com.personal.marketnote.fulfillment.port.in.command.vendor.CancelFulfillmentDeliveryCommand;
import com.personal.marketnote.fulfillment.port.in.command.vendor.CancelFulfillmentDeliveryItemCommand;
import com.personal.marketnote.fulfillment.port.in.result.vendor.CancelFulfillmentDeliveryResult;
import com.personal.marketnote.fulfillment.port.out.vendor.CancelFulfillmentDeliveryPort;
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
@DisplayName("CancelFulfillmentDeliveryService 테스트")
class CancelFulfillmentDeliveryUseCaseTest {
    @InjectMocks
    private CancelFulfillmentDeliveryService cancelFulfillmentDeliveryService;

    @Mock
    private CancelFulfillmentDeliveryPort cancelFulfillmentDeliveryPort;

    @Test
    @DisplayName("출고 취소 커맨드를 전달하면 포트를 통해 취소 결과를 반환한다")
    void shouldReturnCancelDeliveryResultFromPort() {
        // given
        CancelFulfillmentDeliveryItemCommand itemCommand = CancelFulfillmentDeliveryItemCommand.of(
                "SLIP001", "ORD001"
        );
        CancelFulfillmentDeliveryCommand command = CancelFulfillmentDeliveryCommand.of(
                "CUST001", "token", List.of(itemCommand)
        );
        CancelFulfillmentDeliveryResult expectedResult = CancelFulfillmentDeliveryResult.of(0, List.of());
        when(cancelFulfillmentDeliveryPort.cancelDelivery(any())).thenReturn(expectedResult);

        // when
        CancelFulfillmentDeliveryResult result = cancelFulfillmentDeliveryService.cancelDelivery(command);

        // then
        assertThat(result).isEqualTo(expectedResult);
        verify(cancelFulfillmentDeliveryPort).cancelDelivery(any());
    }
}
