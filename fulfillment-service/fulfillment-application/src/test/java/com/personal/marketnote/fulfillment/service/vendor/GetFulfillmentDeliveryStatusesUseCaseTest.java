package com.personal.marketnote.fulfillment.service.vendor;

import com.personal.marketnote.fulfillment.port.in.command.vendor.GetFulfillmentDeliveryStatusesCommand;
import com.personal.marketnote.fulfillment.port.in.result.vendor.GetFulfillmentDeliveryStatusesResult;
import com.personal.marketnote.fulfillment.port.out.vendor.GetFulfillmentDeliveryStatusesPort;
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
@DisplayName("GetFulfillmentDeliveryStatusesService 테스트")
class GetFulfillmentDeliveryStatusesUseCaseTest {
    @InjectMocks
    private GetFulfillmentDeliveryStatusesService getFulfillmentDeliveryStatusesService;

    @Mock
    private GetFulfillmentDeliveryStatusesPort getFulfillmentDeliveryStatusesPort;

    @Test
    @DisplayName("출고 상태 목록 조회 커맨드를 전달하면 포트를 통해 상태 목록을 반환한다")
    void shouldReturnDeliveryStatusesResultFromPort() {
        // given
        GetFulfillmentDeliveryStatusesCommand command = GetFulfillmentDeliveryStatusesCommand.of(
                "CUST001", "token", "2026-01-01", "2026-01-31", "OUT_DIV"
        );
        GetFulfillmentDeliveryStatusesResult expectedResult = GetFulfillmentDeliveryStatusesResult.of(0, List.of());
        when(getFulfillmentDeliveryStatusesPort.getDeliveryStatuses(any())).thenReturn(expectedResult);

        // when
        GetFulfillmentDeliveryStatusesResult result = getFulfillmentDeliveryStatusesService.getDeliveryStatuses(command);

        // then
        assertThat(result).isEqualTo(expectedResult);
        verify(getFulfillmentDeliveryStatusesPort).getDeliveryStatuses(any());
    }
}
